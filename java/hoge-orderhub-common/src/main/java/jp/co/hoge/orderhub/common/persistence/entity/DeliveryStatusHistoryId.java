package jp.co.hoge.orderhub.common.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 配送状態履歴の複合キー。
 *
 * @author Takuya Yamamoto
 */
public class DeliveryStatusHistoryId implements Serializable {

  /** 注文 ID。 */
  private String orderId;

  /** ステータス連番。 */
  private int statusSeq;

  /** JPA 用デフォルトコンストラクタ。 */
  public DeliveryStatusHistoryId() {}

  /**
   * 複合キーを生成する。
   *
   * @param orderId 注文 ID
   * @param statusSeq ステータス連番
   */
  public DeliveryStatusHistoryId(String orderId, int statusSeq) {
    this.orderId = orderId;
    this.statusSeq = statusSeq;
  }

  /**
   * 同一キーかどうかを判定する。
   *
   * @param other 比較対象
   * @return 同一キーの場合 true
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof DeliveryStatusHistoryId that)) {
      return false;
    }
    return statusSeq == that.statusSeq && Objects.equals(orderId, that.orderId);
  }

  /**
   * ハッシュコードを返却する。
   *
   * @return ハッシュコード
   */
  @Override
  public int hashCode() {
    return Objects.hash(orderId, statusSeq);
  }
}
