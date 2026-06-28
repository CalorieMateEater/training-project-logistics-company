package jp.co.hoge.shippinggateway.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationAcceptedResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.integration.SqsMessageGateway;
import jp.co.hoge.orderhub.common.persistence.repository.CustomerCheckResultRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderLineRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.BusinessHoursService;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.shippinggateway.mapper.ShipmentGatewayEntityMapper;
import jp.co.hoge.shippinggateway.mapper.model.ShipmentRegistrationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Hoge社業務部門の直受注登録を処理するサービス。 関連処理機能ID: PGD-004
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentRegistrationService {

  /** レスポンス日時フォーマッタ。 */
  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /** 配送先電話番号パターン。 */
  private static final Pattern PHONE = Pattern.compile("^\\d{10,11}$");

  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** 注文明細リポジトリ。 */
  private final OrderLineRepository orderLineRepository;

  /** 出荷依頼リポジトリ。 */
  private final ShipmentRequestRepository shipmentRequestRepository;

  /** 顧客確認結果リポジトリ。 */
  private final CustomerCheckResultRepository customerCheckResultRepository;

  /** 在庫引当結果リポジトリ。 */
  private final StockReservationResultRepository stockReservationResultRepository;

  /** 顧客マスタ管理クライアント。 */
  private final CustomerRegistryClient customerRegistryClient;

  /** 在庫引当クライアント。 */
  private final StockKeeperClient stockKeeperClient;

  /** 配送会社営業時間判定サービス。 */
  private final BusinessHoursService businessHoursService;

  /** ID 採番サービス。 */
  private final IdFactory idFactory;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /** SQS送信ゲートウェイ。 */
  private final SqsMessageGateway sqsMessageGateway;

  /** 永続化用エンティティマッパー。 */
  private final ShipmentGatewayEntityMapper shipmentGatewayEntityMapper;

  /**
   * Hoge社業務部門の直受注を受け付け、関連データを登録する。
   *
   * @param request 出荷依頼要求
   * @param clientSystemId 呼出元システム ID
   * @param requestId リクエスト ID
   * @param traceId トレース ID
   * @return 出荷依頼受付応答
   */
  @Transactional
  public ShipmentRegistrationAcceptedResponse register(
      ShipmentRegistrationRequest request,
      String clientSystemId,
      String requestId,
      String traceId) {
    ClientSystemId.requireHogeDirectPortal(clientSystemId);
    validateShipmentMode(request);

    if (orderHeaderRepository.findByPartnerRequestId(request.partnerRequestId()).isPresent()) {
      interfaceHistoryService.record(
          "IF-HOGE-OPS-002",
          InterfaceDirection.INBOUND,
          InterfaceStatus.FAILED,
          request.partnerRequestId(),
          traceId,
          "409",
          "partner_request_id duplicated");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "partner_request_id duplicated");
    }

    if (orderHeaderRepository.findByPartnerOrderId(request.partnerOrderId()).isPresent()) {
      interfaceHistoryService.record(
          "IF-HOGE-OPS-002",
          InterfaceDirection.INBOUND,
          InterfaceStatus.FAILED,
          request.partnerRequestId(),
          traceId,
          "409",
          "partner_order_id duplicated");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "partner_order_id duplicated");
    }

    String orderId = idFactory.orderId();
    String shipmentRequestId = idFactory.shipmentRequestId();
    CarrierCode carrierCode = determineCarrier(request);
    CustomerStatusResponse customerStatus = customerRegistryClient.findStatus(request.customerId());
    if (!"ACTIVE".equals(customerStatus.status())) {
      interfaceHistoryService.record(
          "IF-HOGE-OPS-002",
          InterfaceDirection.INBOUND,
          InterfaceStatus.FAILED,
          request.partnerRequestId(),
          traceId,
          "422",
          "customer is not active");
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "customer is not active");
    }

    StockReservationResponse reservationResponse =
        stockKeeperClient.reserve(
            new StockReservationRequest(
                orderId,
                List.of(
                    new StockReservationRequest.ReservationItem(
                        request.itemCode(), request.quantity()))));

    LocalDateTime now = timeProvider.now();
    boolean waitingRelease = isWaitingRelease(request.shippingReleaseAt(), now);
    OrderStatus orderStatus =
        waitingRelease
            ? OrderStatus.WAITING_SHIPPING_RELEASE
            : carrierCode == CarrierCode.FUGA
                ? OrderStatus.WAITING_FUGA_REQUEST
                : OrderStatus.WAITING_BAR_REQUEST;
    ShipmentRequestStatus shipmentRequestStatus =
        waitingRelease
            ? ShipmentRequestStatus.PENDING
            : carrierCode == CarrierCode.BAR && businessHoursService.isBarBusinessHours(now)
                ? ShipmentRequestStatus.PENDING
                : carrierCode == CarrierCode.BAR
                    ? ShipmentRequestStatus.WAITING_BUSINESS_HOURS
                    : ShipmentRequestStatus.PENDING;

    ShipmentRegistrationContext context =
        new ShipmentRegistrationContext(
            orderId,
            shipmentRequestId,
            request,
            customerStatus,
            reservationResponse,
            carrierCode,
            orderStatus,
            shipmentRequestStatus,
            waitingRelease
                ? request.shippingReleaseAt()
                : carrierCode == CarrierCode.BAR
                    ? businessHoursService.nextBarBusinessTime(now)
                    : now,
            determineRoutingRuleId(request, carrierCode),
            now);

    orderHeaderRepository.save(shipmentGatewayEntityMapper.toOrderHeader(context));
    orderLineRepository.save(shipmentGatewayEntityMapper.toOrderLine(context));
    customerCheckResultRepository.save(shipmentGatewayEntityMapper.toCustomerCheckResult(context));
    stockReservationResultRepository.save(
        shipmentGatewayEntityMapper.toStockReservationResult(context));
    var shipmentRequest = shipmentGatewayEntityMapper.toShipmentRequest(context);
    shipmentRequestRepository.save(shipmentRequest);
    sqsMessageGateway.send(
        shipmentQueueName(carrierCode),
        orderId,
        shipmentRequest.getShipmentRequestId(),
        shipmentRequest.getShipmentRequestId());

    interfaceHistoryService.record(
        "IF-HOGE-OPS-002",
        InterfaceDirection.INBOUND,
        InterfaceStatus.ACCEPTED,
        requestId != null ? requestId : request.partnerRequestId(),
        traceId,
        "202",
        "shipment accepted");

    return new ShipmentRegistrationAcceptedResponse(
        orderId, request.partnerRequestId(), "ACCEPTED", orderStatus.name(), ISO.format(now));
  }

  private void validateShipmentMode(ShipmentRegistrationRequest request) {
    if (request.shipmentMode() != null
        && !request.shipmentMode().isBlank()
        && !java.util.Set.of("IMMEDIATE", "RESERVED").contains(request.shipmentMode())) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "shipment_mode invalid");
    }
    if ("RESERVED".equals(request.shipmentMode()) && request.shippingReleaseAt() == null) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "shipping_release_at required");
    }
    if ("IMMEDIATE".equals(request.shipmentMode()) && request.shippingReleaseAt() != null) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "shipping_release_at not allowed");
    }
    if (request.shippingReleaseAt() != null
        && request.shippingReleaseAt().isBefore(timeProvider.now())) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "shipping_release_at invalid");
    }
    if (request.quantity() < 1 || request.quantity() > 999) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "quantity invalid");
    }
    if (request.deliveryName() == null
        || request.deliveryName().isBlank()
        || request.deliveryName().length() > 60) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "delivery_name invalid");
    }
    if (request.deliveryPhone() == null || !PHONE.matcher(request.deliveryPhone()).matches()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "delivery_phone invalid");
    }
    if (request.packageCount() < 1
        || request.packageCount() > 999
        || request.packageCount() > request.quantity()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "package_count invalid");
    }
    if (!Set.of("PREPAID", "COD").contains(request.paymentMethod())) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "payment_method invalid");
    }
    if (request.unitPriceExcludingTax() < 1 || request.unitPriceExcludingTax() > 9_999_999) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "unit_price_excluding_tax invalid");
    }
    if (request.taxRate() != 8 && request.taxRate() != 10) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "tax_rate invalid");
    }
    int subtotal = request.unitPriceExcludingTax() * request.quantity();
    int taxAmount = subtotal * request.taxRate() / 100;
    int billingAmount = subtotal + taxAmount;
    if (taxAmount > 999_999 || billingAmount > 9_999_999) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "billing_amount out of range");
    }
    if (request.requestedDeliveryDate() != null
        && request.requestedDeliveryDate().isBefore(timeProvider.now().toLocalDate())) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "requested_delivery_date invalid");
    }
    if (request.specialInstruction() != null && request.specialInstruction().length() > 200) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "special_instruction invalid");
    }
  }

  private boolean isWaitingRelease(LocalDateTime shippingReleaseAt, LocalDateTime now) {
    return shippingReleaseAt != null && shippingReleaseAt.isAfter(now);
  }

  private CarrierCode determineCarrier(ShipmentRegistrationRequest request) {
    String temperatureZone =
        request.deliveryConstraint().temperatureZone().toUpperCase(Locale.ROOT);
    String shipmentPreference = request.shipmentPreference().toUpperCase(Locale.ROOT);
    if (!"AMBIENT".equals(temperatureZone)
        || "LARGE".equals(shipmentPreference)
        || "REMOTE".equals(shipmentPreference)
        || "SPECIAL".equals(shipmentPreference)) {
      return CarrierCode.FUGA;
    }
    return CarrierCode.BAR;
  }

  private String determineRoutingRuleId(
      ShipmentRegistrationRequest request, CarrierCode carrierCode) {
    if (carrierCode == CarrierCode.FUGA) {
      return "RULE-FUGA-SPECIAL";
    }
    return "RULE-BAR-DEFAULT";
  }

  private String shipmentQueueName(CarrierCode carrierCode) {
    return carrierCode == CarrierCode.FUGA
        ? "fuga-shipment-request-queue.fifo"
        : "bar-shipment-request-queue.fifo";
  }
}
