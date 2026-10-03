package com.rbdip.bookstore.order;

import com.rbdip.bookstore.review.solution.PurchaseFinder;
import org.springframework.stereotype.Service;

@Service
public class OrderPurchaseFinder implements PurchaseFinder {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderPurchaseFinder(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public boolean hasAnyOrdersAndItems() {
        return orderRepository.count() > 0 && orderItemRepository.count() > 0;
    }
}
