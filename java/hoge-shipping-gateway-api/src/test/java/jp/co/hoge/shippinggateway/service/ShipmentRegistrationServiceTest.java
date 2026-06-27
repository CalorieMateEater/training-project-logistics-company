package jp.co.hoge.shippinggateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationAcceptedResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.CustomerCheckResultRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderLineRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.BusinessHoursService;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.shippinggateway.mapper.ShipmentGatewayEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentRegistrationServiceTest {
  @Mock private OrderHeaderRepository orderHeaderRepository;
  @Mock private OrderLineRepository orderLineRepository;
  @Mock private ShipmentRequestRepository shipmentRequestRepository;
  @Mock private CustomerCheckResultRepository customerCheckResultRepository;
  @Mock private StockReservationResultRepository stockReservationResultRepository;
  @Mock private CustomerRegistryClient customerRegistryClient;
  @Mock private StockKeeperClient stockKeeperClient;
  @Mock private InterfaceHistoryService interfaceHistoryService;
  @Mock private BusinessHoursService businessHoursService;
  @Mock private IdFactory idFactory;

  @Captor private ArgumentCaptor<ShipmentRequestEntity> shipmentRequestCaptor;

  private ShipmentRegistrationService shipmentRegistrationService;
  private TimeProvider timeProvider;

  @BeforeEach
  void setUp() {
    timeProvider = () -> LocalDateTime.of(2026, 6, 17, 9, 0, 0);
    shipmentRegistrationService =
        new ShipmentRegistrationService(
            orderHeaderRepository,
            orderLineRepository,
            shipmentRequestRepository,
            customerCheckResultRepository,
            stockReservationResultRepository,
            customerRegistryClient,
            stockKeeperClient,
            businessHoursService,
            idFactory,
            interfaceHistoryService,
            timeProvider,
            Mappers.getMapper(ShipmentGatewayEntityMapper.class));
  }

  @Test
  void shouldRegisterDirectShipment() {
    when(orderHeaderRepository.findByPartnerRequestId("FG202606170001"))
        .thenReturn(Optional.empty());
    when(orderHeaderRepository.findByPartnerOrderId("FGO202606170001"))
        .thenReturn(Optional.empty());
    when(customerRegistryClient.findStatus("C00000000001"))
        .thenReturn(new CustomerStatusResponse("C00000000001", "ACTIVE", "GOLD"));
    when(stockKeeperClient.reserve(any()))
        .thenReturn(
            new StockReservationResponse(
                "RSV-1",
                "RESERVED",
                java.util.List.of(
                    new StockReservationResponse.ReservationResult(
                        "ITM0000001",
                        1,
                        1,
                        "WH-TYO-01",
                        "RESERVED",
                        100,
                        1,
                        99,
                        "StandardItemA",
                        1000,
                        "AMBIENT",
                        "NORMAL"))));
    when(idFactory.orderId()).thenReturn("O-1");
    when(idFactory.shipmentRequestId()).thenReturn("SHP-1");

    LocalDateTime releaseAt = timeProvider.now().plusDays(1);
    ShipmentRegistrationAcceptedResponse response =
        shipmentRegistrationService.register(
            new ShipmentRegistrationRequest(
                "FG202606170001",
                "FGO202606170001",
                "C00000000001",
                "ITM0000001",
                1,
                "STANDARD",
                "RESERVED",
                new ShipmentRegistrationRequest.DeliveryConstraint("AMBIENT", "EVENING"),
                "1000001",
                "Tokyo",
                "Test User",
                "0312345678",
                1,
                "PREPAID",
                5000,
                10,
                LocalDate.of(2026, 6, 18),
                "Handle with care",
                releaseAt),
            "HOGE-DIRECT-PORTAL",
            "REQ-1",
            "TRACE-1");

    assertThat(response.registrationStatus()).isEqualTo("ACCEPTED");
    assertThat(response.currentStatus()).isEqualTo("WAITING_SHIPPING_RELEASE");
    org.mockito.Mockito.verify(shipmentRequestRepository).save(shipmentRequestCaptor.capture());
    assertThat(shipmentRequestCaptor.getValue().getShipmentRequestStatus())
        .isEqualTo(ShipmentRequestStatus.PENDING);
    assertThat(shipmentRequestCaptor.getValue().getCarrierCode()).isEqualTo(CarrierCode.BAR);
  }
}
