package jp.co.hoge.orderhubworker.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.logging.MdcUtils;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubworker.config.WorkerFileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Foo社向け配送結果通知ファイルを出力する Worker サービス。 関連処理機能ID: PGD-003
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FooStatusNotificationWorkerService {
  /** 出力ファイル名用タイムスタンプ。 */
  private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

  /** 出力データ用日時フォーマッタ。 */
  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /** 通知履歴リポジトリ。 */
  private final NotificationHistoryRepository notificationHistoryRepository;

  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** 配送状態最新リポジトリ。 */
  private final DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /** Worker ファイル出力設定。 */
  private final WorkerFileProperties workerFileProperties;

  /** スケジュール起動で Foo社向け配送結果通知を実行する。 */
  @Scheduled(fixedDelayString = "${hoge.worker.foo-status-delay-ms:45000}")
  public void scheduledPublish() {
    log.info("APP_BATCH_START function=fooStatusNotification");
    publishPendingStatusNotifications();
  }

  /**
   * 未通知の Foo社向け配送結果通知をファイル出力する。
   *
   * @return 通知件数
   */
  @Transactional
  public int publishPendingStatusNotifications() {
    List<NotificationHistoryEntity> targets =
        notificationHistoryRepository
            .findByNotificationTypeAndNotificationStatusOrderByCreatedAtAsc(
                NotificationType.FOO_STATUS, NotificationStatus.PENDING);

    int published = 0;
    for (NotificationHistoryEntity notification : targets) {
      OrderHeaderEntity order =
          orderHeaderRepository.findById(notification.getOrderId()).orElseThrow();
      DeliveryStatusCurrentEntity current =
          deliveryStatusCurrentRepository.findById(notification.getOrderId()).orElseThrow();
      try (var scope = MdcUtils.withOrder(order.getOrderId())) {
        log.info(
            "APP_BATCH_RECORD_START function=fooStatusNotification orderId={}", order.getOrderId());
        writeStatusFile(order, current);
        notification.setNotificationStatus(NotificationStatus.SENT);
        notification.setUpdatedAt(timeProvider.now());
        notificationHistoryRepository.save(notification);
        interfaceHistoryService.record(
            "IF-HOGE-FOO-002",
            InterfaceDirection.OUTBOUND,
            InterfaceStatus.SUCCESS,
            order.getPartnerOrderId(),
            "200",
            "foo status sent");
        log.info(
            "APP_BATCH_RECORD_FINISH function=fooStatusNotification orderId={}",
            order.getOrderId());
      }
      published++;
    }

    return published;
  }

  private void writeStatusFile(OrderHeaderEntity order, DeliveryStatusCurrentEntity current) {
    try {
      Path statusDir = Path.of(workerFileProperties.getFooStatusDir());
      Files.createDirectories(statusDir);
      String fileName = "FOO_ORDER_STATUS_" + timeProvider.now().format(FILE_TS) + "_001.dat";
      String line =
          String.join(
              ",",
              order.getPartnerOrderId(),
              current.getLatestStatusCode(),
              current.getLatestStatusAt().format(ISO),
              order.getCarrierCode().name());
      Files.writeString(
          statusDir.resolve(fileName), line + System.lineSeparator(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      log.error(
          "MONITORING_BATCH_ERROR function=fooStatusNotificationWrite message={}",
          exception.getMessage(),
          exception);
      throw new IllegalStateException(exception);
    }
  }
}
