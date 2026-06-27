package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 在庫商品マスタを保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "tm_stock_item")
@Getter
@Setter
public class StockItemEntity {

  /** 商品コード。 */
  @Id
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 商品名。 */
  @Column(name = "item_name", nullable = false, length = 128)
  private String itemName;

  /** 温度帯。 */
  @Column(name = "temperature_zone", nullable = false, length = 16)
  private String temperatureZone;

  /** サイズ区分。 */
  @Column(name = "size_type", nullable = false, length = 16)
  private String sizeType;

  /** 単位重量グラム。 */
  @Column(name = "unit_weight_gram", nullable = false)
  private int unitWeightGram;

  /** 有効フラグ。 */
  @Column(name = "active_flag", nullable = false)
  private boolean activeFlag;
}
