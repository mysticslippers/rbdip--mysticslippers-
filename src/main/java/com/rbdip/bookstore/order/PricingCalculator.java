package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Модуль расчёта цены заказа. Намеренно почти не покрыт тестами и
 * содержит magic numbers / нечитаемые ветвления скидок - цель для
 * характеризационных тестов (ЛР2) и mutation-testing гейта PIT (ЛР5).
 */
public class PricingCalculator {

    private static final int MONEY_SCALE = 2;
    private static final int DISCOUNT_THRESHOLD = 10;
    private static final String VIP_CUSTOMER_TYPE = "vip";
    private static final String WHOLESALE_CUSTOMER_TYPE = "wholesale";
    private static final String FIXED_DISCOUNT_CODE = "SAVE10";
    private static final String PERCENTAGE_DISCOUNT_CODE = "SAVE20PERCENT";
    private static final BigDecimal PROMOCODE_PRICE_COEFFICIENT = new BigDecimal("0.8");
    private static final BigDecimal WHOLESALE_PRICE_COEFFICIENT = new BigDecimal("0.85");
    private static final BigDecimal VIP_PRICE_COEFFICIENT = new BigDecimal("0.9");
    private static final BigDecimal BULK_PRICE_COEFFICIENT = new BigDecimal("0.95");
    private static final BigDecimal HIGH_VALUE_ORDER_PRICE_COEFFICIENT = new BigDecimal("0.98");
    private static final BigDecimal HIGH_VALUE_ORDER_THRESHOLD = new BigDecimal("1000");

    public record LineItem(
            BigDecimal price,
            int quantity
    ) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = BigDecimal.ZERO;

        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > DISCOUNT_THRESHOLD)
                linePrice = linePrice.multiply(BULK_PRICE_COEFFICIENT);

            total = total.add(linePrice);
        }

        if (VIP_CUSTOMER_TYPE.equals(customerType))
            total = total.multiply(VIP_PRICE_COEFFICIENT);
        else if (WHOLESALE_CUSTOMER_TYPE.equals(customerType))
            total = total.multiply(WHOLESALE_PRICE_COEFFICIENT);


        if (FIXED_DISCOUNT_CODE.equals(couponCode))
            total = total.subtract(BigDecimal.TEN);
        else if (PERCENTAGE_DISCOUNT_CODE.equals(couponCode))
            total = total.multiply(PROMOCODE_PRICE_COEFFICIENT);

        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        if (total.compareTo(HIGH_VALUE_ORDER_THRESHOLD) > 0)
            total = total.multiply(HIGH_VALUE_ORDER_PRICE_COEFFICIENT);

        return total.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
