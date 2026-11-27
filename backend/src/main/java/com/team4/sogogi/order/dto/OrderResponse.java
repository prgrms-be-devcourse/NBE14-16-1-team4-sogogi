package com.team4.sogogi.order.dto;

import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 주문 응답 본문
 * 주문 생성, 고객 조회, 관리자 목록에서 함께 사용
 */
public record OrderResponse(
        Long orderId,
        String email,
        String address,
        String postcode,
        LocalDate deliveryDate,
        OrderStatus status,
        int totalPrice,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getEmail(),
                order.getAddress(),
                order.getPostcode(),
                order.getDeliveryDate(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}
