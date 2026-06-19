package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Bar社向け出荷依頼の冪等性履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_bar_idempotency_history", schema = "orderhub")
public class BarIdempotencyHistoryEntity {

    /** 冪等キー。 */
    @Id
    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    /** 出荷依頼 ID。 */
    @Column(name = "shipment_request_id", nullable = false, length = 40)
    private String shipmentRequestId;

    /** 要求ハッシュ。 */
    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    /** Bar社出荷 ID。 */
    @Column(name = "bar_shipment_id", length = 64)
    private String barShipmentId;

    /** 作成日時。 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 冪等キーを返却する。
     *
     * @return 冪等キー
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * 冪等キーを設定する。
     *
     * @param idempotencyKey 冪等キー
     */
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

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
     * 要求ハッシュを返却する。
     *
     * @return 要求ハッシュ
     */
    public String getRequestHash() {
        return requestHash;
    }

    /**
     * 要求ハッシュを設定する。
     *
     * @param requestHash 要求ハッシュ
     */
    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    /**
     * Bar社出荷 ID を返却する。
     *
     * @return Bar社出荷 ID
     */
    public String getBarShipmentId() {
        return barShipmentId;
    }

    /**
     * Bar社出荷 ID を設定する。
     *
     * @param barShipmentId Bar社出荷 ID
     */
    public void setBarShipmentId(String barShipmentId) {
        this.barShipmentId = barShipmentId;
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
}
