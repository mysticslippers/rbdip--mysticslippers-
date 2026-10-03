package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CharacterizationTest {

    private static final String REGULAR_CUSTOMER_TYPE = "regular";
    private static final String VIP_CUSTOMER_TYPE = "vip";
    private static final String WHOLESALE_CUSTOMER_TYPE = "wholesale";
    private static final String FIXED_DISCOUNT_CODE = "SAVE10";
    private static final String PERCENTAGE_DISCOUNT_CODE = "SAVE20PERCENT";

    private final PricingCalculator calculator = new PricingCalculator();

    private BigDecimal calculate(String price, int quantity, String customerType, String promoCode) {
        return calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal(price), quantity)),
                customerType, promoCode);
    }

    @Test
    void customerTypeDeterminesDiscount() {
        assertThat(calculate("100.00", 1, VIP_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("90.00"));
        assertThat(calculate("100.00", 1, WHOLESALE_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("85.00"));
        assertThat(calculate("100.00", 1, null, null)).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void promoCodeApplyFixedOrPercentageDiscount() {
        assertThat(calculate("15.00", 1, REGULAR_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("5.00"));
        assertThat(calculate("100.00", 1, REGULAR_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void promoCodeAppliesAfterCustomerDiscount() {
        assertThat(calculate("100.00", 1, VIP_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void sizeOfDiscountStartsOnlyAboveTenItems() {
        assertThat(calculate("10.00", 10, REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("100.00"));
        assertThat(calculate("10.00", 11, REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("104.50"));
    }

    @Test
    void negativeTotalsBecomeZero() {
        assertThat(calculate("5.00", 1, REGULAR_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("0.00"));
        assertThat(calculate("-5.00", 1, REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void discountsCanMoveOrderBelowHighValueThreshold() {
        assertThat(calculate("1100.00", 1, REGULAR_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("880.00"));
    }

    @Test
    void highValueDiscountStartsOnlyAboveOneThousand() {
        assertThat(calculate("1000.00", 1, REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("1000.00"));
        assertThat(calculate("1000.01", 1, REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("980.01"));
    }
}
