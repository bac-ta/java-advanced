package com.example.javaadvanced.reactorcontext.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Không tìm thấy order id=" + id + " trong tenant hiện tại");
    }
}
