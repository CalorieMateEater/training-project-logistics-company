package jp.co.hoge.orderhubbatch.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.persistence.entity.ArchiveExecutionEntity;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.InterfaceHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.repository.ArchiveExecutionRepository;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.InterfaceHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubbatch.config.BatchFileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 日次アーカイブ処理を実行するサービス。 関連処理機能ID: PGD-007
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArchiveService {
  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** 配送状態履歴リポジトリ。 */
  private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;

  /** 通知履歴リポジトリ。 */
  private final NotificationHistoryRepository notificationHistoryRepository;

  /** IF履歴リポジトリ。 */
  private final InterfaceHistoryRepository interfaceHistoryRepository;

  /** アーカイブ実行履歴リポジトリ。 */
  private final ArchiveExecutionRepository archiveExecutionRepository;

  /** アーカイブ実行 ID 採番サービス。 */
  private final IdFactory idFactory;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /** バッチファイル設定。 */
  private final BatchFileProperties batchFileProperties;

  /**
   * 完了済み注文をアーカイブ出力する。
   *
   * @return アーカイブ実行 ID
   */
  public String archiveCompletedOrders() {
    log.info("APP_BATCH_START function=dailyArchive");
    var startedAt = timeProvider.now();
    List<OrderHeaderEntity> targets =
        orderHeaderRepository.findAll().stream()
            .filter(
                order ->
                    order.getOrderStatus() == OrderStatus.COMPLETED
                        || order.getOrderStatus() == OrderStatus.CANCELLED
                        || order.getOrderStatus() == OrderStatus.EXCEPTION)
            .collect(Collectors.toList());

    String archiveExecutionId = idFactory.archiveExecutionId();
    Path archiveDir = Path.of(batchFileProperties.getArchiveDir());
    Path output =
        archiveDir.resolve(
            "orderhub-archive-"
                + startedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".csv");

    try {
      Files.createDirectories(archiveDir);
      String content = buildArchiveContent(targets);
      Files.writeString(output, content, StandardCharsets.UTF_8);

      ArchiveExecutionEntity execution = new ArchiveExecutionEntity();
      execution.setArchiveExecutionId(archiveExecutionId);
      execution.setStartedAt(startedAt);
      execution.setFinishedAt(timeProvider.now());
      execution.setArchivedOrders(targets.size());
      execution.setOutputPath(output.toString());
      execution.setResultStatus("SUCCESS");
      archiveExecutionRepository.save(execution);
      log.info(
          "APP_BATCH_FINISH function=dailyArchive archiveExecutionId={} archivedOrders={}",
          archiveExecutionId,
          targets.size());
      return archiveExecutionId;
    } catch (IOException exception) {
      ArchiveExecutionEntity execution = new ArchiveExecutionEntity();
      execution.setArchiveExecutionId(archiveExecutionId);
      execution.setStartedAt(startedAt);
      execution.setFinishedAt(timeProvider.now());
      execution.setArchivedOrders(targets.size());
      execution.setOutputPath(output.toString());
      execution.setResultStatus("FAILED");
      archiveExecutionRepository.save(execution);
      log.error(
          "MONITORING_BATCH_ERROR function=dailyArchive archiveExecutionId={} message={}",
          archiveExecutionId,
          exception.getMessage(),
          exception);
      throw new IllegalStateException(exception);
    }
  }

  private String buildArchiveContent(List<OrderHeaderEntity> targets) {
    Set<String> targetOrderIds =
        targets.stream().map(OrderHeaderEntity::getOrderId).collect(Collectors.toSet());
    Set<String> requestKeys = new HashSet<>(targetOrderIds);
    targets.forEach(
        order -> {
          if (order.getPartnerOrderId() != null) {
            requestKeys.add(order.getPartnerOrderId());
          }
          if (order.getPartnerRequestId() != null) {
            requestKeys.add(order.getPartnerRequestId());
          }
        });

    List<String> rows = new ArrayList<>();
    targets.forEach(order -> rows.add(orderArchiveRow(order)));
    deliveryStatusHistoryRepository.findAll().stream()
        .filter(history -> targetOrderIds.contains(history.getOrderId()))
        .map(this::deliveryHistoryArchiveRow)
        .forEach(rows::add);
    notificationHistoryRepository.findAll().stream()
        .filter(notification -> targetOrderIds.contains(notification.getOrderId()))
        .map(this::notificationHistoryArchiveRow)
        .forEach(rows::add);
    interfaceHistoryRepository.findAll().stream()
        .filter(history -> requestKeys.contains(history.getRequestKey()))
        .map(this::interfaceHistoryArchiveRow)
        .forEach(rows::add);
    return String.join(System.lineSeparator(), rows);
  }

  private String orderArchiveRow(OrderHeaderEntity order) {
    return csv(
        "ORDER",
        order.getOrderId(),
        order.getPartnerOrderId(),
        order.getOrderSource().name(),
        order.getOrderStatus().name(),
        order.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }

  private String deliveryHistoryArchiveRow(DeliveryStatusHistoryEntity history) {
    return csv(
        "DELIVERY_HISTORY",
        history.getOrderId(),
        String.valueOf(history.getStatusSeq()),
        history.getStatusCode(),
        history.getStatusName(),
        history.getEventOccurredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        history.getReasonCategory());
  }

  private String notificationHistoryArchiveRow(NotificationHistoryEntity notification) {
    return csv(
        "NOTIFICATION_HISTORY",
        notification.getOrderId(),
        notification.getNotificationId(),
        notification.getNotificationType().name(),
        notification.getNotificationStatus().name(),
        notification.getDestination(),
        notification.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }

  private String interfaceHistoryArchiveRow(InterfaceHistoryEntity history) {
    return csv(
        "INTERFACE_HISTORY",
        history.getInterfaceHistoryId(),
        history.getIfId(),
        history.getDirection().name(),
        history.getResultStatus().name(),
        history.getRequestKey(),
        history.getResultCode(),
        history.getRequestedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }

  private String csv(String... values) {
    return java.util.Arrays.stream(values)
        .map(value -> value == null ? "" : value.replace(",", " "))
        .collect(Collectors.joining(","));
  }
}
