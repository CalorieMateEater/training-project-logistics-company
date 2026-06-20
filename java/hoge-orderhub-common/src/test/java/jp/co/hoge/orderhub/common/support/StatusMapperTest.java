package jp.co.hoge.orderhub.common.support;

import static org.assertj.core.api.Assertions.assertThat;

import jp.co.hoge.orderhub.common.domain.DeliveryStatusCode;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import org.junit.jupiter.api.Test;

class StatusMapperTest {
  private final StatusMapper statusMapper = new StatusMapper();

  @Test
  void shouldMapDeliveredToCompleted() {
    assertThat(statusMapper.toOrderStatus(DeliveryStatusCode.DELIVERED))
        .isEqualTo(OrderStatus.COMPLETED);
  }

  @Test
  void shouldMapAddressErrorToException() {
    assertThat(statusMapper.toOrderStatus(DeliveryStatusCode.ADDRESS_ERROR))
        .isEqualTo(OrderStatus.EXCEPTION);
  }
}
