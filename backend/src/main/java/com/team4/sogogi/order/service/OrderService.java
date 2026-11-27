package com.team4.sogogi.order.service;

import com.team4.sogogi.global.exception.BusinessException;
import com.team4.sogogi.global.exception.ErrorCode;
import com.team4.sogogi.order.domain.DeliveryPolicy;
import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderStatus;
import com.team4.sogogi.order.dto.OrderCreateRequest;
import com.team4.sogogi.order.dto.OrderItemRequest;
import com.team4.sogogi.order.dto.OrderResponse;
import com.team4.sogogi.order.repository.OrderRepository;
import com.team4.sogogi.product.domain.Product;
import com.team4.sogogi.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /**
     * 같은 이메일 + 같은 배송일 + ORDERED 주문이 있으면 그 주문에 합치고(같은 상품은 수량 합산),
     * 없으면 새 주문을 만든다. 취소·배송된 주문에는 합치지 않는다.
     */
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        LocalDate deliveryDate = deliveryPolicy.getDeliveryDateKorea();

        Order order = orderRepository
                .findFirstByEmailAndDeliveryDateAndStatusOrderByIdAsc(
                        request.email(), deliveryDate, OrderStatus.ORDERED)
                .map(existing -> {
                    // 현재 정책: 같은 이메일·다른 주소면 최신 주소로 갱신 (주소별로 분리는 추후 추가)
                    existing.changeAddress(request.address(), request.postcode());
                    return existing;
                })
                .orElseGet(() -> Order.builder()
                        .email(request.email())
                        .address(request.address())
                        .postcode(request.postcode())
                        .deliveryDate(deliveryDate)
                        .build());

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + itemRequest.productId()));
            order.addItem(product, itemRequest.quantity());
        }

        return OrderResponse.from(orderRepository.save(order));
    }
}
