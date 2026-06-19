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
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.shippinggateway.ShippingGatewayApplication;
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
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderHeaderRepository orderHeaderRepository;

    @Autowired
    private ShipmentRequestRepository shipmentRequestRepository;

    @Autowired
    private DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

    @MockBean
    private CustomerRegistryClient customerRegistryClient;

    @MockBean
    private StockKeeperClient stockKeeperClient;

    @Test
    void shouldAcceptShipmentRequestApi() throws Exception {
        when(customerRegistryClient.findStatus("C00000000001"))
                .thenReturn(new CustomerStatusResponse("C00000000001", "ACTIVE", "GOLD"));
        when(stockKeeperClient.reserve(any()))
                .thenReturn(new StockReservationResponse("RSV-1", "RESERVED", List.of()));

        mockMvc.perform(post("/api/v1/shipment-requests")
                        .header("X-Client-System-Id", "FUGA-PORTAL")
                        .header("X-Request-Id", "REQ-1")
                        .header("X-Trace-Id", "TRACE-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                  "requestedDeliveryDate": "2026-06-18",
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

        mockMvc.perform(get("/api/v1/shipment-status/FO202606170010")
                        .header("X-Client-System-Id", "FOO-STATUS-CLIENT")
                        .header("X-Trace-Id", "TRACE-GET-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerOrderId").value("FO202606170010"))
                .andExpect(jsonPath("$.deliveryCompanyCode").value("BAR"))
                .andExpect(jsonPath("$.currentStatus").value("WAITING_BAR_REQUEST"));

        mockMvc.perform(get("/api/v1/shipment-status/FG202606170010")
                        .header("X-Client-System-Id", "FUGA-PORTAL")
                        .header("X-Trace-Id", "TRACE-GET-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerRequestId").value("FG202606170010"))
                .andExpect(jsonPath("$.partnerOrderId").value("FO202606170010"));

        mockMvc.perform(get("/api/v1/shipment-status/FG202606170010")
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

        mockMvc.perform(post("/api/v1/delivery-results/bar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "barShipmentId": "BARS202606170011",
                                  "orderId": "O202606170011",
                                  "partnerOrderId": "FO202606170011",
                                  "statusSeq": 1,
                                  "deliveryStatus": "PREPARING",
                                  "statusLabel": "Preparing",
                                  "eventOccurredAt": "2026-06-17T10:00:00",
                                  "locationCode": "TKY-CHY",
                                  "reasonCode": null,
                                  "reasonCategory": "ADDRESS_CORRECTED",
                                  "addressCorrected": true,
                                  "addressCorrectionLevel": "MINOR",
                                  "driverComment": null
                                }
                                """))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/v1/shipment-status/FO202606170011")
                        .header("X-Client-System-Id", "FOO-STATUS-CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestEvent.reasonCategory").value("ADDRESS_CORRECTED"))
                .andExpect(jsonPath("$.latestEvent.displayStatusName").value("住所補正対応中"));
    }
}
