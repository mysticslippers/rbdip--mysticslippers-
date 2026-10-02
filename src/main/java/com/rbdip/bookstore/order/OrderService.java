package com.rbdip.bookstore.order;

import com.rbdip.bookstore.order.utils.OrderPersistenceService;
import com.rbdip.bookstore.order.utils.OrderNotifier;
import com.rbdip.bookstore.order.utils.OrderRequestValidator;
import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * God-класс: валидация, расчёт цены, персистентность и "уведомление
 * клиента" смешаны в одном методе. Цель для рефакторинга по SRP в ЛР1.
 */
@Service
public class OrderService {

    private static final String REGULAR_CUSTOMER_TYPE = "regular";

    private final ProductRepository productRepository;
    private final OrderPersistenceService orderPersistenceService;
    private final PricingCalculator pricingCalculator = new PricingCalculator();

    public OrderService(ProductRepository productRepository, OrderPersistenceService orderPersistenceService) {
        this.productRepository = productRepository;
        this.orderPersistenceService = orderPersistenceService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        OrderRequestValidator.validateCreateRequest(request);

        List<Product> products = new ArrayList<>();
        List<PricingCalculator.LineItem> lineItems = new ArrayList<>();
        for (CreateOrderRequest.Item raw : request.items()) {
            Product product = productRepository.findById(raw.productId())
                    .orElseThrow(() -> new IllegalArgumentException("product " + raw.productId() + " not found"));
            int quantity = raw.quantity() == null ? 1 : raw.quantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            products.add(product);
            lineItems.add(new PricingCalculator.LineItem(product.getPrice(), quantity));
        }

        BigDecimal total = pricingCalculator.calculateOrderTotal(
                lineItems, request.customerType() == null ? REGULAR_CUSTOMER_TYPE : request.customerType(), request.couponCode());

        Order order = orderPersistenceService.saveOrder(request, products, lineItems);

        OrderNotifier.sendConfirmationEmail(request.customerFullName(), order.getId(), total);

        return order;
    }
}
