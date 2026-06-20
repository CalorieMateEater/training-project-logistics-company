package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 通知履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface NotificationHistoryRepository
    extends JpaRepository<NotificationHistoryEntity, String> {

  /**
   * 指定通知種別かつ指定状態の通知履歴を作成日時順に取得する。
   *
   * @param notificationType 通知種別
   * @param notificationStatus 通知状態
   * @return 通知履歴一覧
   */
  List<NotificationHistoryEntity> findByNotificationTypeAndNotificationStatusOrderByCreatedAtAsc(
      NotificationType notificationType, NotificationStatus notificationStatus);

  /**
   * 指定注文・通知種別・イベント種別の最初の通知履歴を取得する。
   *
   * @param orderId 注文 ID
   * @param notificationType 通知種別
   * @param eventType イベント種別
   * @return 最初の通知履歴
   */
  Optional<NotificationHistoryEntity>
      findFirstByOrderIdAndNotificationTypeAndEventTypeOrderByCreatedAtAsc(
          String orderId, NotificationType notificationType, String eventType);
}
