package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;

/**
 * 外部通知履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "th_notification_history", schema = "orderhub")
public class NotificationHistoryEntity {

  /** 通知 ID。 */
  @Id
  @Column(name = "notification_id", nullable = false, length = 40)
  private String notificationId;

  /** 注文 ID。 */
  @Column(name = "order_id", nullable = false, length = 32)
  private String orderId;

  /** 通知種別。 */
  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 32)
  private NotificationType notificationType;

  /** 通知状態。 */
  @Enumerated(EnumType.STRING)
  @Column(name = "notification_status", nullable = false, length = 16)
  private NotificationStatus notificationStatus;

  /** ペイロード要約。 */
  @Column(name = "payload_summary", length = 512)
  private String payloadSummary;

  /** 通知キー。 */
  @Column(name = "notification_key", length = 128)
  private String notificationKey;

  /** イベント種別。 */
  @Column(name = "event_type", length = 64)
  private String eventType;

  /** 参照元通知 ID。 */
  @Column(name = "reference_notification_id", length = 40)
  private String referenceNotificationId;

  /** 表示用状態名称。 */
  @Column(name = "display_status_name", length = 64)
  private String displayStatusName;

  /** 通知先。 */
  @Column(name = "destination", length = 128)
  private String destination;

  /** 作成日時。 */
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  /** 更新日時。 */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * 通知 ID を返却する。
   *
   * @return 通知 ID
   */
  public String getNotificationId() {
    return notificationId;
  }

  /**
   * 通知 ID を設定する。
   *
   * @param notificationId 通知 ID
   */
  public void setNotificationId(String notificationId) {
    this.notificationId = notificationId;
  }

  /**
   * 注文 ID を返却する。
   *
   * @return 注文 ID
   */
  public String getOrderId() {
    return orderId;
  }

  /**
   * 注文 ID を設定する。
   *
   * @param orderId 注文 ID
   */
  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  /**
   * 通知種別を返却する。
   *
   * @return 通知種別
   */
  public NotificationType getNotificationType() {
    return notificationType;
  }

  /**
   * 通知種別を設定する。
   *
   * @param notificationType 通知種別
   */
  public void setNotificationType(NotificationType notificationType) {
    this.notificationType = notificationType;
  }

  /**
   * 通知状態を返却する。
   *
   * @return 通知状態
   */
  public NotificationStatus getNotificationStatus() {
    return notificationStatus;
  }

  /**
   * 通知状態を設定する。
   *
   * @param notificationStatus 通知状態
   */
  public void setNotificationStatus(NotificationStatus notificationStatus) {
    this.notificationStatus = notificationStatus;
  }

  /**
   * ペイロード要約を返却する。
   *
   * @return ペイロード要約
   */
  public String getPayloadSummary() {
    return payloadSummary;
  }

  /**
   * ペイロード要約を設定する。
   *
   * @param payloadSummary ペイロード要約
   */
  public void setPayloadSummary(String payloadSummary) {
    this.payloadSummary = payloadSummary;
  }

  /**
   * 通知キーを返却する。
   *
   * @return 通知キー
   */
  public String getNotificationKey() {
    return notificationKey;
  }

  /**
   * 通知キーを設定する。
   *
   * @param notificationKey 通知キー
   */
  public void setNotificationKey(String notificationKey) {
    this.notificationKey = notificationKey;
  }

  /**
   * イベント種別を返却する。
   *
   * @return イベント種別
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * イベント種別を設定する。
   *
   * @param eventType イベント種別
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  /**
   * 参照元通知 ID を返却する。
   *
   * @return 参照元通知 ID
   */
  public String getReferenceNotificationId() {
    return referenceNotificationId;
  }

  /**
   * 参照元通知 ID を設定する。
   *
   * @param referenceNotificationId 参照元通知 ID
   */
  public void setReferenceNotificationId(String referenceNotificationId) {
    this.referenceNotificationId = referenceNotificationId;
  }

  /**
   * 表示用状態名称を返却する。
   *
   * @return 表示用状態名称
   */
  public String getDisplayStatusName() {
    return displayStatusName;
  }

  /**
   * 表示用状態名称を設定する。
   *
   * @param displayStatusName 表示用状態名称
   */
  public void setDisplayStatusName(String displayStatusName) {
    this.displayStatusName = displayStatusName;
  }

  /**
   * 通知先を返却する。
   *
   * @return 通知先
   */
  public String getDestination() {
    return destination;
  }

  /**
   * 通知先を設定する。
   *
   * @param destination 通知先
   */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /**
   * 作成日時を返却する。
   *
   * @return 作成日時
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * 作成日時を設定する。
   *
   * @param createdAt 作成日時
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * 更新日時を返却する。
   *
   * @return 更新日時
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * 更新日時を設定する。
   *
   * @param updatedAt 更新日時
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
