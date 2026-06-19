package jp.co.hoge.orderhub.common.mapper.model;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;

/**
 * 通知履歴登録時の入力値を保持するレコード。
 *
 * @param notificationId 通知 ID
 * @param orderId 注文 ID
 * @param notificationType 通知種別
 * @param notificationStatus 通知状態
 * @param payloadSummary 送信内容サマリ
 * @param notificationKey 通知キー
 * @param eventType イベント種別
 * @param referenceNotificationId 参照元通知 ID
 * @param displayStatusName 表示用ステータス名
 * @param destination 送信先
 * @param createdAt 作成日時
 * @param updatedAt 更新日時
 * @author Takuya Yamamoto
 */
public record NotificationHistoryRecord(
        String notificationId,
        String orderId,
        NotificationType notificationType,
        NotificationStatus notificationStatus,
        String payloadSummary,
        String notificationKey,
        String eventType,
        String referenceNotificationId,
        String displayStatusName,
        String destination,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
