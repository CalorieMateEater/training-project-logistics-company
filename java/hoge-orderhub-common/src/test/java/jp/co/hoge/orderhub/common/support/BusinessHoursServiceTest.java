package jp.co.hoge.orderhub.common.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BusinessHoursServiceTest {
    private final BusinessHoursService businessHoursService = new BusinessHoursService();

    @Test
    void shouldReturnTrueWithinBusinessHours() {
        assertThat(businessHoursService.isBarBusinessHours(LocalDateTime.of(2026, 6, 17, 9, 0))).isTrue();
    }

    @Test
    void shouldReturnNextWeekdayMorningOutsideBusinessHours() {
        LocalDateTime next = businessHoursService.nextBarBusinessTime(LocalDateTime.of(2026, 6, 20, 10, 0));
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 6, 22, 8, 0));
    }
}
