package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorTest {

    private static final String REGULAR_CUSTOMER_TYPE = "regular";
    private static final String VIP_CUSTOMER_TYPE = "vip";
    private static final String WHOLESALE_CUSTOMER_TYPE = "wholesale";
    private static final String FIXED_DISCOUNT_CODE = "SAVE10";
    private static final String PERCENTAGE_DISCOUNT_CODE = "SAVE20PERCENT";

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    void unknownCustomerTypesAndPromosDoNotAddDiscounts() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                REGULAR_CUSTOMER_TYPE, "UNKNOWN")).isEqualTo(new BigDecimal("100.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                "unknown", FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    void customerTypesAndPromosAreCaseSensitive() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                "VIP", PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("80.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                VIP_CUSTOMER_TYPE, "save10")).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    void promosCombineWithCustomerDiscounts() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                WHOLESALE_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("75.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                VIP_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("72.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)),
                WHOLESALE_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("68.00"));
    }

    @Test
    void bulkDiscountAppliesBeforeCustomerDiscountAndPromoCode() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 11)),
                VIP_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("84.05"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 11)),
                WHOLESALE_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("71.06"));
    }

    @Test
    void fixedPromoCodeCanReduceTheTotalToZero() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 1)),
                REGULAR_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("0.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("0.00"), 1)),
                VIP_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void nonPositiveQuantitiesHaveZeroTotal() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 0)),
                REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), -1)),
                REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void fixedPromoCodeChangesHighValueDiscountEligibility() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("1010.00"), 1)),
                REGULAR_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("1000.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("1010.01"), 1)),
                REGULAR_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("980.01"));
    }

    @Test
    void percentagePromoCodeChangesHighValueDiscountEligibility() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("1250.00"), 1)),
                REGULAR_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("1000.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("1250.01"), 1)),
                REGULAR_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("980.01"));
    }

    @Test
    void highValueDiscountAppliesAfterAllOtherDiscounts() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("1200.00"), 1)),
                VIP_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("1048.60"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("200.00"), 11)),
                WHOLESALE_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("1392.78"));
    }

    @Test
    void roundsHalfUpToTwoDecimalPlaces() {
        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("0.004"), 1)),
                REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.00"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("0.005"), 1)),
                REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.01"));

        assertThat(calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("0.015"), 1)),
                REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.02"));
    }

    @Test
    void roundsAfterCustomerDiscount() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("0.005"), 1)), VIP_CUSTOMER_TYPE, null);

        assertThat(total).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void sumsEveryLineAndAppliesTheFixedCouponOncePerOrder() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("12.50"), 2),
                        new PricingCalculator.LineItem(new BigDecimal("7.00"), 3)), REGULAR_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE);

        assertThat(total).isEqualTo(new BigDecimal("36.00"));
    }

    @Test
    void bulkDiscountDependsOnEachLineQuantity() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 6),
                        new PricingCalculator.LineItem(new BigDecimal("20.00"), 5)), REGULAR_CUSTOMER_TYPE, null);

        assertThat(total).isEqualTo(new BigDecimal("160.00"));
    }

    @Test
    void bulkDiscountOnlyAffectsTheEligibleLine() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 11),
                        new PricingCalculator.LineItem(new BigDecimal("20.00"), 2)), REGULAR_CUSTOMER_TYPE, null);

        assertThat(total).isEqualTo(new BigDecimal("144.50"));
    }

    @Test
    void highValueDiscountDependsOnTheSumOfAllLines() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("600.00"), 1),
                        new PricingCalculator.LineItem(new BigDecimal("500.00"), 1)), REGULAR_CUSTOMER_TYPE, null);

        assertThat(total).isEqualTo(new BigDecimal("1078.00"));
    }

    @Test
    void roundsAfterSummingLines() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("0.004"), 1),
                        new PricingCalculator.LineItem(new BigDecimal("0.004"), 1)), REGULAR_CUSTOMER_TYPE, null);

        assertThat(total).isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    void clampsTheOrderTotalAfterSummingNegativeAndPositiveLines() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(new PricingCalculator.LineItem(new BigDecimal("-5.00"), 1),
                        new PricingCalculator.LineItem(new BigDecimal("10.00"), 1)), REGULAR_CUSTOMER_TYPE, null);

        assertThat(total).isEqualTo(new BigDecimal("5.00"));
    }

    @Test
    void emptyOrderHasZeroTotal() {
        assertThat(calculator.calculateOrderTotal(List.of(), REGULAR_CUSTOMER_TYPE, null)).isEqualTo(new BigDecimal("0.00"));

        assertThat(calculator.calculateOrderTotal(List.of(), VIP_CUSTOMER_TYPE, FIXED_DISCOUNT_CODE)).isEqualTo(new BigDecimal("0.00"));

        assertThat(calculator.calculateOrderTotal(List.of(), WHOLESALE_CUSTOMER_TYPE, PERCENTAGE_DISCOUNT_CODE)).isEqualTo(new BigDecimal("0.00"));
    }
}
