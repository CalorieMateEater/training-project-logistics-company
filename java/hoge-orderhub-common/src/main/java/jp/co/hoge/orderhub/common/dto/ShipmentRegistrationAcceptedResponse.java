package jp.co.hoge.orderhub.common.dto;

/**
 * Fuga社向け出荷依頼受付結果レスポンス。
 *
 * @param orderId 注文 ID
 * @param partnerRequestId 連携先要求 ID
 * @param registrationStatus 受付状態
 * @param currentStatus 現在状態
 * @param acceptedAt 受付日時
 * @author Takuya Yamamoto
 */
public record ShipmentRegistrationAcceptedResponse(
    String orderId,
    String partnerRequestId,
    String registrationStatus,
    String currentStatus,
    String acceptedAt) {}
