package jp.co.hoge.shippinggateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.dto.ShipmentStatusResponse;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ShipmentStatusQueryServiceTest {
  @Mock private OrderHeaderRepository orderHeaderRepository;
  @Mock private ShipmentRequestRepository shipmentRequestRepository;
  @Mock private DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;
  @Mock private InterfaceHistoryService interfaceHistoryService;

  private ShipmentStatusQueryService shipmentStatusQueryService;

  @BeforeEach
  void setUp() {
    shipmentStatusQueryService =
        new ShipmentStatusQueryService(
            orderHeaderRepository,
            shipmentRequestRepository,
            deliveryStatusCurrentRepository,
            interfaceHistoryService);
  }

  @Test
  void shouldAllowDirectPortalLookupByPartnerRequestId() {
    OrderHeaderEntity orderHeader = createOrderHeader();
    ShipmentRequestEntity shipmentRequest = createShipmentRequest(orderHeader.getOrderId());

    when(orderHeaderRepository.findByPartnerOrderId("FG202606170010")).thenReturn(Optional.empty());
    when(orderHeaderRepository.findByPartnerRequestId("FG202606170010"))
        .thenReturn(Optional.of(orderHeader));
    when(shipmentRequestRepository.findByOrderId(orderHeader.getOrderId()))
        .thenReturn(Optional.of(shipmentRequest));
    when(deliveryStatusCurrentRepository.findById(orderHeader.getOrderId()))
        .thenReturn(Optional.empty());

    ShipmentStatusResponse response =
        shipmentStatusQueryService.findStatus("FG202606170010", "HOGE-DIRECT-PORTAL", "TRACE-1");

    assertThat(response.partnerRequestId()).isEqualTo("FG202606170010");
    verify(orderHeaderRepository).findByPartnerRequestId("FG202606170010");
  }

  @Test
  void shouldRejectFooLookupByPartnerRequestId() {
    when(orderHeaderRepository.findByPartnerOrderId("FG202606170010")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                shipmentStatusQueryService.findStatus(
                    "FG202606170010", "FOO-STATUS-CLIENT", "TRACE-2"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception -> {
              ResponseStatusException responseStatusException = (ResponseStatusException) exception;
              assertThat(responseStatusException.getStatusCode().value()).isEqualTo(404);
            });

    verify(orderHeaderRepository, never()).findByPartnerRequestId("FG202606170010");
  }

  private OrderHeaderEntity createOrderHeader() {
    OrderHeaderEntity order = new OrderHeaderEntity();
    order.setOrderId("O202606170010");
    order.setPartnerOrderId("FO202606170010");
    order.setPartnerRequestId("FG202606170010");
    order.setOrderSource(OrderSource.HOGE);
    order.setPartnerPriorityLevel(0);
    order.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    order.setCustomerId("C00000000001");
    order.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
    order.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
    order.setCarrierCode(CarrierCode.BAR);
    order.setDeliveryZipCode("1000001");
    order.setDeliveryAddress("Tokyo");
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    return order;
  }

  private ShipmentRequestEntity createShipmentRequest(String orderId) {
    ShipmentRequestEntity shipmentRequest = new ShipmentRequestEntity();
    shipmentRequest.setShipmentRequestId("SHP-1");
    shipmentRequest.setOrderId(orderId);
    shipmentRequest.setCarrierCode(CarrierCode.BAR);
    shipmentRequest.setOrderSource(OrderSource.HOGE);
    shipmentRequest.setPartnerPriorityLevel(0);
    shipmentRequest.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
    shipmentRequest.setQueueEnqueuedAt(LocalDateTime.now());
    shipmentRequest.setNextRequestAfter(LocalDateTime.now());
    return shipmentRequest;
  }
}
