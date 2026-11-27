package com.team4.sogogi.order.repository;

import com.team4.sogogi.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** 이 상품을 담은 주문 항목이 하나라도 있으면 true (상품 삭제 가능 여부 판단용) */
    boolean existsByProductId(Long productId);
}
