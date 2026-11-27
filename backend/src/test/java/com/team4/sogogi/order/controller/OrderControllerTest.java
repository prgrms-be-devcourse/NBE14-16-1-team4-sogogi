package com.team4.sogogi.order.controller;

import com.team4.sogogi.order.domain.Order;
import com.team4.sogogi.order.domain.OrderItem;
import com.team4.sogogi.product.domain.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    private static final String EMAIL = "a@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    private Order saveOrder(String email, Product product, int quantity) {
        Order order = Order.builder()
                .email(email).address("서울").postcode("06236")
                .deliveryDate(LocalDate.of(2026, 11, 27))
                .build();
        order.addOrderItem(OrderItem.builder().product(product).quantity(quantity).build());
        em.persist(order);
        em.flush();
        return order;
    }

    /**
     * 테스트용 상품을 DB에 저장한다. (주문하려면 상품이 먼저 있어야 함)
     */
    private Product saveProduct(String name, int price) {
        Product product = Product.builder()
                .name(name).category("커피콩").price(price).description("설명")
                .build();
        em.persist(product);
        em.flush();
        return product;
    }

    /**
     * 주문 생성 요청 JSON을 만든다. 이메일·상품 id·수량만 바꿔 가며 쓴다.
     */
    private static String orderBody(String email, long productId, int quantity) {
        return """
                {
                  "email": "%s",
                  "address": "서울시 강남구",
                  "postcode": "06236",
                  "items": [{ "productId": %d, "quantity": %d }]
                }
                """.formatted(email, productId, quantity);
    }


    @Test
    void 내_주문을_최신순으로_조회() throws Exception {
        Product product = Product.builder().name("A").category("커피콩").price(5000).description("설명").build();
        em.persist(product);
        Order older = saveOrder(EMAIL, product, 1);
        Order newer = saveOrder(EMAIL, product, 2);
        saveOrder("other@example.com", product, 1);

        mockMvc.perform(get("/api/orders").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(newer.getId()))
                .andExpect(jsonPath("$[0].totalPrice").value(10000))
                .andExpect(jsonPath("$[0].items[0].name").value("A"))
                .andExpect(jsonPath("$[1].orderId").value(older.getId()));
    }

    @Test
    void 주문이_없으면_빈_배열() throws Exception {
        mockMvc.perform(get("/api/orders").param("email", "none@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 이메일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(get("/api/orders").param("email", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("email: 이메일 형식이 올바르지 않습니다."));
    }

    @Test
    void 이메일이_비었으면_400() throws Exception {
        mockMvc.perform(get("/api/orders").param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: 이메일은 필수입니다."));
    }

    @Test
    void 이메일이_없으면_400() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    /**
     * 정상 주문: 201과 함께 주문 정보(상태, 배송일, 합계, 상품)를 돌려준다.
     */
    @Test
    void 주문_생성시_201과_주문_정보를_반환() throws Exception {
        // given: 5,000원짜리 상품
        Product product = saveProduct("Columbia Nariño", 5000);

        // when & then: 2개 주문 → 합계 10,000원
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(EMAIL, product.getId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.status").value("ORDERED"))
                // 배송일은 실행 시각(14시 전/후)에 따라 달라서 값 대신 존재 여부만 확인
                .andExpect(jsonPath("$.deliveryDate").exists())
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.items[0].name").value("Columbia Nariño"))
                .andExpect(jsonPath("$.items[0].price").value(5000))   // 주문 당시 가격
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    /**
     * 이메일 형식이 틀리면 400. (@Valid가 빠지면 이 테스트가 실패한다)
     */
    @Test
    void 주문_생성시_이메일_형식이_틀리면_400() throws Exception {
        // given
        Product product = saveProduct("A", 5000);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody("abc", product.getId(), 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("email: 이메일 형식이 올바르지 않습니다."));
    }

    /**
     * 리스트 안의 상품 수량이 0이면 400. (List<@Valid ...>로 리스트 안까지 검증되는지 확인)
     */
    @Test
    void 주문_생성시_수량이_0이면_400() throws Exception {
        // given
        Product product = saveProduct("A", 5000);

        // when & then: 에러 메시지에 몇 번째 상품인지(items[0])까지 나온다
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(EMAIL, product.getId(), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("items[0].quantity: 수량은 1 이상이어야 합니다."));
    }

    /**
     * 존재하지 않는 상품으로 주문하면 404.
     */
    @Test
    void 없는_상품으로_주문하면_404() throws Exception {
        // when & then: Long.MAX_VALUE는 절대 없는 상품 id
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(EMAIL, Long.MAX_VALUE, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    /**
     * 주문 생성(2.2)과 내 주문 조회(2.3)가 이어지는지 확인한다.
     */
    @Test
    void 주문_생성_후_이메일로_조회된다() throws Exception {
        // given
        Product product = saveProduct("A", 5000);

        // when: 주문 생성
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(EMAIL, product.getId(), 1)))
                .andExpect(status().isCreated());

        // then: 같은 이메일로 조회하면 방금 주문이 나온다
        mockMvc.perform(get("/api/orders").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("A"));
    }

}
