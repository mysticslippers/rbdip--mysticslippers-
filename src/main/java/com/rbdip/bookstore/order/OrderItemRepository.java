package com.rbdip.bookstore.order;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Намеренно нет @EntityGraph/JOIN FETCH-варианта - используется в цикле
    // контроллером, что и создаёт N+1 (см. OrderController#listOrders). (solved)
    @EntityGraph(attributePaths = "product")
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}
