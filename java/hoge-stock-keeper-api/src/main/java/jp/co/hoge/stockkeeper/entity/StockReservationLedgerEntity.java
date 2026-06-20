package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 在庫引当台帳を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_stock_reservation_ledger")
public class StockReservationLedgerEntity {

  /** 台帳 ID。 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stock_reservation_ledger_id", nullable = false)
  private Long stockReservationLedgerId;

  /** 引当 ID。 */
  @Column(name = "reservation_id", nullable = false, length = 64)
  private String reservationId;

  /** 注文 ID。 */
  @Column(name = "order_id", nullable = false, length = 32)
  private String orderId;

  /** 商品コード。 */
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** 要求数量。 */
  @Column(name = "requested_quantity", nullable = false)
  private int requestedQuantity;

  /** 引当数量。 */
  @Column(name = "reserved_quantity", nullable = false)
  private int reservedQuantity;

  /** 引当状態。 */
  @Column(name = "reservation_status", nullable = false, length = 32)
  private String reservationStatus;

  /**
   * 台帳 ID を返却する。
   *
   * @return 台帳 ID
   */
  public Long getStockReservationLedgerId() {
    return stockReservationLedgerId;
  }

  /**
   * 台帳 ID を設定する。
   *
   * @param stockReservationLedgerId 台帳 ID
   */
  public void setStockReservationLedgerId(Long stockReservationLedgerId) {
    this.stockReservationLedgerId = stockReservationLedgerId;
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
   * 要求数量を返却する。
   *
   * @return 要求数量
   */
  public int getRequestedQuantity() {
    return requestedQuantity;
  }

  /**
   * 要求数量を設定する。
   *
   * @param requestedQuantity 要求数量
   */
  public void setRequestedQuantity(int requestedQuantity) {
    this.requestedQuantity = requestedQuantity;
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
}
