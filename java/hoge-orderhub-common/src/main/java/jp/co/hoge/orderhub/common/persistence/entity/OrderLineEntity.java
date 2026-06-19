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
    @Column(name = "item_name", length = 128)
    private String itemName;

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
}
