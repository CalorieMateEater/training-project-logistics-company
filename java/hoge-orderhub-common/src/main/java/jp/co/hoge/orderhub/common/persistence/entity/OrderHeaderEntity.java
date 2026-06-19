package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;

/**
 * 注文ヘッダを保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_order_header", schema = "orderhub")
public class OrderHeaderEntity {

    /** 注文 ID。 */
    @Id
    @Column(name = "order_id", nullable = false, length = 32)
    private String orderId;

    /** 連携先注文 ID。 */
    @Column(name = "partner_order_id", nullable = false, length = 32, unique = true)
    private String partnerOrderId;

    /** 連携先要求 ID。 */
    @Column(name = "partner_request_id", length = 32, unique = true)
    private String partnerRequestId;

    /** 注文受付元。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_source", nullable = false, length = 16)
    private OrderSource orderSource;

    /** 連携先優先度。 */
    @Column(name = "partner_priority_level", nullable = false)
    private int partnerPriorityLevel;

    /** 配送優先区分。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_priority_class", nullable = false, length = 16)
    private ShippingPriorityClass shippingPriorityClass;

    /** 顧客 ID。 */
    @Column(name = "customer_id", nullable = false, length = 32)
    private String customerId;

    /** 注文状態。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 32)
    private OrderStatus orderStatus;

    /** 出荷状態。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false, length = 32)
    private OrderStatus shipmentStatus;

    /** 割当配送会社コード。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_code", length = 16)
    private CarrierCode carrierCode;

    /** 配送先郵便番号。 */
    @Column(name = "delivery_zip_code", nullable = false, length = 16)
    private String deliveryZipCode;

    /** 配送先住所。 */
    @Column(name = "delivery_address", nullable = false, length = 256)
    private String deliveryAddress;

    /** 出荷解放日時。 */
    @Column(name = "shipping_release_at")
    private LocalDateTime shippingReleaseAt;

    /** 作成日時。 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 更新日時。 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
     * 連携先注文 ID を返却する。
     *
     * @return 連携先注文 ID
     */
    public String getPartnerOrderId() {
        return partnerOrderId;
    }

    /**
     * 連携先注文 ID を設定する。
     *
     * @param partnerOrderId 連携先注文 ID
     */
    public void setPartnerOrderId(String partnerOrderId) {
        this.partnerOrderId = partnerOrderId;
    }

    /**
     * 連携先要求 ID を返却する。
     *
     * @return 連携先要求 ID
     */
    public String getPartnerRequestId() {
        return partnerRequestId;
    }

    /**
     * 連携先要求 ID を設定する。
     *
     * @param partnerRequestId 連携先要求 ID
     */
    public void setPartnerRequestId(String partnerRequestId) {
        this.partnerRequestId = partnerRequestId;
    }

    /**
     * 注文受付元を返却する。
     *
     * @return 注文受付元
     */
    public OrderSource getOrderSource() {
        return orderSource;
    }

    /**
     * 注文受付元を設定する。
     *
     * @param orderSource 注文受付元
     */
    public void setOrderSource(OrderSource orderSource) {
        this.orderSource = orderSource;
    }

    /**
     * 連携先優先度を返却する。
     *
     * @return 連携先優先度
     */
    public int getPartnerPriorityLevel() {
        return partnerPriorityLevel;
    }

    /**
     * 連携先優先度を設定する。
     *
     * @param partnerPriorityLevel 連携先優先度
     */
    public void setPartnerPriorityLevel(int partnerPriorityLevel) {
        this.partnerPriorityLevel = partnerPriorityLevel;
    }

    /**
     * 配送優先区分を返却する。
     *
     * @return 配送優先区分
     */
    public ShippingPriorityClass getShippingPriorityClass() {
        return shippingPriorityClass;
    }

    /**
     * 配送優先区分を設定する。
     *
     * @param shippingPriorityClass 配送優先区分
     */
    public void setShippingPriorityClass(ShippingPriorityClass shippingPriorityClass) {
        this.shippingPriorityClass = shippingPriorityClass;
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
     * 注文状態を返却する。
     *
     * @return 注文状態
     */
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    /**
     * 注文状態を設定する。
     *
     * @param orderStatus 注文状態
     */
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * 出荷状態を返却する。
     *
     * @return 出荷状態
     */
    public OrderStatus getShipmentStatus() {
        return shipmentStatus;
    }

    /**
     * 出荷状態を設定する。
     *
     * @param shipmentStatus 出荷状態
     */
    public void setShipmentStatus(OrderStatus shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    /**
     * 割当配送会社コードを返却する。
     *
     * @return 配送会社コード
     */
    public CarrierCode getCarrierCode() {
        return carrierCode;
    }

    /**
     * 割当配送会社コードを設定する。
     *
     * @param carrierCode 配送会社コード
     */
    public void setCarrierCode(CarrierCode carrierCode) {
        this.carrierCode = carrierCode;
    }

    /**
     * 配送先郵便番号を返却する。
     *
     * @return 配送先郵便番号
     */
    public String getDeliveryZipCode() {
        return deliveryZipCode;
    }

    /**
     * 配送先郵便番号を設定する。
     *
     * @param deliveryZipCode 配送先郵便番号
     */
    public void setDeliveryZipCode(String deliveryZipCode) {
        this.deliveryZipCode = deliveryZipCode;
    }

    /**
     * 配送先住所を返却する。
     *
     * @return 配送先住所
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * 配送先住所を設定する。
     *
     * @param deliveryAddress 配送先住所
     */
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    /**
     * 出荷解放日時を返却する。
     *
     * @return 出荷解放日時
     */
    public LocalDateTime getShippingReleaseAt() {
        return shippingReleaseAt;
    }

    /**
     * 出荷解放日時を設定する。
     *
     * @param shippingReleaseAt 出荷解放日時
     */
    public void setShippingReleaseAt(LocalDateTime shippingReleaseAt) {
        this.shippingReleaseAt = shippingReleaseAt;
    }

    /**
     * 作成日時を返却する。
     *
     * @return 作成日時
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 作成日時を設定する。
     *
     * @param createdAt 作成日時
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 更新日時を返却する。
     *
     * @return 更新日時
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 更新日時を設定する。
     *
     * @param updatedAt 更新日時
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
