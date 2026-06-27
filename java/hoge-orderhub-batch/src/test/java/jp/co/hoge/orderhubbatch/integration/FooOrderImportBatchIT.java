package jp.co.hoge.orderhubbatch.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhubbatch.OrderHubBatchApplication;
import jp.co.hoge.orderhubbatch.service.CustomerRegistryClient;
import jp.co.hoge.orderhubbatch.service.StockKeeperClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = OrderHubBatchApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FooOrderImportBatchIT {
  @Autowired private MockMvc mockMvc;

  @Autowired private OrderHeaderRepository orderHeaderRepository;

  @Autowired private NotificationHistoryRepository notificationHistoryRepository;

  @MockBean private CustomerRegistryClient customerRegistryClient;

  @MockBean private StockKeeperClient stockKeeperClient;

  private static final Path TEMP_DIR = initTempDir();

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("hoge.files.archive-dir", () -> TEMP_DIR.resolve("archive").toString());
  }

  @Test
  void shouldImportFooFileEndToEnd() throws Exception {
    when(customerRegistryClient.findStatus("C00000000001"))
        .thenReturn(new CustomerStatusResponse("C00000000001", "ACTIVE", "GOLD"));
    when(stockKeeperClient.reserve(any()))
        .thenReturn(
            new StockReservationResponse(
                "RSV-1",
                "RESERVED",
                List.of(
                    new StockReservationResponse.ReservationResult(
                        "ITM0000001",
                        2,
                        2,
                        "WH-TYO-01",
                        "RESERVED",
                        100,
                        2,
                        98,
                        "StandardItemA",
                        1000,
                        "AMBIENT",
                        "NORMAL"))));

    Path input = TEMP_DIR.resolve("FOO_ORDER_20260617090000000_001.dat");
    Files.writeString(
        input,
        "D,FO202606170001,02,8,C00000000001,ITM0000001,2,5000,10,2026-06-17T09:00:00,1000001,Tokyo,Test User,0312345678,1,PREPAID,2026-06-18,,2099-06-18T09:00:00");

    mockMvc
        .perform(
            post("/internal/jobs/foo-orders/import")
                .param("path", input.toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(content().string("imported=1"));

    OrderHeaderEntity order =
        orderHeaderRepository.findByPartnerOrderId("FO202606170001").orElseThrow();
    assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.WAITING_SHIPPING_RELEASE);
    NotificationHistoryEntity notification =
        notificationHistoryRepository
            .findFirstByOrderIdAndNotificationTypeAndEventTypeOrderByCreatedAtAsc(
                order.getOrderId(), NotificationType.FOO_ACK, "ORDER_ACCEPTED")
            .orElseThrow();
    assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(notification.getPayloadSummary()).contains("RECEIVED_HOLD");
  }

  private static Path initTempDir() {
    try {
      return Files.createTempDirectory("foo-order-import-it");
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
