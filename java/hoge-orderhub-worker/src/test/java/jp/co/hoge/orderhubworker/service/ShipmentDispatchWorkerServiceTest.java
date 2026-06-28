package jp.co.hoge.orderhubworker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.dto.BarShipmentAcceptedResponse;
import jp.co.hoge.orderhub.common.integration.SqsMessageGateway;
import jp.co.hoge.orderhub.common.mapper.NotificationHistoryEntityMapper;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.BarIdempotencyHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderLineRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.support.BusinessHoursService;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubworker.mapper.ShipmentDispatchMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentDispatchWorkerServiceTest {
  @Mock private ShipmentRequestRepository shipmentRequestRepository;
  @Mock private OrderHeaderRepository orderHeaderRepository;
  @Mock private OrderLineRepository orderLineRepository;
  @Mock private BarIdempotencyHistoryRepository barIdempotencyHistoryRepository;
  @Mock private NotificationHistoryRepository notificationHistoryRepository;
  @Mock private MockBarDeliveryClient mockBarDeliveryClient;
  @Mock private MockFugaDeliveryClient mockFugaDeliveryClient;
  @Mock private InterfaceHistoryService interfaceHistoryService;
  @Mock private BusinessHoursService businessHoursService;
  @Mock private IdFactory idFactory;
  @Mock private SqsMessageGateway sqsMessageGateway;
  @Captor private ArgumentCaptor<ShipmentRequestEntity> shipmentRequestCaptor;

  private ShipmentDispatchWorkerService shipmentDispatchWorkerService;
  private TimeProvider timeProvider;

  @BeforeEach
  void setUp() {
    timeProvider = () -> LocalDateTime.of(2026, 6, 17, 9, 0, 0);
    shipmentDispatchWorkerService =
        new ShipmentDispatchWorkerService(
            shipmentRequestRepository,
            orderHeaderRepository,
            orderLineRepository,
            barIdempotencyHistoryRepository,
            notificationHistoryRepository,
            mockBarDeliveryClient,
            mockFugaDeliveryClient,
            interfaceHistoryService,
            businessHoursService,
            idFactory,
            timeProvider,
            sqsMessageGateway,
            Mappers.getMapper(ShipmentDispatchMapper.class),
            Mappers.getMapper(NotificationHistoryEntityMapper.class));
  }

  @Test
  void shouldDispatchPendingShipment() {
    LocalDateTime now = timeProvider.now();
    ShipmentRequestEntity shipmentRequest = new ShipmentRequestEntity();
    shipmentRequest.setShipmentRequestId("SHP-1");
    shipmentRequest.setOrderId("O-1");
    shipmentRequest.setCarrierCode(CarrierCode.BAR);
    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
    shipmentRequest.setNextRequestAfter(now.minusMinutes(1));

    OrderHeaderEntity orderHeader = new OrderHeaderEntity();
    orderHeader.setOrderId("O-1");
    orderHeader.setPartnerOrderId("FO202606170001");
    orderHeader.setOrderSource(OrderSource.FOO);
    orderHeader.setShippingPriorityClass(ShippingPriorityClass.PRIORITY);
    orderHeader.setPartnerPriorityLevel(8);
    orderHeader.setCarrierCode(CarrierCode.BAR);
    orderHeader.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
    orderHeader.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
    orderHeader.setDeliveryZipCode("1000001");
    orderHeader.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(orderHeader);
    orderHeader.setUpdatedAt(now);

    OrderLineEntity line = new OrderLineEntity();
    line.setOrderId("O-1");
    line.setOrderLineNo(1);
    line.setItemCode("ITM0000001");
    line.setItemName("FooOrderedItem");
    line.setQuantity(1);
    applyLineSnapshot(line);

    when(orderHeaderRepository.findById("O-1")).thenReturn(Optional.of(orderHeader));
    when(orderLineRepository.findByOrderIdOrderByOrderLineNo("O-1")).thenReturn(List.of(line));
    when(businessHoursService.isBarBusinessHours(org.mockito.ArgumentMatchers.any()))
        .thenReturn(true);
    when(idFactory.idempotencyKey("SHP-1")).thenReturn("IDEMP-0001");
    when(idFactory.notificationId()).thenReturn("NTF-0001", "NTF-0002");
    when(mockBarDeliveryClient.requestShipment(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new BarShipmentAcceptedResponse("BAR-1", "SHP-1", "ACCEPTED", now.toString(), false));

    // Directly invoke scheduled target with fresh repository answer to avoid clock coupling.
    when(shipmentRequestRepository
            .findByShipmentRequestStatusInAndNextRequestAfterLessThanEqualOrderByNextRequestAfterAsc(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(List.of(shipmentRequest));

    int dispatched = shipmentDispatchWorkerService.dispatchPendingShipments();

    assertThat(dispatched).isEqualTo(1);
    verify(notificationHistoryRepository, org.mockito.Mockito.atLeastOnce())
        .save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void shouldRequeueWhenBarRequestFails() {
    LocalDateTime now = timeProvider.now();
    ShipmentRequestEntity shipmentRequest = new ShipmentRequestEntity();
    shipmentRequest.setShipmentRequestId("SHP-2");
    shipmentRequest.setOrderId("O-2");
    shipmentRequest.setCarrierCode(CarrierCode.BAR);
    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
    shipmentRequest.setNextRequestAfter(now.minusMinutes(1));

    OrderHeaderEntity orderHeader = new OrderHeaderEntity();
    orderHeader.setOrderId("O-2");
    orderHeader.setPartnerOrderId("FO202606170002");
    orderHeader.setOrderSource(OrderSource.FOO);
    orderHeader.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    orderHeader.setPartnerPriorityLevel(1);
    orderHeader.setCarrierCode(CarrierCode.BAR);
    orderHeader.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
    orderHeader.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
    orderHeader.setDeliveryZipCode("1000001");
    orderHeader.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(orderHeader);
    orderHeader.setUpdatedAt(now);

    OrderLineEntity line = new OrderLineEntity();
    line.setOrderId("O-2");
    line.setOrderLineNo(1);
    line.setItemCode("ITM0000002");
    line.setItemName("FooOrderedItem");
    line.setQuantity(1);
    applyLineSnapshot(line);

    when(orderHeaderRepository.findById("O-2")).thenReturn(Optional.of(orderHeader));
    when(orderLineRepository.findByOrderIdOrderByOrderLineNo("O-2")).thenReturn(List.of(line));
    when(businessHoursService.isBarBusinessHours(any())).thenReturn(true);
    when(idFactory.idempotencyKey("SHP-2")).thenReturn("IDEMP-0002");
    when(mockBarDeliveryClient.requestShipment(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
        .thenThrow(new IllegalStateException("timeout"));
    when(shipmentRequestRepository
            .findByShipmentRequestStatusInAndNextRequestAfterLessThanEqualOrderByNextRequestAfterAsc(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(List.of(shipmentRequest));

    int dispatched = shipmentDispatchWorkerService.dispatchPendingShipments();

    assertThat(dispatched).isEqualTo(0);
    verify(shipmentRequestRepository, org.mockito.Mockito.atLeast(2))
        .save(shipmentRequestCaptor.capture());
    assertThat(
            shipmentRequestCaptor
                .getAllValues()
                .get(shipmentRequestCaptor.getAllValues().size() - 1)
                .getShipmentRequestStatus())
        .isEqualTo(ShipmentRequestStatus.PENDING);
    verify(interfaceHistoryService)
        .record(
            "IF-HOGE-BAR-001",
            jp.co.hoge.orderhub.common.domain.InterfaceDirection.OUTBOUND,
            jp.co.hoge.orderhub.common.domain.InterfaceStatus.FAILED,
            "SHP-2",
            "500",
            "bar request failed: timeout");
  }

  private void applyOrderSnapshot(OrderHeaderEntity orderHeader) {
    orderHeader.setDeliveryName("Test User");
    orderHeader.setDeliveryPhone("0312345678");
    orderHeader.setPackageCount(1);
    orderHeader.setPaymentMethod("PREPAID");
    orderHeader.setRequestedDeliveryDate(java.time.LocalDate.of(2026, 6, 18));
    orderHeader.setSpecialInstruction("Handle with care");
    orderHeader.setSubtotalExcludingTax(5000);
    orderHeader.setTaxAmount(500);
    orderHeader.setBillingAmount(5500);
  }

  private void applyLineSnapshot(OrderLineEntity line) {
    line.setUnitPriceExcludingTax(5000);
    line.setTaxRate(10);
    line.setLineSubtotalExcludingTax(5000);
    line.setLineTaxAmount(500);
    line.setUnitWeightGramSnapshot(1000);
    line.setTemperatureZoneSnapshot("AMBIENT");
    line.setSizeTypeSnapshot("NORMAL");
  }
}
