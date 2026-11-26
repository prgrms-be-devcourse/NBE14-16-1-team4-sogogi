package com.team4.sogogi.order.domain;

import com.team4.sogogi.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 당시 가격 (상품 가격이 바뀌어도 유지)
    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.price = product.getPrice();
        this.quantity = quantity;
    }

    // Order.addOrderItem()에서만 호출한다.
    void assignOrder(Order order) {
        this.order = order;
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotalPrice() {
        return price * quantity;
    }
}
