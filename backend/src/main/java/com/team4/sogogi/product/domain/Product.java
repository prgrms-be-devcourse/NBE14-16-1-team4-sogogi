package com.team4.sogogi.product.domain;

import com.team4.sogogi.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int price;

    private String description;

    @Builder
    public Product(String name, String category, int price, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
    }

    public void update(String name, String category, int price, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
    }
}
