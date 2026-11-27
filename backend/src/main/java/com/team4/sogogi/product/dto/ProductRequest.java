package com.team4.sogogi.product.dto;

import com.team4.sogogi.product.domain.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 상품 등록·수정 요청 본문.
 */
public record ProductRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 255, message = "상품명은 255자 이하여야 합니다.")
        String name,

        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(max = 255, message = "카테고리는 255자 이하여야 합니다.")
        String category,

        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 0보다 커야 합니다.")
        Integer price,

        @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
        String description
) {

    public Product toEntity() {
        return Product.builder()
                .name(name)
                .category(category)
                .price(price)
                .description(description)
                .build();
    }
}
