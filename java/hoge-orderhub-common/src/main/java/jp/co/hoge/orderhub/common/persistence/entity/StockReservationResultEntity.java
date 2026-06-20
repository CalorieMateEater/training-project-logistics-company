package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 在庫引当結果を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@IdClass(StockReservationResultId.class)
@Table(name = "t_stock_reservation_result", schema = "orderhub")
public class StockReservationResultEntity {

  /** 注文 ID。 */
  @Id
  @Column(name = "order_id", nullable = false, length = 32)
  private String orderId;

  /** 注文明細番号。 */
  @Id
  @Column(name = "order_line_no", nullable = false)
  private int orderLineNo;

  /** 商品コード。 */
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 引当 ID。 */
  @Column(name = "reservation_id", nullable = false, length = 64)
  private String reservationId;

  /** 引当状態。 */
  @Column(name = "reservation_status", nullable = false, length = 32)
  private String reservationStatus;

  /** 引当数量。 */
  @Column(name = "reserved_quantity", nullable = false)
  private int reservedQuantity;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", length = 32)
  private String warehouseLocationCode;

  /** 引当解除数量。 */
  @Column(name = "released_quantity", nullable = false)
  private int releasedQuantity;

  /** 出荷確定数量。 */
  @Column(name = "shipped_confirmed_quantity", nullable = false)
  private int shippedConfirmedQuantity;

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
   * 注文明細番号を返却する。
   *
   * @return 注文明細番号
   */
  public int getOrderLineNo() {
    return orderLineNo;
  }

  /**
   * 注文明細番号を設定する。
   *
   * @param orderLineNo 注文明細番号
   */
  public void setOrderLineNo(int orderLineNo) {
    this.orderLineNo = orderLineNo;
  }

  /**
   * 商品コードを返却する。
   *
   * @return 商品コード
   */
  public String getItemCode() {
    return itemCode;
  }

  /**
   * 商品コードを設定する。
   *
   * @param itemCode 商品コード
   */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /**
   * 引当 ID を返却する。
   *
   * @return 引当 ID
   */
  public String getReservationId() {
    return reservationId;
  }

  /**
   * 引当 ID を設定する。
   *
   * @param reservationId 引当 ID
   */
  public void setReservationId(String reservationId) {
    this.reservationId = reservationId;
  }

  /**
   * 引当状態を返却する。
   *
   * @return 引当状態
   */
  public String getReservationStatus() {
    return reservationStatus;
  }

  /**
   * 引当状態を設定する。
   *
   * @param reservationStatus 引当状態
   */
  public void setReservationStatus(String reservationStatus) {
    this.reservationStatus = reservationStatus;
  }

  /**
   * 引当数量を返却する。
   *
   * @return 引当数量
   */
  public int getReservedQuantity() {
    return reservedQuantity;
  }

  /**
   * 引当数量を設定する。
   *
   * @param reservedQuantity 引当数量
   */
  public void setReservedQuantity(int reservedQuantity) {
    this.reservedQuantity = reservedQuantity;
  }

  /**
   * 倉庫場所コードを返却する。
   *
   * @return 倉庫場所コード
   */
  public String getWarehouseLocationCode() {
    return warehouseLocationCode;
  }

  /**
   * 倉庫場所コードを設定する。
   *
   * @param warehouseLocationCode 倉庫場所コード
   */
  public void setWarehouseLocationCode(String warehouseLocationCode) {
    this.warehouseLocationCode = warehouseLocationCode;
  }

  /**
   * 引当解除数量を返却する。
   *
   * @return 引当解除数量
   */
  public int getReleasedQuantity() {
    return releasedQuantity;
  }

  /**
   * 引当解除数量を設定する。
   *
   * @param releasedQuantity 引当解除数量
   */
  public void setReleasedQuantity(int releasedQuantity) {
    this.releasedQuantity = releasedQuantity;
  }

  /**
   * 出荷確定数量を返却する。
   *
   * @return 出荷確定数量
   */
  public int getShippedConfirmedQuantity() {
    return shippedConfirmedQuantity;
  }

  /**
   * 出荷確定数量を設定する。
   *
   * @param shippedConfirmedQuantity 出荷確定数量
   */
  public void setShippedConfirmedQuantity(int shippedConfirmedQuantity) {
    this.shippedConfirmedQuantity = shippedConfirmedQuantity;
  }
}
