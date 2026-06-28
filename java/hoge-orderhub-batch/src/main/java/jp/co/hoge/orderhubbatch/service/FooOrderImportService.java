package jp.co.hoge.orderhubbatch.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.OrderType;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.integration.SqsMessageGateway;
import jp.co.hoge.orderhub.common.logging.MdcUtils;
import jp.co.hoge.orderhub.common.persistence.entity.CustomerCheckResultEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.orderhub.common.persistence.repository.CustomerCheckResultRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderLineRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.BusinessHoursService;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.PriorityResolver;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubbatch.mapper.FooOrderImportEntityMapper;
import jp.co.hoge.orderhubbatch.mapper.model.FooOrderImportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Foo社注文ファイル取込を処理するサービス。 関連処理機能ID: PGD-001
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FooOrderImportService {
  /** 14桁英数字パターン。 */
  private static final Pattern ALNUM_14 = Pattern.compile("^[A-Za-z0-9]{14}$");

  /** 12桁英数字パターン。 */
  private static final Pattern ALNUM_12 = Pattern.compile("^[A-Za-z0-9]{12}$");

  /** 10桁英数字パターン。 */
  private static final Pattern ALNUM_10 = Pattern.compile("^[A-Za-z0-9]{10}$");

  /** 7桁郵便番号パターン。 */
  private static final Pattern ZIP_7 = Pattern.compile("^\\d{7}$");

  /** 電話番号パターン。 */
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

  /** 通知履歴リポジトリ。 */
  private final NotificationHistoryRepository notificationHistoryRepository;

  /** 顧客マスタ管理クライアント。 */
  private final CustomerRegistryClient customerRegistryClient;

  /** 在庫引当クライアント。 */
  private final StockKeeperClient stockKeeperClient;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /** ID 採番サービス。 */
  private final IdFactory idFactory;

  /** 優先度判定サービス。 */
  private final PriorityResolver priorityResolver;

  /** 配送会社営業時間判定サービス。 */
  private final BusinessHoursService businessHoursService;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /** SQS送信ゲートウェイ。 */
  private final SqsMessageGateway sqsMessageGateway;

  /** 取込エンティティマッパー。 */
  private final FooOrderImportEntityMapper fooOrderImportEntityMapper;

  /**
   * Foo社注文ファイルを読み込み、各レコードを登録する。
   *
   * @param path 取込対象ファイルパス
   * @return 取込件数
   */
  @Transactional
  public int importFile(String path) {
    log.info("APP_BATCH_START function=fooOrderImport path={}", path);
    try {
      List<String> lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
      int imported = 0;
      for (String line : lines) {
        if (!line.isBlank()) {
          importRecord(line);
          imported++;
        }
      }
      log.info("APP_BATCH_FINISH function=fooOrderImport path={} imported={}", path, imported);
      return imported;
    } catch (IOException exception) {
      log.error(
          "MONITORING_BATCH_ERROR function=fooOrderImport path={} message={}",
          path,
          exception.getMessage(),
          exception);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "file read failed: " + exception.getMessage());
    }
  }

  /**
   * 注文ファイル1レコードを検証して業務データへ登録する。
   *
   * @param csvLine 注文 CSV 1レコード
   */
  protected void importRecord(String csvLine) {
    String[] columns = csvLine.split(",", -1);
    if (columns.length != 19) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid column count");
    }

    if (!"D".equals(columns[0])) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "record_type must be D");
    }

    String partnerOrderId = columns[1];
    String orderTypeCode = columns[2];
    int priorityLevel = parseInt(columns[3], "priority_level");
    String customerId = columns[4];
    String itemCode = columns[5];
    int quantity = parseInt(columns[6], "quantity");
    int unitPriceExcludingTax = parseInt(columns[7], "unit_price_excluding_tax");
    int taxRate = parseInt(columns[8], "tax_rate");
    LocalDateTime orderDatetime = parseDateTime(columns[9], "order_datetime");
    String zipCode = columns[10];
    String address = columns[11];
    String deliveryName = columns[12];
    String deliveryPhone = columns[13];
    int packageCount = parseInt(columns[14], "package_count");
    String paymentMethod = columns[15];
    LocalDate requestedDeliveryDate =
        columns[16].isBlank() ? null : parseDate(columns[16], "requested_delivery_date");
    String specialInstruction = columns[17];
    LocalDateTime shippingReleaseAt =
        !columns[18].isBlank() ? parseDateTime(columns[18], "shipping_release_at") : null;

    try (var requestScope = MdcUtils.withEntries(Map.of("requestKey", partnerOrderId))) {
      log.info("APP_BATCH_RECORD_START function=fooOrderImport partnerOrderId={}", partnerOrderId);

      validateFooRecord(
          partnerOrderId,
          orderTypeCode,
          priorityLevel,
          customerId,
          itemCode,
          quantity,
          unitPriceExcludingTax,
          taxRate,
          orderDatetime,
          zipCode,
          address,
          deliveryName,
          deliveryPhone,
          packageCount,
          paymentMethod,
          requestedDeliveryDate,
          specialInstruction,
          shippingReleaseAt);

      if (orderHeaderRepository.findByPartnerOrderId(partnerOrderId).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "partner_order_id duplicated");
      }

      OrderType orderType = OrderType.fromCode(orderTypeCode);
      String orderId = idFactory.orderId();
      String shipmentRequestId = idFactory.shipmentRequestId();
      String notificationId = idFactory.notificationId();

      CustomerStatusResponse customerStatus = customerRegistryClient.findStatus(customerId);
      if (!"ACTIVE".equals(customerStatus.status())) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "customer is not active");
      }

      StockReservationResponse reservationResponse =
          stockKeeperClient.reserve(
              new StockReservationRequest(
                  orderId,
                  List.of(new StockReservationRequest.ReservationItem(itemCode, quantity))));
      int subtotalExcludingTax = unitPriceExcludingTax * quantity;
      int taxAmount = subtotalExcludingTax * taxRate / 100;
      int billingAmount = subtotalExcludingTax + taxAmount;
      validateBillingAmount(taxAmount, billingAmount);

      LocalDateTime now = timeProvider.now();
      CarrierCode carrierCode = determineCarrier(reservationResponse);
      boolean waitingRelease = isWaitingRelease(shippingReleaseAt, now);
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

      FooOrderImportContext context =
          new FooOrderImportContext(
              orderId,
              shipmentRequestId,
              notificationId,
              partnerOrderId,
              priorityLevel,
              customerId,
              itemCode,
              quantity,
              unitPriceExcludingTax,
              taxRate,
              orderDatetime,
              zipCode,
              address,
              deliveryName,
              deliveryPhone,
              packageCount,
              paymentMethod,
              requestedDeliveryDate,
              specialInstruction,
              shippingReleaseAt,
              subtotalExcludingTax,
              taxAmount,
              billingAmount,
              carrierCode,
              orderStatus,
              priorityResolver.resolveFooPriority(orderType, priorityLevel),
              shipmentRequestStatus,
              waitingRelease
                  ? shippingReleaseAt
                  : carrierCode == CarrierCode.BAR
                      ? businessHoursService.nextBarBusinessTime(now)
                      : now,
              determineRoutingRuleId(carrierCode, reservationResponse),
              waitingRelease ? "RECEIVED_HOLD" : "RECEIVED",
              waitingRelease ? "HOGE-ACK-001" : "HOGE-ACK-000",
              customerStatus,
              reservationResponse,
              now);

      OrderHeaderEntity header = fooOrderImportEntityMapper.toOrderHeader(context);
      orderHeaderRepository.save(header);

      OrderLineEntity line = fooOrderImportEntityMapper.toOrderLine(context);
      orderLineRepository.save(line);

      CustomerCheckResultEntity customerCheck =
          fooOrderImportEntityMapper.toCustomerCheckResult(context);
      customerCheckResultRepository.save(customerCheck);

      StockReservationResultEntity stock =
          fooOrderImportEntityMapper.toStockReservationResult(context);
      stockReservationResultRepository.save(stock);

      ShipmentRequestEntity shipmentRequest = fooOrderImportEntityMapper.toShipmentRequest(context);
      shipmentRequestRepository.save(shipmentRequest);
      sqsMessageGateway.send(
          shipmentQueueName(carrierCode),
          orderId,
          shipmentRequest.getShipmentRequestId(),
          shipmentRequest.getShipmentRequestId());

      NotificationHistoryEntity notification =
          fooOrderImportEntityMapper.toFooAckNotification(context);
      notificationHistoryRepository.save(notification);

      interfaceHistoryService.record(
          "IF-FOO-HOGE-001",
          InterfaceDirection.INBOUND,
          InterfaceStatus.SUCCESS,
          partnerOrderId,
          "200",
          "foo order imported");
      log.info(
          "APP_BATCH_RECORD_FINISH function=fooOrderImport orderId={} partnerOrderId={}",
          orderId,
          partnerOrderId);
    }
  }

  private void validateFooRecord(
      String partnerOrderId,
      String orderTypeCode,
      int priorityLevel,
      String customerId,
      String itemCode,
      int quantity,
      int unitPriceExcludingTax,
      int taxRate,
      LocalDateTime orderDatetime,
      String zipCode,
      String address,
      String deliveryName,
      String deliveryPhone,
      int packageCount,
      String paymentMethod,
      LocalDate requestedDeliveryDate,
      String specialInstruction,
      LocalDateTime shippingReleaseAt) {
    if (!ALNUM_14.matcher(partnerOrderId).matches()) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "partner_order_id invalid");
    }
    if (!List.of("01", "02", "03").contains(orderTypeCode)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "order_type invalid");
    }
    if (priorityLevel < 0 || priorityLevel > 9) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "priority_level invalid");
    }
    if (!"02".equals(orderTypeCode) && priorityLevel != 0) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "priority_level only allowed for reserved order");
    }
    if (shippingReleaseAt != null && !"02".equals(orderTypeCode)) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "shipping_release_at only allowed for reserved order");
    }
    if (shippingReleaseAt != null && shippingReleaseAt.isBefore(orderDatetime)) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "shipping_release_at invalid");
    }
    if (!ALNUM_12.matcher(customerId).matches()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "customer_id invalid");
    }
    if (!ALNUM_10.matcher(itemCode).matches()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "item_code invalid");
    }
    if (quantity < 1 || quantity > 999) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "quantity invalid");
    }
    if (unitPriceExcludingTax < 1 || unitPriceExcludingTax > 9_999_999) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "unit_price_excluding_tax invalid");
    }
    if (taxRate != 8 && taxRate != 10) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "tax_rate invalid");
    }
    if (!ZIP_7.matcher(zipCode).matches()) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "delivery_zip_code invalid");
    }
    if (address.isBlank() || address.length() > 200) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "delivery_address invalid");
    }
    if (deliveryName.isBlank() || deliveryName.length() > 60) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "delivery_name invalid");
    }
    if (!PHONE.matcher(deliveryPhone).matches()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "delivery_phone invalid");
    }
    if (packageCount < 1 || packageCount > 999 || packageCount > quantity) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "package_count invalid");
    }
    if (!List.of("PREPAID", "COD").contains(paymentMethod)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "payment_method invalid");
    }
    if (requestedDeliveryDate != null
        && requestedDeliveryDate.isBefore(orderDatetime.toLocalDate())) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "requested_delivery_date invalid");
    }
    if (specialInstruction.length() > 200) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "special_instruction invalid");
    }
  }

  private int parseInt(String rawValue, String fieldName) {
    try {
      return Integer.parseInt(rawValue);
    } catch (NumberFormatException exception) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, fieldName + " invalid");
    }
  }

  private LocalDateTime parseDateTime(String rawValue, String fieldName) {
    try {
      return LocalDateTime.parse(rawValue);
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, fieldName + " invalid");
    }
  }

  private LocalDate parseDate(String rawValue, String fieldName) {
    try {
      return LocalDate.parse(rawValue);
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, fieldName + " invalid");
    }
  }

  private void validateBillingAmount(int taxAmount, int billingAmount) {
    if (taxAmount < 0 || taxAmount > 999_999 || billingAmount < 1 || billingAmount > 9_999_999) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "billing_amount out of range");
    }
  }

  private boolean isWaitingRelease(LocalDateTime shippingReleaseAt, LocalDateTime now) {
    return shippingReleaseAt != null && shippingReleaseAt.isAfter(now);
  }

  private CarrierCode determineCarrier(StockReservationResponse reservationResponse) {
    return reservationResponse.results().stream()
        .map(StockReservationResponse.ReservationResult::warehouseLocationCode)
        .filter(location -> location != null && !location.isBlank())
        .findFirst()
        .filter(location -> location.contains("COOL"))
        .map(location -> CarrierCode.FUGA)
        .orElse(CarrierCode.BAR);
  }

  private String determineRoutingRuleId(
      CarrierCode carrierCode, StockReservationResponse reservationResponse) {
    if (carrierCode == CarrierCode.FUGA) {
      return "RULE-FUGA-WAREHOUSE";
    }
    return "RULE-BAR-FOO";
  }

  private String shipmentQueueName(CarrierCode carrierCode) {
    return carrierCode == CarrierCode.FUGA
        ? "fuga-shipment-request-queue.fifo"
        : "bar-shipment-request-queue.fifo";
  }
}
