package jp.co.hoge.orderhub.common.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Bar社へ送信する出荷依頼電文。
 *
 * @param orderId 注文 ID
 * @param partnerOrderId 連携先注文 ID
 * @param shipmentRequestId 出荷依頼 ID
 * @param orderSourceCode 注文元コード
 * @param shippingPriorityClass 配送優先区分
 * @param partnerPriorityLevel 連携先優先度
 * @param deliveryType 配送種別
 * @param serviceLevel サービスレベル
 * @param temperatureZone 温度帯
 * @param packageCount 梱包数
 * @param cashOnDeliveryAmount 代引金額
 * @param requestedShipDate 出荷希望日
 * @param requestedDeliveryDate 配送希望日
 * @param deliveryZipCode 配送先郵便番号
 * @param deliveryAddress 配送先住所
 * @param deliveryName 配送先氏名
 * @param deliveryPhone 配送先電話番号
 * @param specialInstruction 特記事項
 * @param items 商品一覧
 * @author Takuya Yamamoto
 */
public record BarShipmentRequestPayload(
        String orderId,
        String partnerOrderId,
        String shipmentRequestId,
        String orderSourceCode,
        String shippingPriorityClass,
        int partnerPriorityLevel,
        String deliveryType,
        String serviceLevel,
        String temperatureZone,
        int packageCount,
        int cashOnDeliveryAmount,
        LocalDate requestedShipDate,
        LocalDate requestedDeliveryDate,
        String deliveryZipCode,
        String deliveryAddress,
        String deliveryName,
        String deliveryPhone,
        String specialInstruction,
        List<Item> items
) {

    /**
     * 出荷依頼電文内の商品明細。
     *
     * @param itemCode 商品コード
     * @param itemName 商品名
     * @param quantity 数量
     * @param unitWeightGram 単位重量
     * @author Takuya Yamamoto
     */
    public record Item(
            String itemCode,
            String itemName,
            int quantity,
            int unitWeightGram
    ) {
    }
}
