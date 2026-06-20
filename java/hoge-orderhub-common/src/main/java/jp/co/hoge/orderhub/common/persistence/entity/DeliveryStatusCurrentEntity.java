package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 注文ごとの最新配送状態を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_delivery_status_current", schema = "orderhub")
public class DeliveryStatusCurrentEntity {

  /** 注文 ID。 */
  @Id
  @Column(name = "order_id", nullable = false, length = 32)
  private String orderId;

  /** 最新状態コード。 */
  @Column(name = "latest_status_code", nullable = false, length = 32)
  private String latestStatusCode;

  /** 最新状態名称。 */
  @Column(name = "latest_status_name", nullable = false, length = 64)
  private String latestStatusName;

  /** 表示用最新状態名称。 */
  @Column(name = "latest_display_status_name", length = 64)
  private String latestDisplayStatusName;

  /** 最新状態連番。 */
  @Column(name = "latest_status_seq", nullable = false)
  private int latestStatusSeq;

  /** 最新理由分類。 */
  @Column(name = "latest_reason_category", length = 64)
  private String latestReasonCategory;

  /** 住所補正有無。 */
  @Column(name = "address_corrected")
  private Boolean addressCorrected;

  /** 住所補正レベル。 */
  @Column(name = "address_correction_level", length = 32)
  private String addressCorrectionLevel;

  /** 最新状態発生日時。 */
  @Column(name = "latest_status_at", nullable = false)
  private LocalDateTime latestStatusAt;

  /** 最終受信日時。 */
  @Column(name = "last_received_at", nullable = false)
  private LocalDateTime lastReceivedAt;

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
   * 最新状態コードを返却する。
   *
   * @return 最新状態コード
   */
  public String getLatestStatusCode() {
    return latestStatusCode;
  }

  /**
   * 最新状態コードを設定する。
   *
   * @param latestStatusCode 最新状態コード
   */
  public void setLatestStatusCode(String latestStatusCode) {
    this.latestStatusCode = latestStatusCode;
  }

  /**
   * 最新状態名称を返却する。
   *
   * @return 最新状態名称
   */
  public String getLatestStatusName() {
    return latestStatusName;
  }

  /**
   * 最新状態名称を設定する。
   *
   * @param latestStatusName 最新状態名称
   */
  public void setLatestStatusName(String latestStatusName) {
    this.latestStatusName = latestStatusName;
  }

  /**
   * 表示用最新状態名称を返却する。
   *
   * @return 表示用最新状態名称
   */
  public String getLatestDisplayStatusName() {
    return latestDisplayStatusName;
  }

  /**
   * 表示用最新状態名称を設定する。
   *
   * @param latestDisplayStatusName 表示用最新状態名称
   */
  public void setLatestDisplayStatusName(String latestDisplayStatusName) {
    this.latestDisplayStatusName = latestDisplayStatusName;
  }

  /**
   * 最新状態連番を返却する。
   *
   * @return 最新状態連番
   */
  public int getLatestStatusSeq() {
    return latestStatusSeq;
  }

  /**
   * 最新状態連番を設定する。
   *
   * @param latestStatusSeq 最新状態連番
   */
  public void setLatestStatusSeq(int latestStatusSeq) {
    this.latestStatusSeq = latestStatusSeq;
  }

  /**
   * 最新理由分類を返却する。
   *
   * @return 最新理由分類
   */
  public String getLatestReasonCategory() {
    return latestReasonCategory;
  }

  /**
   * 最新理由分類を設定する。
   *
   * @param latestReasonCategory 最新理由分類
   */
  public void setLatestReasonCategory(String latestReasonCategory) {
    this.latestReasonCategory = latestReasonCategory;
  }

  /**
   * 住所補正有無を返却する。
   *
   * @return 住所補正有無
   */
  public Boolean getAddressCorrected() {
    return addressCorrected;
  }

  /**
   * 住所補正有無を設定する。
   *
   * @param addressCorrected 住所補正有無
   */
  public void setAddressCorrected(Boolean addressCorrected) {
    this.addressCorrected = addressCorrected;
  }

  /**
   * 住所補正レベルを返却する。
   *
   * @return 住所補正レベル
   */
  public String getAddressCorrectionLevel() {
    return addressCorrectionLevel;
  }

  /**
   * 住所補正レベルを設定する。
   *
   * @param addressCorrectionLevel 住所補正レベル
   */
  public void setAddressCorrectionLevel(String addressCorrectionLevel) {
    this.addressCorrectionLevel = addressCorrectionLevel;
  }

  /**
   * 最新状態発生日時を返却する。
   *
   * @return 最新状態発生日時
   */
  public LocalDateTime getLatestStatusAt() {
    return latestStatusAt;
  }

  /**
   * 最新状態発生日時を設定する。
   *
   * @param latestStatusAt 最新状態発生日時
   */
  public void setLatestStatusAt(LocalDateTime latestStatusAt) {
    this.latestStatusAt = latestStatusAt;
  }

  /**
   * 最終受信日時を返却する。
   *
   * @return 最終受信日時
   */
  public LocalDateTime getLastReceivedAt() {
    return lastReceivedAt;
  }

  /**
   * 最終受信日時を設定する。
   *
   * @param lastReceivedAt 最終受信日時
   */
  public void setLastReceivedAt(LocalDateTime lastReceivedAt) {
    this.lastReceivedAt = lastReceivedAt;
  }
}
