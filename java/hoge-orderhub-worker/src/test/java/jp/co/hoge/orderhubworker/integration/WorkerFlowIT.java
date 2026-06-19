package jp.co.hoge.orderhubworker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.dto.BarShipmentAcceptedResponse;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderLineRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhubworker.OrderHubWorkerApplication;
import jp.co.hoge.orderhubworker.service.MockBarDeliveryClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = OrderHubWorkerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkerFlowIT {
    @Autowired private MockMvc mockMvc;
    @Autowired private ShipmentRequestRepository shipmentRequestRepository;
    @Autowired private OrderHeaderRepository orderHeaderRepository;
    @Autowired private OrderLineRepository orderLineRepository;
    @Autowired private NotificationHistoryRepository notificationHistoryRepository;
    @Autowired private DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

    @MockBean
    private MockBarDeliveryClient mockBarDeliveryClient;

    private static final Path TEMP_DIR = initTempDir();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("hoge.files.foo-ack-dir", () -> TEMP_DIR.resolve("ack").toString());
        registry.add("hoge.files.foo-status-dir", () -> TEMP_DIR.resolve("status").toString());
    }

    @Test
    void shouldDispatchAndWriteFooAckAndStatusFile() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        OrderHeaderEntity order = new OrderHeaderEntity();
        order.setOrderId("O-1");
        order.setPartnerOrderId("FO202606170099");
        order.setOrderSource(OrderSource.FOO);
        order.setPartnerPriorityLevel(8);
        order.setShippingPriorityClass(ShippingPriorityClass.PRIORITY);
        order.setCustomerId("C00000000001");
        order.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
        order.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
        order.setCarrierCode(CarrierCode.BAR);
        order.setDeliveryZipCode("1000001");
        order.setDeliveryAddress("Tokyo");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderHeaderRepository.save(order);

        OrderLineEntity line = new OrderLineEntity();
        line.setOrderId("O-1");
        line.setOrderLineNo(1);
        line.setItemCode("ITM0000001");
        line.setItemName("FooOrderedItem");
        line.setQuantity(1);
        orderLineRepository.save(line);

        ShipmentRequestEntity shipmentRequest = new ShipmentRequestEntity();
        shipmentRequest.setShipmentRequestId("SHP-1");
        shipmentRequest.setOrderId("O-1");
        shipmentRequest.setCarrierCode(CarrierCode.BAR);
        shipmentRequest.setOrderSource(OrderSource.FOO);
        shipmentRequest.setPartnerPriorityLevel(8);
        shipmentRequest.setShippingPriorityClass(ShippingPriorityClass.PRIORITY);
        shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
        shipmentRequest.setQueueEnqueuedAt(now);
        shipmentRequest.setNextRequestAfter(now.minusMinutes(1));
        shipmentRequestRepository.save(shipmentRequest);

        when(mockBarDeliveryClient.requestShipment(anyString(), any()))
                .thenReturn(new BarShipmentAcceptedResponse("BAR-1", "SHP-1", "ACCEPTED", now.toString(), false));

        NotificationHistoryEntity ack = new NotificationHistoryEntity();
        ack.setNotificationId("NTF-ACK-1");
        ack.setOrderId("O-1");
        ack.setNotificationType(NotificationType.FOO_ACK);
        ack.setNotificationStatus(NotificationStatus.PENDING);
        ack.setEventType("ORDER_ACCEPTED");
        ack.setPayloadSummary("RECEIVED|HOGE-ACK-000|" + now);
        ack.setDestination("foo-ack-file");
        ack.setCreatedAt(now);
        ack.setUpdatedAt(now);
        notificationHistoryRepository.save(ack);

        mockMvc.perform(post("/internal/workers/foo-ack/run"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/internal/workers/dispatch/run"))
                .andExpect(status().isOk());

        NotificationHistoryEntity pending = new NotificationHistoryEntity();
        pending.setNotificationId("NTF-1");
        pending.setOrderId("O-1");
        pending.setNotificationType(NotificationType.FOO_STATUS);
        pending.setNotificationStatus(NotificationStatus.PENDING);
        pending.setPayloadSummary("PREPARING");
        pending.setDestination("foo-status-file");
        pending.setCreatedAt(now);
        pending.setUpdatedAt(now);
        notificationHistoryRepository.save(pending);

        DeliveryStatusCurrentEntity current = new DeliveryStatusCurrentEntity();
        current.setOrderId("O-1");
        current.setLatestStatusCode("PREPARING");
        current.setLatestStatusName("Preparing");
        current.setLatestStatusSeq(1);
        current.setLatestStatusAt(now);
        current.setLastReceivedAt(now);
        deliveryStatusCurrentRepository.save(current);

        mockMvc.perform(post("/internal/workers/foo-status/run"))
                .andExpect(status().isOk());

        try (var files = Files.list(TEMP_DIR.resolve("ack"))) {
            assertThat(files.count()).isEqualTo(1);
        }
        try (var files = Files.list(TEMP_DIR.resolve("status"))) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    private static Path initTempDir() {
        try {
            return Files.createTempDirectory("worker-flow-it");
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
