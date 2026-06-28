package jp.co.hoge.orderhubworker.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jp.co.hoge.orderhub.common.domain.HulftSendStatus;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.integration.HulftSendGateway;
import jp.co.hoge.orderhub.common.logging.MdcUtils;
import jp.co.hoge.orderhub.common.persistence.entity.HulftSendRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.repository.HulftSendRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubworker.config.WorkerFileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Foo社向け受付通知ファイルを出力する Worker サービス。 関連処理機能ID: PGD-001
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FooAckNotificationWorkerService {
  /** 出力ファイル名用タイムスタンプ。 */
  private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

  /** 通知履歴リポジトリ。 */
  private final NotificationHistoryRepository notificationHistoryRepository;

  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /** HULFT送信要求リポジトリ。 */
  private final HulftSendRequestRepository hulftSendRequestRepository;

  /** HULFT送信ゲートウェイ。 */
  private final HulftSendGateway hulftSendGateway;

  /** ID 採番サービス。 */
  private final IdFactory idFactory;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /** Worker ファイル出力設定。 */
  private final WorkerFileProperties workerFileProperties;

  /** スケジュール起動で Foo社向け受付通知を実行する。 */
  @Scheduled(fixedDelayString = "${hoge.worker.foo-ack-delay-ms:45000}")
  public void scheduledPublish() {
    log.info("APP_BATCH_START function=fooAckNotification");
    publishPendingAckNotifications();
  }

  /**
   * 未通知の Foo社向け受付通知をファイル出力する。
   *
   * @return 通知件数
   */
  @Transactional
  public int publishPendingAckNotifications() {
    List<NotificationHistoryEntity> targets =
        notificationHistoryRepository
            .findByNotificationTypeAndNotificationStatusOrderByCreatedAtAsc(
                NotificationType.FOO_ACK, NotificationStatus.PENDING);

    int published = 0;
    for (NotificationHistoryEntity notification : targets) {
      OrderHeaderEntity order =
          orderHeaderRepository.findById(notification.getOrderId()).orElseThrow();
      try (var scope = MdcUtils.withOrder(order.getOrderId())) {
        log.info(
            "APP_BATCH_RECORD_START function=fooAckNotification orderId={}", order.getOrderId());
        Path filePath = writeAckFile(order.getPartnerOrderId(), notification.getPayloadSummary());
        requestHulftSend("IF-HOGE-FOO-001", "FOO", filePath, notification);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setUpdatedAt(timeProvider.now());
        notificationHistoryRepository.save(notification);
        interfaceHistoryService.record(
            "IF-HOGE-FOO-001",
            InterfaceDirection.OUTBOUND,
            InterfaceStatus.SUCCESS,
            order.getPartnerOrderId(),
            "200",
            "foo ack notified");
        log.info(
            "APP_BATCH_RECORD_FINISH function=fooAckNotification orderId={}", order.getOrderId());
      }
      published++;
    }

    return published;
  }

  private Path writeAckFile(String partnerOrderId, String payloadSummary) {
    try {
      Path ackDir = Path.of(workerFileProperties.getFooAckDir());
      Files.createDirectories(ackDir);
      String[] payload = payloadSummary.split("\\|", 3);
      String receiptStatus = payload[0];
      String messageCode = payload[1];
      String acceptedAt = payload[2];
      String fileName = "FOO_ORDER_ACK_" + timeProvider.now().format(FILE_TS) + "_001.dat";
      String line = partnerOrderId + "," + receiptStatus + "," + acceptedAt + "," + messageCode;
      Path filePath = ackDir.resolve(fileName);
      Files.writeString(filePath, line + System.lineSeparator(), StandardCharsets.UTF_8);
      return filePath;
    } catch (IOException exception) {
      log.error(
          "MONITORING_BATCH_ERROR function=fooAckNotificationWrite message={}",
          exception.getMessage(),
          exception);
      throw new IllegalStateException(exception);
    }
  }

  private void requestHulftSend(
      String ifId, String partnerCode, Path filePath, NotificationHistoryEntity notification) {
    var now = timeProvider.now();
    HulftSendRequestEntity sendRequest = new HulftSendRequestEntity();
    sendRequest.setHulftSendRequestId(idFactory.notificationId());
    sendRequest.setIfId(ifId);
    sendRequest.setPartnerCode(partnerCode);
    sendRequest.setFilePath(filePath.toString());
    sendRequest.setNotificationId(notification.getNotificationId());
    sendRequest.setSendStatus(HulftSendStatus.PENDING);
    sendRequest.setCreatedAt(now);
    sendRequest.setUpdatedAt(now);
    hulftSendRequestRepository.save(sendRequest);

    sendRequest.setSendStatus(HulftSendStatus.REQUESTING);
    sendRequest.setUpdatedAt(timeProvider.now());
    hulftSendRequestRepository.save(sendRequest);
    String transferId =
        hulftSendGateway.requestSend(
            sendRequest.getHulftSendRequestId(), ifId, partnerCode, filePath);
    sendRequest.setHulftTransferId(transferId);
    sendRequest.setRequestedAt(timeProvider.now());
    sendRequest.setSendStatus(HulftSendStatus.SENT);
    sendRequest.setUpdatedAt(sendRequest.getRequestedAt());
    hulftSendRequestRepository.save(sendRequest);
  }
}
