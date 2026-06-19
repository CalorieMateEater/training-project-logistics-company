package jp.co.hoge.orderhubworker.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import jp.co.hoge.orderhub.common.domain.DeliveryStatusCode;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.dto.BarShipmentAcceptedResponse;
import jp.co.hoge.orderhub.common.dto.BarShipmentRequestPayload;
import jp.co.hoge.orderhub.common.logging.MdcUtils;
import jp.co.hoge.orderhub.common.mapper.NotificationHistoryEntityMapper;
import jp.co.hoge.orderhub.common.mapper.model.NotificationHistoryRecord;
import jp.co.hoge.orderhub.common.persistence.entity.BarIdempotencyHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
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
import jp.co.hoge.orderhubworker.mapper.model.BarIdempotencyContext;
import jp.co.hoge.orderhubworker.mapper.model.ShipmentDispatchContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配送会社向け出荷依頼を実行する Worker サービス。
 * 関連処理設計書ID: PDS-003
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentDispatchWorkerService {
    /** 出荷依頼済みを示す表示名。 */
    private static final String SHIPMENT_REQUESTED_DISPLAY_NAME = "出荷依頼受付";
    /** 再送待機時間。 */
    private static final long RETRY_DELAY_MINUTES = 5L;

    /** 出荷依頼リポジトリ。 */
    private final ShipmentRequestRepository shipmentRequestRepository;
    /** 注文ヘッダリポジトリ。 */
    private final OrderHeaderRepository orderHeaderRepository;
    /** 注文明細リポジトリ。 */
    private final OrderLineRepository orderLineRepository;
    /** Bar社冪等履歴リポジトリ。 */
    private final BarIdempotencyHistoryRepository barIdempotencyHistoryRepository;
    /** 通知履歴リポジトリ。 */
    private final NotificationHistoryRepository notificationHistoryRepository;
    /** Bar社配送 API クライアント。 */
    private final MockBarDeliveryClient mockBarDeliveryClient;
    /** インターフェース履歴記録サービス。 */
    private final InterfaceHistoryService interfaceHistoryService;
    /** 配送会社営業時間判定サービス。 */
    private final BusinessHoursService businessHoursService;
    /** ID 採番サービス。 */
    private final IdFactory idFactory;
    /** 現在時刻提供サービス。 */
    private final TimeProvider timeProvider;
    /** 出荷依頼ペイロードマッパー。 */
    private final ShipmentDispatchMapper shipmentDispatchMapper;
    /** 通知履歴エンティティマッパー。 */
    private final NotificationHistoryEntityMapper notificationHistoryEntityMapper;

    /**
     * スケジュール起動で配送依頼処理を実行する。
     */
    @Scheduled(fixedDelayString = "${hoge.worker.dispatch-delay-ms:30000}")
    public void scheduledDispatch() {
        log.info("APP_BATCH_START function=shipmentDispatch");
        dispatchPendingShipments();
    }

    /**
     * 送信対象の出荷依頼を Bar社へ配送依頼する。
     *
     * @return 配送依頼件数
     */
    @Transactional
    public int dispatchPendingShipments() {
        LocalDateTime now = timeProvider.now();
        List<ShipmentRequestEntity> targets = shipmentRequestRepository
                .findByShipmentRequestStatusInAndNextRequestAfterLessThanEqualOrderByNextRequestAfterAsc(
                        List.of(ShipmentRequestStatus.PENDING, ShipmentRequestStatus.WAITING_BUSINESS_HOURS),
                        now
                );

        int dispatched = 0;
        for (ShipmentRequestEntity shipmentRequest : targets) {
            OrderHeaderEntity order = orderHeaderRepository.findById(shipmentRequest.getOrderId()).orElseThrow();
            try (var scope = MdcUtils.withEntries(Map.of(
                    "orderId", order.getOrderId(),
                    "requestKey", shipmentRequest.getShipmentRequestId()
            ))) {
                log.info("APP_BATCH_RECORD_START function=shipmentDispatch orderId={} shipmentRequestId={}",
                        order.getOrderId(), shipmentRequest.getShipmentRequestId());

                if (!businessHoursService.isBarBusinessHours(now)) {
                    if (order.getOrderStatus() == OrderStatus.WAITING_SHIPPING_RELEASE
                            && order.getShippingReleaseAt() != null
                            && !order.getShippingReleaseAt().isAfter(now)) {
                        order.setOrderStatus(OrderStatus.WAITING_BAR_REQUEST);
                        order.setShipmentStatus(OrderStatus.WAITING_BAR_REQUEST);
                        order.setUpdatedAt(now);
                        orderHeaderRepository.save(order);
                    }
                    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.WAITING_BUSINESS_HOURS);
                    shipmentRequest.setNextRequestAfter(businessHoursService.nextBarBusinessTime(now));
                    shipmentRequestRepository.save(shipmentRequest);
                    log.info("APP_BATCH_RECORD_WAIT function=shipmentDispatch reason=outsideBusinessHours nextRequestAfter={}",
                            shipmentRequest.getNextRequestAfter());
                    continue;
                }

                List<OrderLineEntity> lines =
                        orderLineRepository.findByOrderIdOrderByOrderLineNo(order.getOrderId());
                String idempotencyKey = idFactory.idempotencyKey(shipmentRequest.getShipmentRequestId());
                BarShipmentRequestPayload payload = shipmentDispatchMapper.toBarShipmentRequestPayload(
                        new ShipmentDispatchContext(
                                order,
                                shipmentRequest,
                                lines,
                                now.toLocalDate(),
                                now.toLocalDate().plusDays(1))
                );

                shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.REQUESTING);
                shipmentRequestRepository.save(shipmentRequest);

                BarShipmentAcceptedResponse response;
                try {
                    response = mockBarDeliveryClient.requestShipment(idempotencyKey, payload);
                } catch (RuntimeException exception) {
                    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.PENDING);
                    shipmentRequest.setNextRequestAfter(now.plusMinutes(RETRY_DELAY_MINUTES));
                    shipmentRequestRepository.save(shipmentRequest);
                    interfaceHistoryService.record(
                            "IF-HOGE-BAR-001",
                            InterfaceDirection.OUTBOUND,
                            InterfaceStatus.FAILED,
                            shipmentRequest.getShipmentRequestId(),
                            "500",
                            "bar request failed: " + exception.getMessage());
                    log.error("MONITORING_BATCH_ERROR function=shipmentDispatch shipmentRequestId={} message={}",
                            shipmentRequest.getShipmentRequestId(), exception.getMessage(), exception);
                    continue;
                }

                BarIdempotencyHistoryEntity idempotency = shipmentDispatchMapper.toBarIdempotencyHistoryEntity(
                        new BarIdempotencyContext(
                                idempotencyKey,
                                shipmentRequest.getShipmentRequestId(),
                                hash(payload.toString()),
                                response.barShipmentId(),
                                now
                        )
                );
                barIdempotencyHistoryRepository.save(idempotency);

                shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.ACCEPTED);
                shipmentRequest.setRequestedAt(now);
                shipmentRequest.setBarAcceptedAt(now);
                shipmentRequest.setBarAcceptNo(response.barShipmentId());
                shipmentRequestRepository.save(shipmentRequest);

                order.setOrderStatus(OrderStatus.BAR_ACCEPTED);
                order.setShipmentStatus(OrderStatus.BAR_ACCEPTED);
                order.setUpdatedAt(now);
                orderHeaderRepository.save(order);

                interfaceHistoryService.record(
                        "IF-HOGE-BAR-001",
                        InterfaceDirection.OUTBOUND,
                        InterfaceStatus.SUCCESS,
                        shipmentRequest.getShipmentRequestId(),
                        "201",
                        "bar accepted");

                createNotification(
                        order,
                        NotificationType.BAZ_BILLING,
                        "billing-plan-queue",
                        "PROVISIONAL_BILLING",
                        DeliveryStatusCode.ACCEPTED.name(),
                        null,
                        null,
                        "baz-billing:" + order.getOrderId() + ":provisional"
                );
                createNotification(
                        order,
                        NotificationType.QUX_ORDER,
                        "order-notice-queue.fifo",
                        "SHIPMENT_REQUESTED",
                        OrderStatus.BAR_ACCEPTED.name(),
                        SHIPMENT_REQUESTED_DISPLAY_NAME,
                        null,
                        "qux-shipment:" + order.getOrderId()
                );
                log.info("APP_BATCH_RECORD_FINISH function=shipmentDispatch orderId={} shipmentRequestId={} result=accepted",
                        order.getOrderId(), shipmentRequest.getShipmentRequestId());
                dispatched++;
            }
        }

        return dispatched;
    }

    private void createNotification(
            OrderHeaderEntity order,
            NotificationType type,
            String destination,
            String eventType,
            String payloadSummary,
            String displayStatusName,
            String referenceNotificationId,
            String notificationKey
    ) {
        LocalDateTime now = timeProvider.now();
        NotificationHistoryEntity notification = notificationHistoryEntityMapper.toEntity(
                new NotificationHistoryRecord(
                        idFactory.notificationId(),
                        order.getOrderId(),
                        type,
                        NotificationStatus.SENT,
                        payloadSummary,
                        notificationKey,
                        eventType,
                        referenceNotificationId,
                        displayStatusName,
                        destination,
                        now,
                        now
                )
        );
        notificationHistoryRepository.save(notification);
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
