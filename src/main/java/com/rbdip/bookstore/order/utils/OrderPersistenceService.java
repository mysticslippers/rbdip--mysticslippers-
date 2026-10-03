package com.rbdip.bookstore.order.utils;

import com.rbdip.bookstore.order.CreateOrderRequest;
import com.rbdip.bookstore.order.persistence.CustomerRepository;
import com.rbdip.bookstore.order.Order;
import com.rbdip.bookstore.order.OrderItem;
import com.rbdip.bookstore.order.OrderItemRepository;
import com.rbdip.bookstore.order.OrderRepository;
import com.rbdip.bookstore.order.PricingCalculator;
import com.rbdip.bookstore.order.domain.Customer;
import com.rbdip.bookstore.product.Product;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderPersistenceService {

    private static final String NEW_ORDER_STATUS = "new";

    private final OrderRepository repository;
    private final OrderItemRepository itemRepository;
    private final CustomerRepository customerRepository;

    public OrderPersistenceService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CustomerRepository customerRepository) {
        this.repository = orderRepository;
        this.itemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
    }

    public Order saveOrder(CreateOrderRequest request, List<Product> products, List<PricingCalculator.LineItem> lineItems) {
        Customer newCustomer = new Customer(request.customerFullName(), request.customerAddress(), request.customerPhone());

        Customer customer = customerRepository.findFirstByFirstNameAndLastNameAndAddressAndPhone(
                newCustomer.getFirstName(), newCustomer.getLastName(), newCustomer.getAddress(), newCustomer.getPhone())
                .orElseGet(() -> customerRepository.save(newCustomer));


        Order order = new Order(customer, NEW_ORDER_STATUS);

        Order saved = repository.save(order);
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = lineItems.get(i).quantity();
            itemRepository.save(new OrderItem(saved.getId(), product, quantity));
        }

        return saved;
    }
}
