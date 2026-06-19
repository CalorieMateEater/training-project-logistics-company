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
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;

/**
 * 出荷依頼情報を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_shipment_request", schema = "orderhub")
public class ShipmentRequestEntity {

    /** 出荷依頼 ID。 */
    @Id
    @Column(name = "shipment_request_id", nullable = false, length = 40)
    private String shipmentRequestId;

    /** 注文 ID。 */
    @Column(name = "order_id", nullable = false, length = 32)
    private String orderId;

    /** 配送会社コード。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_code", nullable = false, length = 16)
    private CarrierCode carrierCode;

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

    /** ルーティングルール ID。 */
    @Column(name = "routing_rule_id", length = 64)
    private String routingRuleId;

    /** 出荷依頼状態。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_request_status", nullable = false, length = 32)
    private ShipmentRequestStatus shipmentRequestStatus;

    /** キュー投入日時。 */
    @Column(name = "queue_enqueued_at")
    private LocalDateTime queueEnqueuedAt;

    /** 次回送信予定日時。 */
    @Column(name = "next_request_after")
    private LocalDateTime nextRequestAfter;

    /** 出荷依頼送信日時。 */
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    /** Bar社受付日時。 */
    @Column(name = "bar_accepted_at")
    private LocalDateTime barAcceptedAt;

    /** Bar社受付番号。 */
    @Column(name = "bar_accept_no", length = 64)
    private String barAcceptNo;

    /**
     * 出荷依頼 ID を返却する。
     *
     * @return 出荷依頼 ID
     */
    public String getShipmentRequestId() {
        return shipmentRequestId;
    }

    /**
     * 出荷依頼 ID を設定する。
     *
     * @param shipmentRequestId 出荷依頼 ID
     */
    public void setShipmentRequestId(String shipmentRequestId) {
        this.shipmentRequestId = shipmentRequestId;
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
     * 配送会社コードを返却する。
     *
     * @return 配送会社コード
     */
    public CarrierCode getCarrierCode() {
        return carrierCode;
    }

    /**
     * 配送会社コードを設定する。
     *
     * @param carrierCode 配送会社コード
     */
    public void setCarrierCode(CarrierCode carrierCode) {
        this.carrierCode = carrierCode;
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
     * ルーティングルール ID を返却する。
     *
     * @return ルーティングルール ID
     */
    public String getRoutingRuleId() {
        return routingRuleId;
    }

    /**
     * ルーティングルール ID を設定する。
     *
     * @param routingRuleId ルーティングルール ID
     */
    public void setRoutingRuleId(String routingRuleId) {
        this.routingRuleId = routingRuleId;
    }

    /**
     * 出荷依頼状態を返却する。
     *
     * @return 出荷依頼状態
     */
    public ShipmentRequestStatus getShipmentRequestStatus() {
        return shipmentRequestStatus;
    }

    /**
     * 出荷依頼状態を設定する。
     *
     * @param shipmentRequestStatus 出荷依頼状態
     */
    public void setShipmentRequestStatus(ShipmentRequestStatus shipmentRequestStatus) {
        this.shipmentRequestStatus = shipmentRequestStatus;
    }

    /**
     * キュー投入日時を返却する。
     *
     * @return キュー投入日時
     */
    public LocalDateTime getQueueEnqueuedAt() {
        return queueEnqueuedAt;
    }

    /**
     * キュー投入日時を設定する。
     *
     * @param queueEnqueuedAt キュー投入日時
     */
    public void setQueueEnqueuedAt(LocalDateTime queueEnqueuedAt) {
        this.queueEnqueuedAt = queueEnqueuedAt;
    }

    /**
     * 次回送信予定日時を返却する。
     *
     * @return 次回送信予定日時
     */
    public LocalDateTime getNextRequestAfter() {
        return nextRequestAfter;
    }

    /**
     * 次回送信予定日時を設定する。
     *
     * @param nextRequestAfter 次回送信予定日時
     */
    public void setNextRequestAfter(LocalDateTime nextRequestAfter) {
        this.nextRequestAfter = nextRequestAfter;
    }

    /**
     * 出荷依頼送信日時を返却する。
     *
     * @return 出荷依頼送信日時
     */
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    /**
     * 出荷依頼送信日時を設定する。
     *
     * @param requestedAt 出荷依頼送信日時
     */
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    /**
     * Bar社受付日時を返却する。
     *
     * @return Bar社受付日時
     */
    public LocalDateTime getBarAcceptedAt() {
        return barAcceptedAt;
    }

    /**
     * Bar社受付日時を設定する。
     *
     * @param barAcceptedAt Bar社受付日時
     */
    public void setBarAcceptedAt(LocalDateTime barAcceptedAt) {
        this.barAcceptedAt = barAcceptedAt;
    }

    /**
     * Bar社受付番号を返却する。
     *
     * @return Bar社受付番号
     */
    public String getBarAcceptNo() {
        return barAcceptNo;
    }

    /**
     * Bar社受付番号を設定する。
     *
     * @param barAcceptNo Bar社受付番号
     */
    public void setBarAcceptNo(String barAcceptNo) {
        this.barAcceptNo = barAcceptNo;
    }
}
