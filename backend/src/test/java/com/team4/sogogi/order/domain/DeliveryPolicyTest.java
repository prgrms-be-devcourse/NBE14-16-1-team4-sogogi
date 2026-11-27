package com.team4.sogogi.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeliveryPolicy 경계값 분석 테스트.
 * 구간 A(00:00:00 ~ 13:59:59.999999999) → 당일 배송
 * 구간 B(14:00:00 ~ 23:59:59.999999999) → 다음 날 배송
 */
class DeliveryPolicyTest {

    private final DeliveryPolicy deliveryPolicy = new DeliveryPolicy();

    @DisplayName("마감(14:00) 전 주문은 당일 배송")
    @ParameterizedTest(name = "[{index}] {0} 주문 → {1} 배송")
    @CsvSource({
            "2026-11-27T00:00:00,           2026-11-27",   // 하루 시작
            "2026-11-27T13:59:59,           2026-11-27",   // 마감 1초 전
            "2026-11-27T13:59:59.999999999, 2026-11-27"    // 마감 직전 최소 단위
    })
    void 마감_전_주문은_당일_배송(LocalDateTime orderedAt, LocalDate expected) {
        assertThat(deliveryPolicy.getDeliveryDate(orderedAt)).isEqualTo(expected);
    }

    @DisplayName("마감(14:00) 정각 포함 이후 주문은 다음 날 배송")
    @ParameterizedTest(name = "[{index}] {0} 주문 → {1} 배송")
    @CsvSource({
            "2026-11-27T14:00:00,           2026-11-28",   // 마감 정각 (포함)
            "2026-11-27T14:00:01,           2026-11-28",   // 마감 1초 후
            "2026-11-27T23:59:59.999999999, 2026-11-28"    // 하루 끝
    })
    void 마감_이후_주문은_다음날_배송(LocalDateTime orderedAt, LocalDate expected) {
        assertThat(deliveryPolicy.getDeliveryDate(orderedAt)).isEqualTo(expected);
    }

    @DisplayName("마감 이후 주문 시 월·연·윤년 경계를 넘어 다음 날로 계산")
    @ParameterizedTest(name = "[{index}] {0} 주문 → {1} 배송")
    @CsvSource({
            "2026-11-30T14:00:00, 2026-12-01",   // 월말
            "2026-12-31T14:00:00, 2027-01-01",   // 연말
            "2026-02-28T14:00:00, 2026-03-01",   // 평년 2월 말
            "2028-02-28T14:00:00, 2028-02-29"    // 윤년 2월 말
    })
    void 날짜_경계를_넘는_다음날_배송(LocalDateTime orderedAt, LocalDate expected) {
        assertThat(deliveryPolicy.getDeliveryDate(orderedAt)).isEqualTo(expected);
    }
}