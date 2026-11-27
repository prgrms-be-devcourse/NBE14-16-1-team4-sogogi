package com.team4.sogogi.order.repository;

import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    // 이메일 병합 대상: 같은 이메일 + 같은 배송일 + 지정한 상태(ORDERED)의 가장 오래된 주문
    Optional<Order> findFirstByEmailIgnoreCaseAndDeliveryDateAndStatusOrderByIdAsc(
            String email, LocalDate deliveryDate, OrderStatus status);

    // 관리자 주문 목록: 배송일(배송 그룹)별 조회
    List<Order> findByDeliveryDate(LocalDate deliveryDate, Sort sort);
}
