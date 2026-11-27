package com.team4.sogogi.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 주문 생성 요청 본문
 */
public record OrderCreateRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "우편번호는 필수입니다.")
        String postcode,

        @NotEmpty(message = "주문할 상품은 1개 이상이어야 합니다.")
        List<@Valid OrderItemRequest> items
) {

}
