package jp.co.hoge.shippinggateway.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.shippinggateway.ShippingGatewayApplication;
import jp.co.hoge.shippinggateway.service.BarDeliveryResultService;
import jp.co.hoge.shippinggateway.service.CustomerRegistryClient;
import jp.co.hoge.shippinggateway.service.StockKeeperClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ShippingGatewayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShipmentGatewayApiIT {
  @Autowired private MockMvc mockMvc;

  @Autowired private OrderHeaderRepository orderHeaderRepository;

  @Autowired private ShipmentRequestRepository shipmentRequestRepository;

  @Autowired private DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

  @Autowired private BarDeliveryResultService barDeliveryResultService;

  @MockBean private CustomerRegistryClient customerRegistryClient;

  @MockBean private StockKeeperClient stockKeeperClient;

  @Test
  void shouldAcceptShipmentRequestApi() throws Exception {
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

    mockMvc
        .perform(
            post("/api/v1/shipment-requests")
                .header("X-Client-System-Id", "HOGE-DIRECT-PORTAL")
                .header("X-Request-Id", "REQ-1")
                .header("X-Trace-Id", "TRACE-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "partnerRequestId": "FG202606170001",
                                  "partnerOrderId": "FGO202606170001",
                                  "customerId": "C00000000001",
                                  "itemCode": "ITM0000001",
                                  "quantity": 1,
                                  "shipmentPreference": "STANDARD",
                                  "shipmentMode": "RESERVED",
                                  "deliveryConstraint": {
                                    "temperatureZone": "AMBIENT",
                                    "timeSlot": "EVENING"
                                  },
                                  "deliveryZipCode": "1000001",
                                  "deliveryAddress": "Tokyo",
                                  "deliveryName": "Test User",
                                  "deliveryPhone": "0312345678",
                                  "packageCount": 1,
                                  "paymentMethod": "PREPAID",
                                  "unitPriceExcludingTax": 5000,
                                  "taxRate": 10,
                                  "requestedDeliveryDate": "2099-06-18",
                                  "specialInstruction": "Handle with care",
                                  "shippingReleaseAt": "2099-06-18T09:00:00"
                                }
                                """))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.registrationStatus").value("ACCEPTED"))
        .andExpect(jsonPath("$.currentStatus").value("WAITING_SHIPPING_RELEASE"));
  }

  @Test
  void shouldReturnShipmentStatusApi() throws Exception {
    OrderHeaderEntity order = new OrderHeaderEntity();
    order.setOrderId("O202606170010");
    order.setPartnerOrderId("FO202606170010");
    order.setPartnerRequestId("FG202606170010");
    order.setOrderSource(OrderSource.FOO);
    order.setPartnerPriorityLevel(0);
    order.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    order.setCustomerId("C00000000001");
    order.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
    order.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
    order.setCarrierCode(CarrierCode.BAR);
    order.setDeliveryZipCode("1000001");
    order.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(order);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    orderHeaderRepository.save(order);

    ShipmentRequestEntity request = new ShipmentRequestEntity();
    request.setShipmentRequestId("SHP-1");
    request.setOrderId(order.getOrderId());
    request.setCarrierCode(CarrierCode.BAR);
    request.setOrderSource(OrderSource.FOO);
    request.setPartnerPriorityLevel(0);
    request.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    request.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
    request.setQueueEnqueuedAt(LocalDateTime.now());
    request.setNextRequestAfter(LocalDateTime.now());
    shipmentRequestRepository.save(request);

    DeliveryStatusCurrentEntity current = new DeliveryStatusCurrentEntity();
    current.setOrderId(order.getOrderId());
    current.setLatestStatusCode("WAITING_BAR_REQUEST");
    current.setLatestStatusName("WAITING_BAR_REQUEST");
    current.setLatestStatusSeq(0);
    current.setLatestStatusAt(LocalDateTime.now());
    current.setLastReceivedAt(LocalDateTime.now());
    deliveryStatusCurrentRepository.save(current);

    mockMvc
        .perform(
            get("/api/v1/shipment-status/FO202606170010")
                .header("X-Client-System-Id", "FOO-STATUS-CLIENT")
                .header("X-Trace-Id", "TRACE-GET-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.partnerOrderId").value("FO202606170010"))
        .andExpect(jsonPath("$.deliveryCompanyCode").value("BAR"))
        .andExpect(jsonPath("$.currentStatus").value("WAITING_BAR_REQUEST"));

    mockMvc
        .perform(
            get("/api/v1/shipment-status/FG202606170010")
                .header("X-Client-System-Id", "HOGE-DIRECT-PORTAL")
                .header("X-Trace-Id", "TRACE-GET-2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.partnerRequestId").value("FG202606170010"))
        .andExpect(jsonPath("$.partnerOrderId").value("FO202606170010"));

    mockMvc
        .perform(
            get("/api/v1/shipment-status/FG202606170010")
                .header("X-Client-System-Id", "FOO-STATUS-CLIENT")
                .header("X-Trace-Id", "TRACE-GET-3"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldAcceptBarDeliveryResultApi() throws Exception {
    OrderHeaderEntity order = new OrderHeaderEntity();
    order.setOrderId("O202606170011");
    order.setPartnerOrderId("FO202606170011");
    order.setPartnerRequestId("FG202606170011");
    order.setOrderSource(OrderSource.FOO);
    order.setPartnerPriorityLevel(0);
    order.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    order.setCustomerId("C00000000001");
    order.setOrderStatus(OrderStatus.BAR_ACCEPTED);
    order.setShipmentStatus(OrderStatus.BAR_ACCEPTED);
    order.setCarrierCode(CarrierCode.BAR);
    order.setDeliveryZipCode("1000001");
    order.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(order);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    orderHeaderRepository.save(order);

    ShipmentRequestEntity request = new ShipmentRequestEntity();
    request.setShipmentRequestId("SHP-11");
    request.setOrderId(order.getOrderId());
    request.setCarrierCode(CarrierCode.BAR);
    request.setOrderSource(OrderSource.FOO);
    request.setPartnerPriorityLevel(0);
    request.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    request.setShipmentRequestStatus(ShipmentRequestStatus.ACCEPTED);
    request.setQueueEnqueuedAt(LocalDateTime.now());
    request.setNextRequestAfter(LocalDateTime.now());
    shipmentRequestRepository.save(request);

    mockMvc
        .perform(
            post("/api/v1/delivery-results/bar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "bar_shipment_id": "BARS202606170011",
                                  "order_id": "O202606170011",
                                  "partner_order_id": "FO202606170011",
                                  "status_seq": 1,
                                  "delivery_status": "PREPARING",
                                  "status_label": "Preparing",
                                  "event_occurred_at": "2026-06-17T10:00:00",
                                  "location_code": "TKY-CHY",
                                  "reason_code": null,
                                  "reason_category": "ADDRESS_CORRECTED",
                                  "address_corrected": true,
                                  "address_correction_level": "MINOR",
                                  "driver_comment": null
                                }
                                """))
        .andExpect(status().isAccepted());

    barDeliveryResultService.processPendingReflectionRequests();

    mockMvc
        .perform(
            get("/api/v1/shipment-status/FO202606170011")
                .header("X-Client-System-Id", "FOO-STATUS-CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latestEvent.reasonCategory").value("ADDRESS_CORRECTED"))
        .andExpect(jsonPath("$.latestEvent.displayStatusName").value("住所補正対応中"));
  }

  @Test
  void shouldAcceptFugaDeliveryResultApi() throws Exception {
    OrderHeaderEntity order = new OrderHeaderEntity();
    order.setOrderId("O202606170013");
    order.setPartnerOrderId("HO202606170013");
    order.setPartnerRequestId("FG202606170013");
    order.setOrderSource(OrderSource.HOGE);
    order.setPartnerPriorityLevel(0);
    order.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    order.setCustomerId("C00000000001");
    order.setOrderStatus(OrderStatus.FUGA_ACCEPTED);
    order.setShipmentStatus(OrderStatus.FUGA_ACCEPTED);
    order.setCarrierCode(CarrierCode.FUGA);
    order.setDeliveryZipCode("1000001");
    order.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(order);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    orderHeaderRepository.save(order);

    ShipmentRequestEntity request = new ShipmentRequestEntity();
    request.setShipmentRequestId("SHP-13");
    request.setOrderId(order.getOrderId());
    request.setCarrierCode(CarrierCode.FUGA);
    request.setOrderSource(OrderSource.HOGE);
    request.setPartnerPriorityLevel(0);
    request.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    request.setShipmentRequestStatus(ShipmentRequestStatus.ACCEPTED);
    request.setQueueEnqueuedAt(LocalDateTime.now());
    request.setNextRequestAfter(LocalDateTime.now());
    shipmentRequestRepository.save(request);

    mockMvc
        .perform(
            post("/api/v1/delivery-results/fuga")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "fuga_shipment_id": "FGS202606170013",
                                  "order_id": "O202606170013",
                                  "partner_order_id": "HO202606170013",
                                  "status_seq": 3,
                                  "delivery_status": "IN_TRANSIT",
                                  "status_label": "配送中",
                                  "event_occurred_at": "2026-06-17T12:00:00",
                                  "temperature_zone": "COOL",
                                  "size_type": "LARGE",
                                  "reason_code": null,
                                  "reason_category": null
                                }
                                """))
        .andExpect(status().isAccepted());

    barDeliveryResultService.processPendingReflectionRequests();

    mockMvc
        .perform(
            get("/api/v1/shipment-status/FG202606170013")
                .header("X-Client-System-Id", "HOGE-DIRECT-PORTAL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deliveryCompanyCode").value("FUGA"))
        .andExpect(jsonPath("$.currentStatus").value("IN_TRANSIT"));
  }

  @Test
  void shouldRejectOldFugaDeliveryResultApi() throws Exception {
    OrderHeaderEntity order = new OrderHeaderEntity();
    order.setOrderId("O202606170014");
    order.setPartnerOrderId("HO202606170014");
    order.setPartnerRequestId("FG202606170014");
    order.setOrderSource(OrderSource.HOGE);
    order.setPartnerPriorityLevel(0);
    order.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    order.setCustomerId("C00000000001");
    order.setOrderStatus(OrderStatus.IN_DELIVERY_FLOW);
    order.setShipmentStatus(OrderStatus.IN_DELIVERY_FLOW);
    order.setCarrierCode(CarrierCode.FUGA);
    order.setDeliveryZipCode("1000001");
    order.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(order);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    orderHeaderRepository.save(order);

    DeliveryStatusCurrentEntity current = new DeliveryStatusCurrentEntity();
    current.setOrderId(order.getOrderId());
    current.setLatestStatusCode("IN_TRANSIT");
    current.setLatestStatusName("配送中");
    current.setLatestDisplayStatusName("配送中");
    current.setLatestStatusSeq(3);
    current.setLatestStatusAt(LocalDateTime.now());
    current.setLastReceivedAt(LocalDateTime.now());
    deliveryStatusCurrentRepository.save(current);

    mockMvc
        .perform(
            post("/api/v1/delivery-results/fuga")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "fuga_shipment_id": "FGS202606170014",
                                  "order_id": "O202606170014",
                                  "partner_order_id": "HO202606170014",
                                  "status_seq": 2,
                                  "delivery_status": "PREPARING",
                                  "status_label": "配送準備中",
                                  "event_occurred_at": "2026-06-17T11:00:00",
                                  "temperature_zone": "COOL",
                                  "size_type": "LARGE",
                                  "reason_code": null,
                                  "reason_category": null
                                }
                                """))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldCancelWaitingShipmentApi() throws Exception {
    OrderHeaderEntity order = new OrderHeaderEntity();
    order.setOrderId("O202606170012");
    order.setPartnerOrderId("FO202606170012");
    order.setPartnerRequestId("FG202606170012");
    order.setOrderSource(OrderSource.FOO);
    order.setPartnerPriorityLevel(0);
    order.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    order.setCustomerId("C00000000001");
    order.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
    order.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
    order.setCarrierCode(CarrierCode.BAR);
    order.setDeliveryZipCode("1000001");
    order.setDeliveryAddress("Tokyo");
    applyOrderSnapshot(order);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    orderHeaderRepository.save(order);

    ShipmentRequestEntity request = new ShipmentRequestEntity();
    request.setShipmentRequestId("SHP-12");
    request.setOrderId(order.getOrderId());
    request.setCarrierCode(CarrierCode.BAR);
    request.setOrderSource(OrderSource.FOO);
    request.setPartnerPriorityLevel(0);
    request.setShippingPriorityClass(ShippingPriorityClass.NORMAL);
    request.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
    request.setQueueEnqueuedAt(LocalDateTime.now());
    request.setNextRequestAfter(LocalDateTime.now());
    shipmentRequestRepository.save(request);

    when(stockKeeperClient.release(any()))
        .thenReturn(
            new StockReservationOperationResponse(
                "RSV-12",
                "RELEASED",
                List.of(
                    new StockReservationOperationResponse.OperationResult(
                        "ITM0000001", "WH-TYO-01", 2, "RELEASED", 100, 0, 100))));

    mockMvc
        .perform(
            post("/api/v1/internal/orders/O202606170012/cancel")
                .header("X-Client-System-Id", "HOGE-OPS-PORTAL")
                .header("X-Trace-Id", "TRACE-CANCEL-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "cancelReason": "manual operation cancel"
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cancelStatus").value("CANCELLED"))
        .andExpect(jsonPath("$.currentStatus").value("CANCELLED"));
  }

  private void applyOrderSnapshot(OrderHeaderEntity order) {
    order.setDeliveryName("Test User");
    order.setDeliveryPhone("0312345678");
    order.setPackageCount(1);
    order.setPaymentMethod("PREPAID");
    order.setRequestedDeliveryDate(java.time.LocalDate.of(2026, 6, 18));
    order.setSpecialInstruction("Handle with care");
    order.setSubtotalExcludingTax(5000);
    order.setTaxAmount(500);
    order.setBillingAmount(5500);
  }
}
