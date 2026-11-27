package com.team4.sogogi.order.service;

import com.team4.sogogi.global.exception.BusinessException;
import com.team4.sogogi.global.exception.ErrorCode;
import com.team4.sogogi.order.domain.DeliveryPolicy;
import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderItem;
import com.team4.sogogi.order.dto.OrderCreateRequest;
import com.team4.sogogi.order.dto.OrderItemRequest;
import com.team4.sogogi.order.dto.OrderResponse;
import com.team4.sogogi.order.repository.OrderRepository;
import com.team4.sogogi.product.domain.Product;
import com.team4.sogogi.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DeliveryPolicy deliveryPolicy;

    public List<OrderResponse> findByEmail(String email) {
        return orderRepository.findByEmailOrderByCreatedAtDesc(email).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Order order = Order.builder()
                .email(request.email())
                .address(request.address())
                .postcode(request.postcode())
                .deliveryDate(deliveryPolicy.getDeliveryDateKorea())
                .build();

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + itemRequest.productId()));
            order.addOrderItem(OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .build());
        }

        //TODO(#20): 이메일 병합 로직이 merge 되면 병합 메서드 호출로 교체
        return OrderResponse.from(orderRepository.save(order));
    }
}
