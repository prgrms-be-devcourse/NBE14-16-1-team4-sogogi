package com.team4.sogogi.product.service;

import com.team4.sogogi.global.exception.BusinessException;
import com.team4.sogogi.global.exception.ErrorCode;
import com.team4.sogogi.order.repository.OrderItemRepository;
import com.team4.sogogi.product.domain.Product;
import com.team4.sogogi.product.dto.ProductRequest;
import com.team4.sogogi.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

/** 다른 요청과 겹쳤을 때도 README 예외 정책(404·409)대로 응답하는지 확인한다. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 확인_후_삭제_사이에_주문이_생겨_FK_위반이면_PRODUCT_HAS_ORDERS() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product()));
        given(orderItemRepository.existsByProductId(1L)).willReturn(false);
        willThrow(new DataIntegrityViolationException("FK")).given(productRepository).flush();

        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_HAS_ORDERS);
    }

    @Test
    void 다른_요청이_먼저_삭제해_지울_행이_없으면_PRODUCT_NOT_FOUND() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product()));
        given(orderItemRepository.existsByProductId(1L)).willReturn(false);
        willThrow(new ObjectOptimisticLockingFailureException(Product.class, 1L))
                .given(productRepository).flush();

        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 수정_중_다른_요청이_삭제하면_PRODUCT_NOT_FOUND() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product()));
        willThrow(new ObjectOptimisticLockingFailureException(Product.class, 1L))
                .given(productRepository).flush();

        ProductRequest request = new ProductRequest("B", "커피콩", 6000, null);

        assertThatThrownBy(() -> productService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private static Product product() {
        return Product.builder().name("A").category("커피콩").price(5000).build();
    }
}
