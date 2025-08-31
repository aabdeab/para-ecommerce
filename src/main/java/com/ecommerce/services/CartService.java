package com.ecommerce.services;

import com.ecommerce.DTOs.CartItemDto;
import com.ecommerce.DTOs.CartItemSummary;
import com.ecommerce.DTOs.CartSummary;
import com.ecommerce.exceptions.StockNotFound;
import com.ecommerce.models.Cart;
import com.ecommerce.models.CartItem;
import com.ecommerce.models.Product;
import com.ecommerce.models.Stock;
import com.ecommerce.repositories.CartRepository;
import com.ecommerce.repositories.StockRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.utils.CartCalculator;
import com.ecommerce.utils.StockValidator;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final StockValidator stockValidator;
    private final CartCalculator cartCalculator;


    @Cacheable(value = "UserCart",key="#userId")
    public Cart getCartForUser(Long userId){
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found for user: " + userId));

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }
        return cart;
    };

    @CacheEvict(value = "guestCartItems", key = "#guestCartId")
    public void addItemToGuestCart(String guestCartId, CartItemDto itemDto, String sessionId) {
        validateCartItemDto(itemDto);

        Product product = findProductOrThrow(itemDto.getProductId());
        Stock stock = findStockOrThrow(product.getProductId());
        stockValidator.validateAvailableQuantity(stock, itemDto.getQuantity());

        Cart cart = findOrCreateGuestCart(guestCartId, sessionId);
        addOrUpdateCartItem(cart, itemDto, product, stock);
        saveCartWithUpdatedTotals(cart);

        logger.info("Item added to guest cart: guestCartId={}, productId={}, quantity={}",
                guestCartId, itemDto.getProductId(), itemDto.getQuantity());
    }


    @CacheEvict(value = "guestCartItems", key = "#guestCartId")
    public void clearGuestCart(String guestCartId) {
        cartRepository.findByGuestCartId(guestCartId).ifPresent(cart -> {
            cart.clearItems();
            cartRepository.save(cart);
            logger.info("Guest cart cleared: guestCartId={}", guestCartId);
        });
    }

    @Cacheable(value = "guestCartItems", key = "#guestCartId")
    public CartSummary getGuestCartSummary(String guestCartId) {
        return cartRepository.findByGuestCartId(guestCartId)
                .map(this::buildCartSummary)
                .orElse(CartSummary.emptyGuest(guestCartId));
    }

    @CacheEvict(value = "userCartItems", key = "#userId")
    public void addItemToUserCart(Long userId, CartItemDto itemDto) {
        validateCartItemDto(itemDto);

        Product product = findProductOrThrow(itemDto.getProductId());
        Stock stock = findStockOrThrow(product.getProductId());
        stockValidator.validateAvailableQuantity(stock, itemDto.getQuantity());

        Cart cart = findOrCreateUserCart(userId);
        addOrUpdateCartItem(cart, itemDto, product, stock);
        saveCartWithUpdatedTotals(cart);

        logger.info("Item added to user cart: userId={}, productId={}, quantity={}",
                userId, itemDto.getProductId(), itemDto.getQuantity());
    }

    @CacheEvict(value = "userCartItems", key = "#userId")
    public void removeItemFromUserCart(Long userId, Long productId) {
        Cart cart = findUserCartOrThrow(userId);

        boolean itemRemoved = cart.getItems().removeIf(item ->
                Objects.equals(item.getProductId(), productId));

        if (!itemRemoved) {
            logger.warn("Attempted to remove non-existent item from user cart: userId={}, productId={}",
                    userId, productId);
            return;
        }

        saveCartWithUpdatedTotals(cart);
        logger.info("Item removed from user cart: userId={}, productId={}", userId, productId);
    }

    @CacheEvict(value = "userCartItems", key = "#userId")
    public void clearUserCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.clearItems();
            cartRepository.save(cart);
            logger.info("User cart cleared: userId={}", userId);
        });
    }

    @Cacheable(value = "userCartItems", key = "#userId")
    public CartSummary getUserCartSummary(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(this::buildCartSummary)
                .orElse(CartSummary.emptyUser(userId));
    }

    @CacheEvict(value = "userCartItems", key = "#userId")
    public void updateUserCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        if (isQuantityInvalid(newQuantity)) {
            removeItemFromUserCart(userId, productId);
            return;
        }

        Cart cart = findUserCartOrThrow(userId);
        CartItem item = findCartItemOrThrow(cart, productId);
        Stock stock = findStockOrThrow(productId);

        stockValidator.validateAvailableQuantity(stock, newQuantity);

        item.updateQuantity(newQuantity);
        saveCartWithUpdatedTotals(cart);

        logger.info("User cart item quantity updated: userId={}, productId={}, newQuantity={}",
                userId, productId, newQuantity);
    }

    @Cacheable(value = "userCartItemCount", key = "#userId")
    public Integer getUserCartItemCount(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(Cart::getTotalItems)
                .orElse(0);
    }

    public boolean userCartExists(Long userId) {
        return cartRepository.existsByUserId(userId);
    }

    // ===== CART MIGRATION (Guest → User after login) =====


    @CacheEvict(value = {"userCartItems", "userCartItemCount"}, key = "#userId")
    public void evictUserCartCache(Long userId) {
        logger.debug("User cart cache evicted: userId={}", userId);
    }

    // ===== PRIVATE HELPER METHODS =====

    private Cart findOrCreateGuestCart(String guestCartId, String sessionId) {
        return cartRepository.findByGuestCartId(guestCartId)
                .orElseGet(() -> createNewGuestCart(guestCartId, sessionId));
    }

    private Cart findOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserCart(userId));
    }


    private Cart findUserCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User cart not found: " + userId));
    }

    private Cart createNewGuestCart(String guestCartId, String sessionId) {
        Cart newCart = Cart.builder()
                .guestCartId(guestCartId)
                .userId(null)
                .sessionId(sessionId)
                .isTemporary(true)
                .totalItems(0)
                .totalAmount(0.0)
                .lastUpdatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
        return cartRepository.save(newCart);
    }

    private Cart createNewUserCart(Long userId) {
        Cart newCart = Cart.builder()
                .guestCartId(null)
                .userId(userId)
                .sessionId(null)
                .isTemporary(false)
                .totalItems(0)
                .totalAmount(0.0)
                .lastUpdatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
        return cartRepository.save(newCart);
    }

    private void mergeCartItems(Cart guestCart, Cart userCart) {
        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existingUserItem = userCart.getItems().stream()
                    .filter(item -> item.getProductId().equals(guestItem.getProductId()))
                    .findFirst();

            if (existingUserItem.isPresent()) {
                existingUserItem.get().addQuantity(guestItem.getQuantity());
            } else {
                guestItem.setCart(userCart);
                userCart.getItems().add(guestItem);
            }
        }
        cartRepository.save(userCart);
    }

    private CartSummary buildCartSummary(Cart cart) {
        List<CartItemSummary> itemSummaries = cart.getItems().stream()
                .map(this::buildCartItemSummary)
                .collect(Collectors.toList());

        return CartSummary.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .guestCartId(cart.getGuestCartId())
                .items(itemSummaries)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .isEmpty(cart.getItems().isEmpty())
                .lastUpdatedAt(cart.getLastUpdatedAt())
                .build();
    }

    private CartItemSummary buildCartItemSummary(CartItem cartItem) {
        return productRepository.findById(cartItem.getProductId())
                .map(product -> CartItemSummary.builder()
                        .productId(cartItem.getProductId())
                        .productName(product.getName())
                        .productBrand(product.getBrand())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getPrice())
                        .subtotal(cartItem.calculateSubtotal())
                        .imageId(cartItem.getImageId())
                        .isAvailable(isProductAvailable(product, cartItem.getQuantity()))
                        .build())
                .orElse(CartItemSummary.unavailable(cartItem.getProductId()));
    }

    private boolean isProductAvailable(Product product, Integer requestedQuantity) {
        return stockRepository.findByProduct_ProductId(product.getProductId())
                .map(stock -> stock.getAvailableQuantity() >= requestedQuantity)
                .orElse(false);
    }

    private void validateCartItemDto(CartItemDto itemDto) {
        if (itemDto == null || itemDto.getProductId() == null || itemDto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid cart item data");
        }
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }

    private Stock findStockOrThrow(Long productId) {
        return stockRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new StockNotFound("Stock not found for product: " + productId));
    }

    private CartItem findCartItemOrThrow(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> Objects.equals(item.getProductId(), productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));
    }

    private void addOrUpdateCartItem(Cart cart, CartItemDto itemDto, Product product, Stock stock) {
        Optional<CartItem> existingItem = findExistingCartItem(cart, itemDto.getProductId());

        if (existingItem.isPresent()) {
            updateExistingCartItem(existingItem.get(), itemDto, stock);
        } else {
            addNewCartItem(cart, itemDto, product);
        }
    }

    private Optional<CartItem> findExistingCartItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> Objects.equals(item.getProductId(), productId))
                .findFirst();
    }

    private void updateExistingCartItem(CartItem existingItem, CartItemDto itemDto, Stock stock) {
        int newTotalQuantity = existingItem.getQuantity() + itemDto.getQuantity();
        stockValidator.validateAvailableQuantity(stock, newTotalQuantity);
        existingItem.addQuantity(itemDto.getQuantity());
    }

    private void addNewCartItem(Cart cart, CartItemDto itemDto, Product product) {
        CartItem newItem = CartItem.builder()
                .productId(itemDto.getProductId())
                .cart(cart)
                .price(product.getPrice())
                .quantity(itemDto.getQuantity())
                .imageId(product.getImageUrl())
                .build();
        cart.addItem(newItem);
    }

    private void saveCartWithUpdatedTotals(Cart cart) {
        cartCalculator.updateTotals(cart);
        cart.setLastUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private boolean isQuantityInvalid(Integer quantity) {
        return quantity == null || quantity <= 0;
    }
}


