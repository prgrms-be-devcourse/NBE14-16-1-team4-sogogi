package com.team4.sogogi.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 입력값_검증_실패시_400() throws Exception {
        mockMvc.perform(post("/test/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("quantity: 수량은 1 이상이어야 합니다."));
    }

    @Test
    void 존재하지_않는_리소스_404() throws Exception {
        mockMvc.perform(get("/test/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다. id=99"));
    }

    @Test
    void 상태_충돌_409() throws Exception {
        mockMvc.perform(post("/test/orders/1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_SHIPPED"));
    }

    @Test
    void 예상하지_못한_예외_500() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @RestController
    static class TestController {

        record ItemRequest(@Positive(message = "수량은 1 이상이어야 합니다.") int quantity) {
        }

        @PostMapping("/test/items")
        void create(@Valid @RequestBody ItemRequest request) {
        }

        @GetMapping("/test/products/{id}")
        void findProduct(@PathVariable Long id) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + id);
        }

        @PostMapping("/test/orders/{id}/cancel")
        void cancel(@PathVariable Long id) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_SHIPPED);
        }

        @GetMapping("/test/error")
        void error() {
            throw new IllegalStateException("내부 정보");
        }
    }
}
