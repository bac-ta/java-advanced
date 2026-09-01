package com.example.javaadvanced.reactorcontext.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Not found order id=" + id + " in current tenant");
    }
}
