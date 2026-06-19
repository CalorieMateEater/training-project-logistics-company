package jp.co.hoge.shippinggateway.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.DeliveryStatusCode;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.dto.BarDeliveryResultRequest;
import jp.co.hoge.orderhub.common.mapper.NotificationHistoryEntityMapper;
import jp.co.hoge.orderhub.common.mapper.model.NotificationHistoryRecord;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.StatusMapper;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.shippinggateway.mapper.ShipmentGatewayEntityMapper;
import jp.co.hoge.shippinggateway.mapper.model.BarDeliveryResultContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Bar社から返却された配送結果を反映するサービス。
 * 関連処理設計書ID: PDS-004, PDS-005
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BarDeliveryResultService {

    /** 住所補正中の表示名。 */
    private static final String ADDRESS_CORRECTED_DISPLAY_NAME = "住所補正対応中";

    /** 注文ヘッダリポジトリ。 */
    private final OrderHeaderRepository orderHeaderRepository;

    /** 配送状態最新リポジトリ。 */
    private final DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

    /** 配送状態履歴リポジトリ。 */
    private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;

    /** 通知履歴リポジトリ。 */
    private final NotificationHistoryRepository notificationHistoryRepository;

    /** インターフェース履歴記録サービス。 */
    private final InterfaceHistoryService interfaceHistoryService;

    /** ID 採番サービス。 */
    private final IdFactory idFactory;

    /** ステータス変換サービス。 */
    private final StatusMapper statusMapper;

    /** 現在時刻提供サービス。 */
    private final TimeProvider timeProvider;

    /** 配送結果エンティティマッパー。 */
    private final ShipmentGatewayEntityMapper shipmentGatewayEntityMapper;

    /** 通知履歴エンティティマッパー。 */
    private final NotificationHistoryEntityMapper notificationHistoryEntityMapper;

    /**
     * Bar社の配送結果通知を受け付けて注文データへ反映する。
     *
     * @param request 配送結果リクエスト
     */
    @Transactional
    public void accept(BarDeliveryResultRequest request) {
        log.info(
                "APP_DELIVERY_RESULT_START orderId={} barShipmentId={} statusSeq={}",
                request.orderId(),
                request.barShipmentId(),
                request.statusSeq()
        );

        OrderHeaderEntity orderHeader = orderHeaderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        DeliveryStatusCurrentEntity current = deliveryStatusCurrentRepository.findById(request.orderId()).orElse(null);
        if (current != null && request.statusSeq() <= current.getLatestStatusSeq()) {
            interfaceHistoryService.record(
                    "IF-BAR-HOGE-002",
                    InterfaceDirection.INBOUND,
                    InterfaceStatus.SKIPPED,
                    request.barShipmentId(),
                    null,
                    "202",
                    "old or duplicated status skipped"
            );
            log.info(
                    "APP_DELIVERY_RESULT_SKIPPED orderId={} barShipmentId={} latestStatusSeq={} requestStatusSeq={}",
                    request.orderId(),
                    request.barShipmentId(),
                    current.getLatestStatusSeq(),
                    request.statusSeq()
            );
            return;
        }

        DeliveryStatusCode statusCode = DeliveryStatusCode.valueOf(request.deliveryStatus());
        LocalDateTime now = timeProvider.now();
        LocalDateTime occurredAt = LocalDateTime.parse(request.eventOccurredAt());
        BarDeliveryResultContext context = new BarDeliveryResultContext(
                request,
                resolveDisplayStatusName(request),
                hash(request.toString()),
                occurredAt,
                now
        );

        deliveryStatusHistoryRepository.save(shipmentGatewayEntityMapper.toDeliveryStatusHistoryEntity(context));

        DeliveryStatusCurrentEntity currentEntity = current == null ? new DeliveryStatusCurrentEntity() : current;
        shipmentGatewayEntityMapper.updateDeliveryStatusCurrentEntity(context, currentEntity);
        deliveryStatusCurrentRepository.save(currentEntity);

        orderHeader.setOrderStatus(statusMapper.toOrderStatus(statusCode));
        orderHeader.setShipmentStatus(statusMapper.toOrderStatus(statusCode));
        orderHeader.setUpdatedAt(now);
        orderHeaderRepository.save(orderHeader);

        if (orderHeader.getOrderSource() == OrderSource.FOO) {
            notificationHistoryRepository.save(notificationHistoryEntityMapper.toEntity(
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
                            now
                    )
            ));
        }

        createQuxNotification(orderHeader, currentEntity, request, now);
        if (statusCode == DeliveryStatusCode.DELIVERED) {
            createBazFinalNotification(orderHeader, now);
        }

        interfaceHistoryService.record(
                "IF-BAR-HOGE-002",
                InterfaceDirection.INBOUND,
                InterfaceStatus.SUCCESS,
                request.barShipmentId(),
                null,
                "202",
                "delivery result accepted"
        );

        log.info(
                "APP_DELIVERY_RESULT_FINISH orderId={} barShipmentId={} deliveryStatus={}",
                request.orderId(),
                request.barShipmentId(),
                request.deliveryStatus()
        );
    }

    private void createQuxNotification(
            OrderHeaderEntity orderHeader,
            DeliveryStatusCurrentEntity currentEntity,
            BarDeliveryResultRequest request,
            LocalDateTime now
    ) {
        notificationHistoryRepository.save(notificationHistoryEntityMapper.toEntity(
                new NotificationHistoryRecord(
                        idFactory.notificationId(),
                        orderHeader.getOrderId(),
                        NotificationType.QUX_ORDER,
                        NotificationStatus.SENT,
                        request.deliveryStatus(),
                        "qux-status:" + orderHeader.getOrderId() + ":" + request.statusSeq(),
                        "STATUS_UPDATED",
                        null,
                        currentEntity.getLatestDisplayStatusName(),
                        "order-notice-queue.fifo",
                        now,
                        now
                )
        ));
    }

    private void createBazFinalNotification(OrderHeaderEntity orderHeader, LocalDateTime now) {
        var provisional = notificationHistoryRepository
                .findFirstByOrderIdAndNotificationTypeAndEventTypeOrderByCreatedAtAsc(
                        orderHeader.getOrderId(),
                        NotificationType.BAZ_BILLING,
                        "PROVISIONAL_BILLING"
                )
                .orElse(null);

        notificationHistoryRepository.save(notificationHistoryEntityMapper.toEntity(
                new NotificationHistoryRecord(
                        idFactory.notificationId(),
                        orderHeader.getOrderId(),
                        NotificationType.BAZ_BILLING,
                        NotificationStatus.SENT,
                        DeliveryStatusCode.DELIVERED.name(),
                        "baz-billing:" + orderHeader.getOrderId() + ":final",
                        "FINAL_BILLING",
                        provisional == null ? null : provisional.getNotificationId(),
                        null,
                        "billing-plan-queue",
                        now,
                        now
                )
        ));
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
