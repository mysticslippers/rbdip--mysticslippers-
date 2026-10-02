package com.rbdip.bookstore.order.utils;

import com.rbdip.bookstore.order.CreateOrderRequest;

import java.util.List;

public final class OrderRequestValidator {

    private OrderRequestValidator() {}

    public static void validateCreateRequest(CreateOrderRequest request) {
        isFullNameValid(request.customerFullName());
        isAddressValid(request.customerAddress());
        isItemsValid(request.items());
    }

    private static void isFullNameValid(String fullName) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("customerFullName is required");
    }

    private static void isAddressValid(String address) {
        if (address == null || address.isBlank())
            throw new IllegalArgumentException("customerAddress is required");
    }

    private static void isItemsValid(List<CreateOrderRequest.Item> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("order must contain at least one item");
    }
}
