package com.rbdip.bookstore.order.utils;

import com.rbdip.bookstore.order.CreateOrderRequest;
import com.rbdip.bookstore.order.Order;
import com.rbdip.bookstore.order.OrderItem;
import com.rbdip.bookstore.order.OrderItemRepository;
import com.rbdip.bookstore.order.OrderRepository;
import com.rbdip.bookstore.order.PricingCalculator;
import com.rbdip.bookstore.product.Product;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderPersistenceService {

    private static final String NEW_ORDER_STATUS = "new";

    private final OrderRepository repository;
    private final OrderItemRepository itemRepository;

    public OrderPersistenceService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.repository = orderRepository;
        this.itemRepository = orderItemRepository;
    }

    public Order saveOrder(CreateOrderRequest request, List<Product> products, List<PricingCalculator.LineItem> lineItems) {
        Order order = new Order(request.customerFullName(), request.customerAddress(), request.customerPhone(), NEW_ORDER_STATUS);

        Order saved = repository.save(order);
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = lineItems.get(i).quantity();
            itemRepository.save(new OrderItem(saved.getId(), product.getName(), product.getPrice(), quantity));
        }

        return saved;
    }
}
