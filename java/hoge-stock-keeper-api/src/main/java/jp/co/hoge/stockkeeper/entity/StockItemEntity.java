package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 在庫商品マスタを保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_stock_item")
public class StockItemEntity {

  /** 在庫 ID。 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stock_item_id", nullable = false)
  private Long stockItemId;

  /** 商品コード。 */
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 商品名。 */
  @Column(name = "item_name", nullable = false, length = 128)
  private String itemName;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** 保有在庫数。 */
  @Column(name = "on_hand_quantity", nullable = false)
  private int onHandQuantity;

  /** 引当済在庫数。 */
  @Column(name = "reserved_quantity", nullable = false)
  private int reservedQuantity;

  /** 利用可能在庫数。 */
  @Column(name = "available_quantity", nullable = false)
  private int availableQuantity;

  /** 温度帯。 */
  @Column(name = "temperature_zone", nullable = false, length = 16)
  private String temperatureZone;

  /**
   * 在庫 ID を返却する。
   *
   * @return 在庫 ID
   */
  public Long getStockItemId() {
    return stockItemId;
  }

  /**
   * 在庫 ID を設定する。
   *
   * @param stockItemId 在庫 ID
   */
  public void setStockItemId(Long stockItemId) {
    this.stockItemId = stockItemId;
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
   * 保有在庫数を返却する。
   *
   * @return 保有在庫数
   */
  public int getOnHandQuantity() {
    return onHandQuantity;
  }

  /**
   * 保有在庫数を設定する。
   *
   * @param onHandQuantity 保有在庫数
   */
  public void setOnHandQuantity(int onHandQuantity) {
    this.onHandQuantity = onHandQuantity;
  }

  /**
   * 引当済在庫数を返却する。
   *
   * @return 引当済在庫数
   */
  public int getReservedQuantity() {
    return reservedQuantity;
  }

  /**
   * 引当済在庫数を設定する。
   *
   * @param reservedQuantity 引当済在庫数
   */
  public void setReservedQuantity(int reservedQuantity) {
    this.reservedQuantity = reservedQuantity;
  }

  /**
   * 利用可能在庫数を返却する。
   *
   * @return 利用可能在庫数
   */
  public int getAvailableQuantity() {
    return availableQuantity;
  }

  /**
   * 利用可能在庫数を設定する。
   *
   * @param availableQuantity 利用可能在庫数
   */
  public void setAvailableQuantity(int availableQuantity) {
    this.availableQuantity = availableQuantity;
  }

  /**
   * 温度帯を返却する。
   *
   * @return 温度帯
   */
  public String getTemperatureZone() {
    return temperatureZone;
  }

  /**
   * 温度帯を設定する。
   *
   * @param temperatureZone 温度帯
   */
  public void setTemperatureZone(String temperatureZone) {
    this.temperatureZone = temperatureZone;
  }
}
