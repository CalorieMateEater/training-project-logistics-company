package jp.co.hoge.shippinggateway.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import jp.co.hoge.orderhub.common.domain.DeliveryStatusCode;
import jp.co.hoge.orderhub.common.domain.DeliveryStatusReflectionStatus;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.dto.BarDeliveryResultRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.integration.SqsMessageGateway;
import jp.co.hoge.orderhub.common.mapper.NotificationHistoryEntityMapper;
import jp.co.hoge.orderhub.common.mapper.model.NotificationHistoryRecord;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusHistoryId;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusReflectionRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusReflectionRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.StatusMapper;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.shippinggateway.mapper.ShipmentGatewayEntityMapper;
import jp.co.hoge.shippinggateway.mapper.model.BarDeliveryResultContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 配送会社から返却された配送結果を受け付け、状態反映Workerへ引き渡すサービス。 関連処理機能ID: PGD-006
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BarDeliveryResultService {

  /** Bar社コード。 */
  private static final String CARRIER_BAR = "BAR";

  /** Fuga社コード。 */
  private static final String CARRIER_FUGA = "FUGA";

  /** 住所補正中の表示名。 */
  private static final String ADDRESS_CORRECTED_DISPLAY_NAME = "住所補正対応中";

  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** 配送状態最新リポジトリ。 */
  private final DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

  /** 配送状態履歴リポジトリ。 */
  private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;

  /** 配送状態反映要求リポジトリ。 */
  private final DeliveryStatusReflectionRequestRepository reflectionRequestRepository;

  /** 通知履歴リポジトリ。 */
  private final NotificationHistoryRepository notificationHistoryRepository;

  /** 在庫引当結果リポジトリ。 */
  private final StockReservationResultRepository stockReservationResultRepository;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /** ID 採番サービス。 */
  private final IdFactory idFactory;

  /** ステータス変換サービス。 */
  private final StatusMapper statusMapper;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /** 在庫管理クライアント。 */
  private final StockKeeperClient stockKeeperClient;

  /** SQS送信ゲートウェイ。 */
  private final SqsMessageGateway sqsMessageGateway;

  /** 配送結果エンティティマッパー。 */
  private final ShipmentGatewayEntityMapper shipmentGatewayEntityMapper;

  /** 通知履歴エンティティマッパー。 */
  private final NotificationHistoryEntityMapper notificationHistoryEntityMapper;

  /**
   * 配送会社の配送結果通知を受け付け、状態反映要求を起票する。
   *
   * @param carrierRaw 配送会社コード
   * @param request 配送結果リクエスト
   */
  @Transactional
  public void accept(String carrierRaw, BarDeliveryResultRequest request) {
    String carrierCode = normalizeCarrier(carrierRaw);
    String interfaceId = deliveryResultInterfaceId(carrierCode);
    log.info(
        "APP_DELIVERY_RESULT_ACCEPT_START carrierCode={} orderId={} carrierShipmentId={} statusSeq={}",
        carrierCode,
        request.orderId(),
        request.barShipmentId(),
        request.statusSeq());

    OrderHeaderEntity orderHeader =
        orderHeaderRepository
            .findById(request.orderId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    DeliveryStatusCode statusCode = parseStatusCode(request.deliveryStatus());
    String rawPayloadHash = hash(request.toString());
    handleDuplicatedReflectionRequest(carrierCode, interfaceId, request, rawPayloadHash);

    DeliveryStatusCurrentEntity current =
        deliveryStatusCurrentRepository.findById(request.orderId()).orElse(null);
    if (current != null && request.statusSeq() <= current.getLatestStatusSeq()) {
      handleProcessedEvent(carrierCode, interfaceId, request, rawPayloadHash, current);
      return;
    }
    validateTransition(carrierCode, orderHeader.getOrderStatus(), statusCode);

    LocalDateTime now = timeProvider.now();
    DeliveryStatusReflectionRequestEntity reflectionRequest =
        toReflectionRequest(carrierCode, request, rawPayloadHash, now);
    reflectionRequestRepository.save(reflectionRequest);

    interfaceHistoryService.record(
        interfaceId,
        InterfaceDirection.INBOUND,
        InterfaceStatus.ACCEPTED,
        request.barShipmentId(),
        null,
        "202",
        "delivery result reflection requested");

    log.info(
        "APP_DELIVERY_RESULT_ACCEPT_FINISH carrierCode={} orderId={} reflectionRequestId={}",
        carrierCode,
        request.orderId(),
        reflectionRequest.getReflectionRequestId());
  }

  /** スケジュール起動で配送状態反映要求を処理する。 */
  @Scheduled(fixedDelayString = "${hoge.gateway.delivery-result-reflection-delay-ms:15000}")
  public void scheduledReflect() {
    log.info("APP_BATCH_START function=deliveryStatusReflection");
    processPendingReflectionRequests();
  }

  /**
   * 未処理の配送状態反映要求を配送状態へ反映する。
   *
   * @return 反映件数
   */
  @Transactional
  public int processPendingReflectionRequests() {
    List<DeliveryStatusReflectionRequestEntity> targets =
        reflectionRequestRepository.findByReflectionStatusOrderByCreatedAtAsc(
            DeliveryStatusReflectionStatus.PENDING);
    int processed = 0;
    for (DeliveryStatusReflectionRequestEntity target : targets) {
      try {
        target.setReflectionStatus(DeliveryStatusReflectionStatus.PROCESSING);
        target.setUpdatedAt(timeProvider.now());
        reflectionRequestRepository.save(target);
        applyReflection(target);
        target.setReflectionStatus(DeliveryStatusReflectionStatus.PROCESSED);
        target.setProcessedAt(timeProvider.now());
        target.setUpdatedAt(target.getProcessedAt());
        reflectionRequestRepository.save(target);
        processed++;
      } catch (RuntimeException exception) {
        target.setReflectionStatus(DeliveryStatusReflectionStatus.FAILED);
        target.setErrorMessage(exception.getMessage());
        target.setUpdatedAt(timeProvider.now());
        reflectionRequestRepository.save(target);
        log.error(
            "MONITORING_BATCH_ERROR function=deliveryStatusReflection reflectionRequestId={} message={}",
            target.getReflectionRequestId(),
            exception.getMessage(),
            exception);
      }
    }
    return processed;
  }

  private void applyReflection(DeliveryStatusReflectionRequestEntity reflectionRequest) {
    BarDeliveryResultRequest request = toRequest(reflectionRequest);
    DeliveryStatusCode statusCode =
        DeliveryStatusCode.valueOf(reflectionRequest.getDeliveryStatus());
    OrderHeaderEntity orderHeader =
        orderHeaderRepository
            .findById(reflectionRequest.getOrderId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    DeliveryStatusCurrentEntity current =
        deliveryStatusCurrentRepository.findById(reflectionRequest.getOrderId()).orElse(null);
    if (current != null && reflectionRequest.getStatusSeq() <= current.getLatestStatusSeq()) {
      handleProcessedEvent(
          reflectionRequest.getCarrierCode(),
          deliveryResultInterfaceId(reflectionRequest.getCarrierCode()),
          request,
          reflectionRequest.getRawPayloadHash(),
          current);
      return;
    }
    validateTransition(
        reflectionRequest.getCarrierCode(), orderHeader.getOrderStatus(), statusCode);

    LocalDateTime now = timeProvider.now();
    BarDeliveryResultContext context =
        new BarDeliveryResultContext(
            request,
            resolveDisplayStatusName(request),
            reflectionRequest.getRawPayloadHash(),
            reflectionRequest.getEventOccurredAt(),
            now);

    deliveryStatusHistoryRepository.save(
        shipmentGatewayEntityMapper.toDeliveryStatusHistoryEntity(context));

    DeliveryStatusCurrentEntity currentEntity =
        current == null ? new DeliveryStatusCurrentEntity() : current;
    shipmentGatewayEntityMapper.updateDeliveryStatusCurrentEntity(context, currentEntity);
    deliveryStatusCurrentRepository.save(currentEntity);

    orderHeader.setOrderStatus(toOrderStatus(reflectionRequest.getCarrierCode(), statusCode));
    orderHeader.setShipmentStatus(orderHeader.getOrderStatus());
    orderHeader.setUpdatedAt(now);
    orderHeaderRepository.save(orderHeader);

    if (CARRIER_BAR.equals(reflectionRequest.getCarrierCode())
        && statusCode == DeliveryStatusCode.ACCEPTED) {
      confirmReservedStock(orderHeader.getOrderId());
    }

    if (orderHeader.getOrderSource() == OrderSource.FOO) {
      createFooStatusNotification(orderHeader, currentEntity, request, now);
      createQuxNotification(orderHeader, currentEntity, request, now);
    }

    if (statusCode == DeliveryStatusCode.DELIVERED) {
      createBazFinalNotification(orderHeader, now);
    }

    interfaceHistoryService.record(
        deliveryResultInterfaceId(reflectionRequest.getCarrierCode()),
        InterfaceDirection.INBOUND,
        InterfaceStatus.SUCCESS,
        reflectionRequest.getCarrierShipmentId(),
        null,
        "202",
        "delivery result reflected");

    log.info(
        "APP_DELIVERY_RESULT_REFLECT_FINISH carrierCode={} orderId={} carrierShipmentId={} deliveryStatus={}",
        reflectionRequest.getCarrierCode(),
        reflectionRequest.getOrderId(),
        reflectionRequest.getCarrierShipmentId(),
        reflectionRequest.getDeliveryStatus());
  }

  private void handleDuplicatedReflectionRequest(
      String carrierCode,
      String interfaceId,
      BarDeliveryResultRequest request,
      String rawPayloadHash) {
    reflectionRequestRepository
        .findFirstByOrderIdAndStatusSeqOrderByCreatedAtDesc(request.orderId(), request.statusSeq())
        .ifPresent(
            existing -> {
              if (!existing.getRawPayloadHash().equals(rawPayloadHash)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "delivery result payload conflict");
              }
              if (existing.getReflectionStatus() != DeliveryStatusReflectionStatus.FAILED) {
                interfaceHistoryService.record(
                    interfaceId,
                    InterfaceDirection.INBOUND,
                    InterfaceStatus.SKIPPED,
                    request.barShipmentId(),
                    null,
                    "202",
                    carrierCode.toLowerCase() + " duplicated reflection request skipped");
                throw new ResponseStatusException(HttpStatus.ACCEPTED, "duplicated accepted");
              }
            });
  }

  private void handleProcessedEvent(
      String carrierCode,
      String interfaceId,
      BarDeliveryResultRequest request,
      String rawPayloadHash,
      DeliveryStatusCurrentEntity current) {
    if (request.statusSeq() == current.getLatestStatusSeq()) {
      deliveryStatusHistoryRepository
          .findById(new DeliveryStatusHistoryId(request.orderId(), request.statusSeq()))
          .ifPresent(
              history -> {
                if (!history.getRawPayloadHash().equals(rawPayloadHash)) {
                  throw new ResponseStatusException(
                      HttpStatus.CONFLICT, "delivery result payload conflict");
                }
              });
    }

    if (request.statusSeq() < current.getLatestStatusSeq() && CARRIER_FUGA.equals(carrierCode)) {
      interfaceHistoryService.record(
          interfaceId,
          InterfaceDirection.INBOUND,
          InterfaceStatus.FAILED,
          request.barShipmentId(),
          null,
          "409",
          "old status sequence rejected");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "old status sequence");
    }

    interfaceHistoryService.record(
        interfaceId,
        InterfaceDirection.INBOUND,
        InterfaceStatus.SKIPPED,
        request.barShipmentId(),
        null,
        "202",
        "old or duplicated status skipped");
    log.info(
        "APP_DELIVERY_RESULT_SKIPPED carrierCode={} orderId={} carrierShipmentId={} latestStatusSeq={} requestStatusSeq={}",
        carrierCode,
        request.orderId(),
        request.barShipmentId(),
        current.getLatestStatusSeq(),
        request.statusSeq());
  }

  private DeliveryStatusReflectionRequestEntity toReflectionRequest(
      String carrierCode,
      BarDeliveryResultRequest request,
      String rawPayloadHash,
      LocalDateTime now) {
    DeliveryStatusReflectionRequestEntity entity = new DeliveryStatusReflectionRequestEntity();
    entity.setReflectionRequestId(idFactory.notificationId());
    entity.setCarrierCode(carrierCode);
    entity.setCarrierShipmentId(request.barShipmentId());
    entity.setOrderId(request.orderId());
    entity.setPartnerOrderId(request.partnerOrderId());
    entity.setStatusSeq(request.statusSeq());
    entity.setDeliveryStatus(request.deliveryStatus());
    entity.setStatusLabel(request.statusLabel());
    entity.setEventOccurredAt(LocalDateTime.parse(request.eventOccurredAt()));
    entity.setTemperatureZone(request.temperatureZone());
    entity.setSizeType(request.sizeType());
    entity.setLocationCode(request.locationCode());
    entity.setReasonCode(request.reasonCode());
    entity.setReasonCategory(request.reasonCategory());
    entity.setAddressCorrected(request.addressCorrected());
    entity.setAddressCorrectionLevel(request.addressCorrectionLevel());
    entity.setDriverComment(request.driverComment());
    entity.setRawPayloadHash(rawPayloadHash);
    entity.setReflectionStatus(DeliveryStatusReflectionStatus.PENDING);
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    return entity;
  }

  private BarDeliveryResultRequest toRequest(DeliveryStatusReflectionRequestEntity entity) {
    return new BarDeliveryResultRequest(
        entity.getCarrierShipmentId(),
        entity.getOrderId(),
        entity.getPartnerOrderId(),
        entity.getStatusSeq(),
        entity.getDeliveryStatus(),
        entity.getStatusLabel(),
        entity.getEventOccurredAt().toString(),
        entity.getTemperatureZone(),
        entity.getSizeType(),
        entity.getLocationCode(),
        entity.getReasonCode(),
        entity.getReasonCategory(),
        entity.getAddressCorrected(),
        entity.getAddressCorrectionLevel(),
        entity.getDriverComment());
  }

  private DeliveryStatusCode parseStatusCode(String deliveryStatus) {
    try {
      return DeliveryStatusCode.valueOf(deliveryStatus);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delivery status not supported");
    }
  }

  private void validateTransition(
      String carrierCode, OrderStatus currentOrderStatus, DeliveryStatusCode nextStatusCode) {
    OrderStatus nextOrderStatus = toOrderStatus(carrierCode, nextStatusCode);
    if (currentOrderStatus == nextOrderStatus) {
      return;
    }
    boolean allowed =
        switch (nextStatusCode) {
          case ACCEPTED ->
              CARRIER_FUGA.equals(carrierCode)
                  ? EnumSet.of(OrderStatus.WAITING_FUGA_REQUEST, OrderStatus.FUGA_ACCEPTED)
                      .contains(currentOrderStatus)
                  : EnumSet.of(OrderStatus.WAITING_BAR_REQUEST, OrderStatus.BAR_ACCEPTED)
                      .contains(currentOrderStatus);
          case PREPARING ->
              EnumSet.of(OrderStatus.BAR_ACCEPTED, OrderStatus.FUGA_ACCEPTED)
                  .contains(currentOrderStatus);
          case IN_TRANSIT ->
              EnumSet.of(
                      OrderStatus.BAR_ACCEPTED,
                      OrderStatus.FUGA_ACCEPTED,
                      OrderStatus.PREPARING_FOR_SHIPMENT,
                      OrderStatus.IN_DELIVERY_FLOW,
                      OrderStatus.REDISPATCH_PENDING)
                  .contains(currentOrderStatus);
          case DELIVERED ->
              EnumSet.of(
                      OrderStatus.PREPARING_FOR_SHIPMENT,
                      OrderStatus.IN_DELIVERY_FLOW,
                      OrderStatus.REDISPATCH_PENDING)
                  .contains(currentOrderStatus);
          case DELIVERY_FAILED, RETURNED_TO_BASE, ADDRESS_ERROR, REDISPATCH_PENDING ->
              EnumSet.of(
                      OrderStatus.BAR_ACCEPTED,
                      OrderStatus.FUGA_ACCEPTED,
                      OrderStatus.PREPARING_FOR_SHIPMENT,
                      OrderStatus.IN_DELIVERY_FLOW,
                      OrderStatus.REDISPATCH_PENDING)
                  .contains(currentOrderStatus);
          case CANCELLED ->
              !EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)
                  .contains(currentOrderStatus);
          case WAITING_BAR_REQUEST -> false;
        };
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "delivery status transition conflict");
    }
  }

  private String normalizeCarrier(String carrierRaw) {
    String carrierCode = carrierRaw == null ? "" : carrierRaw.toUpperCase();
    if (CARRIER_BAR.equals(carrierCode) || CARRIER_FUGA.equals(carrierCode)) {
      return carrierCode;
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "carrier not supported");
  }

  private String deliveryResultInterfaceId(String carrierCode) {
    return CARRIER_FUGA.equals(carrierCode) ? "IF-FUGA-HOGE-002" : "IF-BAR-HOGE-002";
  }

  private OrderStatus toOrderStatus(String carrierCode, DeliveryStatusCode statusCode) {
    if (statusCode == DeliveryStatusCode.ACCEPTED && CARRIER_FUGA.equals(carrierCode)) {
      return OrderStatus.FUGA_ACCEPTED;
    }
    return statusMapper.toOrderStatus(statusCode);
  }

  private void createFooStatusNotification(
      OrderHeaderEntity orderHeader,
      DeliveryStatusCurrentEntity currentEntity,
      BarDeliveryResultRequest request,
      LocalDateTime now) {
    notificationHistoryRepository.save(
        notificationHistoryEntityMapper.toEntity(
            new NotificationHistoryRecord(
                idFactory.notificationId(),
                orderHeader.getOrderId(),
                NotificationType.FOO_STATUS,
                NotificationStatus.PENDING,
                request.deliveryStatus() + ":" + request.statusLabel(),
                "foo-status:" + orderHeader.getOrderId() + ":" + request.statusSeq(),
                "STATUS_UPDATED",
                null,
                currentEntity.getLatestDisplayStatusName(),
                "foo-status-file",
                now,
                now)));
  }

  private void createQuxNotification(
      OrderHeaderEntity orderHeader,
      DeliveryStatusCurrentEntity currentEntity,
      BarDeliveryResultRequest request,
      LocalDateTime now) {
    String notificationKey = "qux-status:" + orderHeader.getOrderId() + ":" + request.statusSeq();
    String destination = "order-notice-queue.fifo";
    sqsMessageGateway.send(
        destination, orderHeader.getOrderId(), notificationKey, request.deliveryStatus());
    notificationHistoryRepository.save(
        notificationHistoryEntityMapper.toEntity(
            new NotificationHistoryRecord(
                idFactory.notificationId(),
                orderHeader.getOrderId(),
                NotificationType.QUX_ORDER,
                NotificationStatus.SENT,
                request.deliveryStatus(),
                notificationKey,
                "STATUS_UPDATED",
                null,
                currentEntity.getLatestDisplayStatusName(),
                destination,
                now,
                now)));
  }

  private void createBazFinalNotification(OrderHeaderEntity orderHeader, LocalDateTime now) {
    var provisional =
        notificationHistoryRepository
            .findFirstByOrderIdAndNotificationTypeAndEventTypeOrderByCreatedAtAsc(
                orderHeader.getOrderId(), NotificationType.BAZ_BILLING, "PROVISIONAL_BILLING")
            .orElse(null);
    String destination = "billing-plan-queue";
    String notificationKey = "baz-billing:" + orderHeader.getOrderId() + ":final";
    sqsMessageGateway.send(
        destination,
        orderHeader.getOrderId(),
        notificationKey,
        DeliveryStatusCode.DELIVERED.name());

    notificationHistoryRepository.save(
        notificationHistoryEntityMapper.toEntity(
            new NotificationHistoryRecord(
                idFactory.notificationId(),
                orderHeader.getOrderId(),
                NotificationType.BAZ_BILLING,
                NotificationStatus.SENT,
                DeliveryStatusCode.DELIVERED.name(),
                notificationKey,
                "FINAL_BILLING",
                provisional == null ? null : provisional.getNotificationId(),
                null,
                destination,
                now,
                now)));
  }

  private String resolveDisplayStatusName(BarDeliveryResultRequest request) {
    if (Boolean.TRUE.equals(request.addressCorrected())) {
      return ADDRESS_CORRECTED_DISPLAY_NAME;
    }
    if ("ADDRESS_CORRECTED".equals(request.reasonCategory())) {
      return ADDRESS_CORRECTED_DISPLAY_NAME;
    }
    return request.statusLabel();
  }

  private void confirmReservedStock(String orderId) {
    List<StockReservationResultEntity> reservationResults =
        stockReservationResultRepository.findByOrderIdOrderByOrderLineNo(orderId);
    for (StockReservationResultEntity reservationResult : reservationResults) {
      if ("SHIPPED_CONFIRMED".equals(reservationResult.getReservationStatus())) {
        continue;
      }
      StockReservationOperationResponse response =
          stockKeeperClient.shipConfirm(reservationResult.getReservationId());
      response.results().stream()
          .filter(result -> result.itemCode().equals(reservationResult.getItemCode()))
          .findFirst()
          .ifPresent(
              result -> {
                reservationResult.setReservationStatus(result.reservationStatus());
                reservationResult.setShippedConfirmedQuantity(result.processedQuantity());
                stockReservationResultRepository.save(reservationResult);
              });
    }
  }

  private String hash(String source) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte value : hash) {
        builder.append(String.format("%02x", value));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
