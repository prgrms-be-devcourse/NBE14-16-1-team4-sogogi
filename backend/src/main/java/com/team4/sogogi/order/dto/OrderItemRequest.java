package com.team4.sogogi.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 상품 한 줄 요청
 * 가격은 받지 않고 서버가 상품에서 가져온다
 */
public record OrderItemRequest(
        @NotNull(message = "상품 id 는 필수입니다.")
        Long productId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
