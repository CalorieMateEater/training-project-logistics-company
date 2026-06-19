package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    /** 商品コード。 */
    @Id
    @Column(name = "item_code", nullable = false, length = 32)
    private String itemCode;

    /** 商品名。 */
    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    /** 利用可能在庫数。 */
    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    /** 温度帯。 */
    @Column(name = "temperature_zone", nullable = false, length = 16)
    private String temperatureZone;

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
