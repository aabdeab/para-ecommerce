package com.ecommerce.exceptions;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(String msg){
        super(msg);
    }

}
