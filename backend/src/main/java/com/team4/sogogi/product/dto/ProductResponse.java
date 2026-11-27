package com.team4.sogogi.product.dto;

import com.team4.sogogi.product.domain.Product;

import java.time.LocalDateTime;

/**
 * 상품 응답 본문.
 */
public record ProductResponse(
        Long id,
        String name,
        String category,
        int price,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getDescription(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
