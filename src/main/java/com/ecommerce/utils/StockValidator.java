package com.ecommerce.utils;


import com.ecommerce.exceptions.InsufficientStockException;
import com.ecommerce.exceptions.StockNotFound;
import com.ecommerce.models.Stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class StockValidator {
    private static final Logger logger = LoggerFactory.getLogger(StockValidator.class);

    /**
     *
     * @param stock stock details for a product
     * @param requestedQuantity the quantity of a product that client want to reserve
     * @throws InsufficientStockException if stock is insufficient
     */
    public void validateAvailableQuantity(Stock stock,Integer requestedQuantity){
        if (!stock.canReserve(requestedQuantity)) {
            logger.warn("Insufficient stock: available={}, requested={}",
                    stock.getAvailableQuantity(), requestedQuantity);
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            stock.getAvailableQuantity(), requestedQuantity));
        }

    }
}
