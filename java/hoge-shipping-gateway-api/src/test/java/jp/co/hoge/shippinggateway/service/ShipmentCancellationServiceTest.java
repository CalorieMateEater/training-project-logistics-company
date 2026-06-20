package jp.co.hoge.shippinggateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.dto.ShipmentCancelResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentCancellationServiceTest {
  @Mock private OrderHeaderRepository orderHeaderRepository;
  @Mock private ShipmentRequestRepository shipmentRequestRepository;
  @Mock private StockReservationResultRepository stockReservationResultRepository;
  @Mock private StockKeeperClient stockKeeperClient;
  @Mock private InterfaceHistoryService interfaceHistoryService;

  private ShipmentCancellationService shipmentCancellationService;

  @BeforeEach
  void setUp() {
    TimeProvider timeProvider = () -> LocalDateTime.of(2026, 6, 20, 11, 30, 0);
    shipmentCancellationService =
        new ShipmentCancellationService(
            orderHeaderRepository,
            shipmentRequestRepository,
            stockReservationResultRepository,
            stockKeeperClient,
            interfaceHistoryService,
            timeProvider);
  }

  @Test
  void shouldCancelWaitingOrderAndReleaseStock() {
    OrderHeaderEntity orderHeader = new OrderHeaderEntity();
    orderHeader.setOrderId("O-1");
    orderHeader.setOrderSource(OrderSource.FOO);
    orderHeader.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
    orderHeader.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
    orderHeader.setCarrierCode(CarrierCode.BAR);
    orderHeader.setShippingPriorityClass(ShippingPriorityClass.NORMAL);

    ShipmentRequestEntity shipmentRequest = new ShipmentRequestEntity();
    shipmentRequest.setShipmentRequestId("SHP-1");
    shipmentRequest.setOrderId("O-1");
    shipmentRequest.setCarrierCode(CarrierCode.BAR);
    shipmentRequest.setOrderSource(OrderSource.FOO);
    shipmentRequest.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);

    StockReservationResultEntity reservationResult = new StockReservationResultEntity();
    reservationResult.setOrderId("O-1");
    reservationResult.setOrderLineNo(1);
    reservationResult.setItemCode("ITM0000001");
    reservationResult.setReservationId("RSV-1");
    reservationResult.setReservationStatus("RESERVED");
    reservationResult.setReservedQuantity(2);

    when(orderHeaderRepository.findById("O-1")).thenReturn(Optional.of(orderHeader));
    when(shipmentRequestRepository.findByOrderId("O-1")).thenReturn(Optional.of(shipmentRequest));
    when(stockReservationResultRepository.findByOrderIdOrderByOrderLineNo("O-1"))
        .thenReturn(List.of(reservationResult));
    when(stockKeeperClient.release("RSV-1"))
        .thenReturn(
            new StockReservationOperationResponse(
                "RSV-1",
                "RELEASED",
                List.of(
                    new StockReservationOperationResponse.OperationResult(
                        "ITM0000001", "WH-TYO-01", 2, "RELEASED", 100, 0, 100))));

    ShipmentCancelResponse response =
        shipmentCancellationService.cancel(
            "O-1", "HOGE-OPS-PORTAL", "TRACE-CANCEL-1", "ops cancel");

    assertThat(response.cancelStatus()).isEqualTo("CANCELLED");
    assertThat(response.currentStatus()).isEqualTo("CANCELLED");
    assertThat(orderHeader.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(shipmentRequest.getShipmentRequestStatus())
        .isEqualTo(ShipmentRequestStatus.CANCELLED);
    assertThat(reservationResult.getReservationStatus()).isEqualTo("RELEASED");
    assertThat(reservationResult.getReleasedQuantity()).isEqualTo(2);
  }
}
