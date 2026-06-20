package jp.co.hoge.orderhub.common.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 注文明細の複合キー。
 *
 * @author Takuya Yamamoto
 */
public class OrderLineId implements Serializable {

  /** 注文 ID。 */
  private String orderId;

  /** 注文明細番号。 */
  private int orderLineNo;

  /** JPA 用デフォルトコンストラクタ。 */
  public OrderLineId() {}

  /**
   * 複合キーを生成する。
   *
   * @param orderId 注文 ID
   * @param orderLineNo 注文明細番号
   */
  public OrderLineId(String orderId, int orderLineNo) {
    this.orderId = orderId;
    this.orderLineNo = orderLineNo;
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
    if (!(other instanceof OrderLineId that)) {
      return false;
    }
    return orderLineNo == that.orderLineNo && Objects.equals(orderId, that.orderId);
  }

  /**
   * ハッシュコードを返却する。
   *
   * @return ハッシュコード
   */
  @Override
  public int hashCode() {
    return Objects.hash(orderId, orderLineNo);
  }
}
