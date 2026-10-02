package com.rbdip.bookstore.order.utils;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OrderNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderNotifier.class);

    private OrderNotifier() {}

    public static void sendConfirmationEmail(String customerName, Long orderId, BigDecimal total) {
        LOGGER.info("[email] Dear {}, your order #{} for {} has been placed.", customerName, orderId, total);
    }
}
