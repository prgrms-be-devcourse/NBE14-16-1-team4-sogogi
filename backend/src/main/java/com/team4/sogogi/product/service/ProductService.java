package com.team4.sogogi.product.service;

import com.team4.sogogi.global.exception.BusinessException;
import com.team4.sogogi.global.exception.ErrorCode;
import com.team4.sogogi.order.repository.OrderItemRepository;
import com.team4.sogogi.product.domain.Product;
import com.team4.sogogi.product.dto.ProductRequest;
import com.team4.sogogi.product.dto.ProductResponse;
import com.team4.sogogi.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product saved = productRepository.save(request.toEntity());
        return ProductResponse.from(saved);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.from(getProduct(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        product.update(request.name(), request.category(), request.price(), request.description());
        // updatedAt은 flush 시점에 채워지므로 응답 전에 반영한다.
        try {
            productRepository.flush();
        } catch (OptimisticLockingFailureException e) {
            // 조회 직후 다른 요청이 상품을 삭제해 UPDATE 대상 행이 없음
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + id);
        }
        return ProductResponse.from(product);
    }

    /** 주문 이력이 있는 상품은 과거 주문 내역 보존을 위해 삭제하지 않는다. (409) */
    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        if (orderItemRepository.existsByProductId(id)) {
            throw new BusinessException(ErrorCode.PRODUCT_HAS_ORDERS, "id=" + id);
        }
        productRepository.delete(product);
        try {
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // 확인 직후 다른 트랜잭션이 이 상품을 주문해 FK 위반
            throw new BusinessException(ErrorCode.PRODUCT_HAS_ORDERS, "id=" + id);
        } catch (OptimisticLockingFailureException e) {
            // 조회 직후 다른 요청이 먼저 삭제해 DELETE 대상 행이 없음
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + id);
        }
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + id));
    }
}
