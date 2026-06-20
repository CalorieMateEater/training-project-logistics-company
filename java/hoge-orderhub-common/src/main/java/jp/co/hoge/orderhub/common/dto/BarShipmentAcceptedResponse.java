package jp.co.hoge.orderhub.common.dto;

/**
 * Bar社の出荷依頼受付結果レスポンス。
 *
 * @param barShipmentId Bar社出荷 ID
 * @param shipmentRequestId 出荷依頼 ID
 * @param acceptanceStatus 受付状態
 * @param acceptedAt 受付日時
 * @param duplicate 重複受付フラグ
 * @author Takuya Yamamoto
 */
public record BarShipmentAcceptedResponse(
    String barShipmentId,
    String shipmentRequestId,
    String acceptanceStatus,
    String acceptedAt,
    boolean duplicate) {}
