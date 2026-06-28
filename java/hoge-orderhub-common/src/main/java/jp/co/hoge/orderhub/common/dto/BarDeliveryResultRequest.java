package jp.co.hoge.orderhub.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * 配送会社から受信する配送結果通知リクエスト。
 *
 * @param barShipmentId 配送会社出荷 ID
 * @param orderId 注文 ID
 * @param partnerOrderId 連携先注文 ID
 * @param statusSeq 配送ステータス連番
 * @param deliveryStatus 配送ステータスコード
 * @param statusLabel 配送ステータス名称
 * @param eventOccurredAt イベント発生日時
 * @param temperatureZone 温度帯
 * @param sizeType サイズ区分
 * @param locationCode 拠点コード
 * @param reasonCode 理由コード
 * @param reasonCategory 理由分類
 * @param addressCorrected 住所補正有無
 * @param addressCorrectionLevel 住所補正レベル
 * @param driverComment ドライバーコメント
 * @author Takuya Yamamoto
 */
public record BarDeliveryResultRequest(
    @JsonAlias({"bar_shipment_id", "fuga_shipment_id"}) @NotBlank String barShipmentId,
    @JsonAlias("order_id") @NotBlank String orderId,
    @JsonAlias("partner_order_id") @NotBlank String partnerOrderId,
    @JsonAlias("status_seq") int statusSeq,
    @JsonAlias("delivery_status") @NotBlank String deliveryStatus,
    @JsonAlias("status_label") @NotBlank String statusLabel,
    @JsonAlias("event_occurred_at") @NotBlank String eventOccurredAt,
    @JsonAlias("temperature_zone") String temperatureZone,
    @JsonAlias("size_type") String sizeType,
    @JsonAlias("location_code") String locationCode,
    @JsonAlias("reason_code") String reasonCode,
    @JsonAlias("reason_category") String reasonCategory,
    @JsonAlias("address_corrected") Boolean addressCorrected,
    @JsonAlias("address_correction_level") String addressCorrectionLevel,
    @JsonAlias("driver_comment") String driverComment) {}
