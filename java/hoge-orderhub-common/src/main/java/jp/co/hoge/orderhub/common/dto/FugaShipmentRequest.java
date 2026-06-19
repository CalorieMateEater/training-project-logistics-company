package jp.co.hoge.orderhub.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fuga社から受信する出荷依頼 API リクエスト。
 *
 * @param partnerRequestId 連携先要求 ID
 * @param partnerOrderId 連携先注文 ID
 * @param customerId 顧客 ID
 * @param itemCode 商品コード
 * @param quantity 数量
 * @param shipmentPreference 出荷希望区分
 * @param shipmentMode 出荷モード
 * @param deliveryConstraint 配送制約
 * @param deliveryZipCode 配送先郵便番号
 * @param deliveryAddress 配送先住所
 * @param requestedDeliveryDate 配送希望日
 * @param shippingReleaseAt 出荷解放日時
 * @author Takuya Yamamoto
 */
public record FugaShipmentRequest(
        @NotBlank String partnerRequestId,
        @NotBlank String partnerOrderId,
        @NotBlank String customerId,
        @NotBlank String itemCode,
        int quantity,
        @NotBlank String shipmentPreference,
        String shipmentMode,
        @Valid @NotNull DeliveryConstraint deliveryConstraint,
        @NotBlank String deliveryZipCode,
        @NotBlank String deliveryAddress,
        LocalDate requestedDeliveryDate,
        LocalDateTime shippingReleaseAt
) {

    /**
     * Fuga社依頼に付随する配送制約情報。
     *
     * @param temperatureZone 温度帯
     * @param timeSlot 時間帯
     * @author Takuya Yamamoto
     */
    public record DeliveryConstraint(
            @NotBlank String temperatureZone,
            @NotBlank String timeSlot
    ) {
    }
}
