package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 配送状態の時系列履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@IdClass(DeliveryStatusHistoryId.class)
@Table(name = "th_delivery_status_history", schema = "orderhub")
public class DeliveryStatusHistoryEntity {

    /** 注文 ID。 */
    @Id
    @Column(name = "order_id", nullable = false, length = 32)
    private String orderId;

    /** ステータス連番。 */
    @Id
    @Column(name = "status_seq", nullable = false)
    private int statusSeq;

    /** 状態コード。 */
    @Column(name = "status_code", nullable = false, length = 32)
    private String statusCode;

    /** 状態名称。 */
    @Column(name = "status_name", nullable = false, length = 64)
    private String statusName;

    /** イベント発生日時。 */
    @Column(name = "event_occurred_at", nullable = false)
    private LocalDateTime eventOccurredAt;

    /** 受信日時。 */
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    /** 生電文ハッシュ。 */
    @Column(name = "raw_payload_hash", nullable = false, length = 128)
    private String rawPayloadHash;

    /** 理由コード。 */
    @Column(name = "reason_code", length = 64)
    private String reasonCode;

    /** 理由分類。 */
    @Column(name = "reason_category", length = 64)
    private String reasonCategory;

    /** 住所補正有無。 */
    @Column(name = "address_corrected")
    private Boolean addressCorrected;

    /** 住所補正レベル。 */
    @Column(name = "address_correction_level", length = 32)
    private String addressCorrectionLevel;

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
     * ステータス連番を返却する。
     *
     * @return ステータス連番
     */
    public int getStatusSeq() {
        return statusSeq;
    }

    /**
     * ステータス連番を設定する。
     *
     * @param statusSeq ステータス連番
     */
    public void setStatusSeq(int statusSeq) {
        this.statusSeq = statusSeq;
    }

    /**
     * 状態コードを返却する。
     *
     * @return 状態コード
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * 状態コードを設定する。
     *
     * @param statusCode 状態コード
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 状態名称を返却する。
     *
     * @return 状態名称
     */
    public String getStatusName() {
        return statusName;
    }

    /**
     * 状態名称を設定する。
     *
     * @param statusName 状態名称
     */
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    /**
     * イベント発生日時を返却する。
     *
     * @return イベント発生日時
     */
    public LocalDateTime getEventOccurredAt() {
        return eventOccurredAt;
    }

    /**
     * イベント発生日時を設定する。
     *
     * @param eventOccurredAt イベント発生日時
     */
    public void setEventOccurredAt(LocalDateTime eventOccurredAt) {
        this.eventOccurredAt = eventOccurredAt;
    }

    /**
     * 受信日時を返却する。
     *
     * @return 受信日時
     */
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    /**
     * 受信日時を設定する。
     *
     * @param receivedAt 受信日時
     */
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    /**
     * 生電文ハッシュを返却する。
     *
     * @return 生電文ハッシュ
     */
    public String getRawPayloadHash() {
        return rawPayloadHash;
    }

    /**
     * 生電文ハッシュを設定する。
     *
     * @param rawPayloadHash 生電文ハッシュ
     */
    public void setRawPayloadHash(String rawPayloadHash) {
        this.rawPayloadHash = rawPayloadHash;
    }

    /**
     * 理由コードを返却する。
     *
     * @return 理由コード
     */
    public String getReasonCode() {
        return reasonCode;
    }

    /**
     * 理由コードを設定する。
     *
     * @param reasonCode 理由コード
     */
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    /**
     * 理由分類を返却する。
     *
     * @return 理由分類
     */
    public String getReasonCategory() {
        return reasonCategory;
    }

    /**
     * 理由分類を設定する。
     *
     * @param reasonCategory 理由分類
     */
    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    /**
     * 住所補正有無を返却する。
     *
     * @return 住所補正有無
     */
    public Boolean getAddressCorrected() {
        return addressCorrected;
    }

    /**
     * 住所補正有無を設定する。
     *
     * @param addressCorrected 住所補正有無
     */
    public void setAddressCorrected(Boolean addressCorrected) {
        this.addressCorrected = addressCorrected;
    }

    /**
     * 住所補正レベルを返却する。
     *
     * @return 住所補正レベル
     */
    public String getAddressCorrectionLevel() {
        return addressCorrectionLevel;
    }

    /**
     * 住所補正レベルを設定する。
     *
     * @param addressCorrectionLevel 住所補正レベル
     */
    public void setAddressCorrectionLevel(String addressCorrectionLevel) {
        this.addressCorrectionLevel = addressCorrectionLevel;
    }
}
