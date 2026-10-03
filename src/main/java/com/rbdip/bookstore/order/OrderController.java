package com.rbdip.bookstore.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderController(
            OrderService orderService, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return Map.of("id", order.getId(), "status", order.getStatus());
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> listOrders() {
        List<Order> orders = orderRepository.findAll();
        List<Long> orderIds = orders.stream().map(Order::getId).toList();

        List<OrderItem> orderItems = List.of();
        if (!orderIds.isEmpty())
            orderItems = orderItemRepository.findByOrderIdIn(orderIds);

        Map<Long, List<OrderItem>> itemsByOrder = orderItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream()
                .map(order -> {
                    // N+1: отдельный запрос на позиции для каждого заказа вместо
                    // одного JOIN FETCH / batch-запроса. Цель для ЛР4. (solved)
                    List<OrderItem> items = itemsByOrder.getOrDefault(order.getId(), List.of());
                    return Map.<String, Object>of(
                            "id", order.getId(),
                            "customerFullName", order.getCustomerFullName(),
                            "status", order.getStatus(),
                            "items", items.stream()
                                    .map(i -> Map.of("productName", i.getProductName(), "quantity", i.getQuantity()))
                                    .toList());
                })
                .toList();
    }
}
