package jp.co.hoge.orderhub.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Bar社から受信する配送結果通知リクエスト。
 *
 * @param barShipmentId Bar社出荷 ID
 * @param orderId 注文 ID
 * @param partnerOrderId 連携先注文 ID
 * @param statusSeq 配送ステータス連番
 * @param deliveryStatus 配送ステータスコード
 * @param statusLabel 配送ステータス名称
 * @param eventOccurredAt イベント発生日時
 * @param locationCode 拠点コード
 * @param reasonCode 理由コード
 * @param reasonCategory 理由分類
 * @param addressCorrected 住所補正有無
 * @param addressCorrectionLevel 住所補正レベル
 * @param driverComment ドライバーコメント
 * @author Takuya Yamamoto
 */
public record BarDeliveryResultRequest(
        @NotBlank String barShipmentId,
        @NotBlank String orderId,
        @NotBlank String partnerOrderId,
        int statusSeq,
        @NotBlank String deliveryStatus,
        @NotBlank String statusLabel,
        @NotBlank String eventOccurredAt,
        String locationCode,
        String reasonCode,
        String reasonCategory,
        Boolean addressCorrected,
        String addressCorrectionLevel,
        String driverComment
) {
}
