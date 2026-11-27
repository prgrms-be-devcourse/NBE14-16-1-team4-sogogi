package com.team4.sogogi.order.dto;

import com.team4.sogogi.order.domain.OrderItem;

/**
 * 주문 상품 한 줄 응답
 * price 는 주문 당시 가격
 */
public record OrderItemResponse(
        Long productId,
        String name,
        int price,
        int quantity
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getPrice(),
                item.getQuantity()
        );

    }
}


