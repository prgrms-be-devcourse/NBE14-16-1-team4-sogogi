package com.team4.sogogi.order.domain;

import com.team4.sogogi.global.common.BaseTimeEntity;
import com.team4.sogogi.product.domain.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// ORDER는 SQL 예약어라서 테이블 이름을 orders로 둔다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String postcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // 배송 그룹 (DeliveryPolicy가 주문 시각으로 계산한 배송일)
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(String email, String address, String postcode, LocalDate deliveryDate) {
        this.email = email;
        this.address = address;
        this.postcode = postcode;
        this.deliveryDate = deliveryDate;
        this.status = OrderStatus.ORDERED;
    }

    // 양방향 연관관계를 한 번에 맞춘다.
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    // 같은 상품이 이미 있으면 수량을 합치고, 없으면 새 주문 항목으로 추가한다.
    public void addItem(Product product, int quantity) {
        orderItems.stream()
                .filter(item -> isSameProduct(item.getProduct(), product))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.changeQuantity(item.getQuantity() + quantity),
                        () -> addOrderItem(OrderItem.builder()
                                .product(product)
                                .quantity(quantity)
                                .build())
                );
    }
    // 저장된 상품은 id로, 저장 전 상품(단위 테스트)은 같은 객체인지로 비교한다.
    private boolean isSameProduct(Product a, Product b) {
        if (a == b) {
            return true;
        }
        return a.getId() != null && a.getId().equals(b.getId());
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }

    public int getTotalPrice() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    // 밖에서 목록을 직접 바꾸지 못하도록 복사본을 준다. (Lombok getter 대신 사용)
    public List<OrderItem> getOrderItems() {
        return List.copyOf(orderItems);
    }
}
