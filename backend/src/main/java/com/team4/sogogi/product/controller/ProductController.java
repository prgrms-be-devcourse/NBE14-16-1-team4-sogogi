package com.team4.sogogi.product.controller;

import com.team4.sogogi.product.dto.ProductRequest;
import com.team4.sogogi.product.dto.ProductResponse;
import com.team4.sogogi.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "상품")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    @Operation(summary = "상품 목록 조회")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @Operation(summary = "상품 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "상품 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(summary = "상품 삭제", description = "주문 이력이 있는 상품은 삭제할 수 없습니다. (409)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
