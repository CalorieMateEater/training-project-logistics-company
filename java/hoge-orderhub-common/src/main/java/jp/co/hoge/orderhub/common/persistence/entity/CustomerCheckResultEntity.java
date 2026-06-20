package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 顧客確認結果を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_customer_check_result", schema = "orderhub")
public class CustomerCheckResultEntity {

  /** 注文 ID。 */
  @Id
  @Column(name = "order_id", nullable = false, length = 32)
  private String orderId;

  /** 顧客 ID。 */
  @Column(name = "customer_id", nullable = false, length = 32)
  private String customerId;

  /** 確認結果状態。 */
  @Column(name = "check_status", nullable = false, length = 32)
  private String checkStatus;

  /** 会員ランク。 */
  @Column(name = "member_rank", length = 32)
  private String memberRank;

  /** 確認日時。 */
  @Column(name = "checked_at", nullable = false)
  private LocalDateTime checkedAt;

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
   * 顧客 ID を返却する。
   *
   * @return 顧客 ID
   */
  public String getCustomerId() {
    return customerId;
  }

  /**
   * 顧客 ID を設定する。
   *
   * @param customerId 顧客 ID
   */
  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  /**
   * 確認結果状態を返却する。
   *
   * @return 確認結果状態
   */
  public String getCheckStatus() {
    return checkStatus;
  }

  /**
   * 確認結果状態を設定する。
   *
   * @param checkStatus 確認結果状態
   */
  public void setCheckStatus(String checkStatus) {
    this.checkStatus = checkStatus;
  }

  /**
   * 会員ランクを返却する。
   *
   * @return 会員ランク
   */
  public String getMemberRank() {
    return memberRank;
  }

  /**
   * 会員ランクを設定する。
   *
   * @param memberRank 会員ランク
   */
  public void setMemberRank(String memberRank) {
    this.memberRank = memberRank;
  }

  /**
   * 確認日時を返却する。
   *
   * @return 確認日時
   */
  public LocalDateTime getCheckedAt() {
    return checkedAt;
  }

  /**
   * 確認日時を設定する。
   *
   * @param checkedAt 確認日時
   */
  public void setCheckedAt(LocalDateTime checkedAt) {
    this.checkedAt = checkedAt;
  }
}
