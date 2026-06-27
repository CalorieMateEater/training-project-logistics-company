package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 注文明細を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@IdClass(OrderLineId.class)
@Table(name = "t_order_line", schema = "orderhub")
public class OrderLineEntity {

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

  /** 注文数量。 */
  @Column(name = "quantity", nullable = false)
  private int quantity;

  /** 商品名。 */
  @Column(name = "item_name_snapshot", length = 128)
  private String itemName;

  /** 税抜単価。 */
  @Column(name = "unit_price_excluding_tax", nullable = false)
  private int unitPriceExcludingTax;

  /** 消費税率。 */
  @Column(name = "tax_rate", nullable = false)
  private int taxRate;

  /** 明細税抜小計。 */
  @Column(name = "line_subtotal_excluding_tax", nullable = false)
  private int lineSubtotalExcludingTax;

  /** 明細消費税額。 */
  @Column(name = "line_tax_amount", nullable = false)
  private int lineTaxAmount;

  /** 単位重量スナップショット。 */
  @Column(name = "unit_weight_gram_snapshot", nullable = false)
  private int unitWeightGramSnapshot;

  /** 温度帯スナップショット。 */
  @Column(name = "temperature_zone_snapshot", nullable = false, length = 16)
  private String temperatureZoneSnapshot;

  /** サイズ区分スナップショット。 */
  @Column(name = "size_type_snapshot", nullable = false, length = 16)
  private String sizeTypeSnapshot;

  /** 出荷元倉庫場所コード。 */
  @Column(name = "source_warehouse_location_code", length = 32)
  private String sourceWarehouseLocationCode;

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
   * 注文数量を返却する。
   *
   * @return 注文数量
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * 注文数量を設定する。
   *
   * @param quantity 注文数量
   */
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  /**
   * 商品名を返却する。
   *
   * @return 商品名
   */
  public String getItemName() {
    return itemName;
  }

  /**
   * 商品名を設定する。
   *
   * @param itemName 商品名
   */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /**
   * 税抜単価を返却する。
   *
   * @return 税抜単価
   */
  public int getUnitPriceExcludingTax() {
    return unitPriceExcludingTax;
  }

  /**
   * 税抜単価を設定する。
   *
   * @param unitPriceExcludingTax 税抜単価
   */
  public void setUnitPriceExcludingTax(int unitPriceExcludingTax) {
    this.unitPriceExcludingTax = unitPriceExcludingTax;
  }

  /**
   * 消費税率を返却する。
   *
   * @return 消費税率
   */
  public int getTaxRate() {
    return taxRate;
  }

  /**
   * 消費税率を設定する。
   *
   * @param taxRate 消費税率
   */
  public void setTaxRate(int taxRate) {
    this.taxRate = taxRate;
  }

  /**
   * 明細税抜小計を返却する。
   *
   * @return 明細税抜小計
   */
  public int getLineSubtotalExcludingTax() {
    return lineSubtotalExcludingTax;
  }

  /**
   * 明細税抜小計を設定する。
   *
   * @param lineSubtotalExcludingTax 明細税抜小計
   */
  public void setLineSubtotalExcludingTax(int lineSubtotalExcludingTax) {
    this.lineSubtotalExcludingTax = lineSubtotalExcludingTax;
  }

  /**
   * 明細消費税額を返却する。
   *
   * @return 明細消費税額
   */
  public int getLineTaxAmount() {
    return lineTaxAmount;
  }

  /**
   * 明細消費税額を設定する。
   *
   * @param lineTaxAmount 明細消費税額
   */
  public void setLineTaxAmount(int lineTaxAmount) {
    this.lineTaxAmount = lineTaxAmount;
  }

  /**
   * 単位重量スナップショットを返却する。
   *
   * @return 単位重量スナップショット
   */
  public int getUnitWeightGramSnapshot() {
    return unitWeightGramSnapshot;
  }

  /**
   * 単位重量スナップショットを設定する。
   *
   * @param unitWeightGramSnapshot 単位重量スナップショット
   */
  public void setUnitWeightGramSnapshot(int unitWeightGramSnapshot) {
    this.unitWeightGramSnapshot = unitWeightGramSnapshot;
  }

  /**
   * 温度帯スナップショットを返却する。
   *
   * @return 温度帯スナップショット
   */
  public String getTemperatureZoneSnapshot() {
    return temperatureZoneSnapshot;
  }

  /**
   * 温度帯スナップショットを設定する。
   *
   * @param temperatureZoneSnapshot 温度帯スナップショット
   */
  public void setTemperatureZoneSnapshot(String temperatureZoneSnapshot) {
    this.temperatureZoneSnapshot = temperatureZoneSnapshot;
  }

  /**
   * サイズ区分スナップショットを返却する。
   *
   * @return サイズ区分スナップショット
   */
  public String getSizeTypeSnapshot() {
    return sizeTypeSnapshot;
  }

  /**
   * サイズ区分スナップショットを設定する。
   *
   * @param sizeTypeSnapshot サイズ区分スナップショット
   */
  public void setSizeTypeSnapshot(String sizeTypeSnapshot) {
    this.sizeTypeSnapshot = sizeTypeSnapshot;
  }

  /**
   * 出荷元倉庫場所コードを返却する。
   *
   * @return 出荷元倉庫場所コード
   */
  public String getSourceWarehouseLocationCode() {
    return sourceWarehouseLocationCode;
  }

  /**
   * 出荷元倉庫場所コードを設定する。
   *
   * @param sourceWarehouseLocationCode 出荷元倉庫場所コード
   */
  public void setSourceWarehouseLocationCode(String sourceWarehouseLocationCode) {
    this.sourceWarehouseLocationCode = sourceWarehouseLocationCode;
  }
}
