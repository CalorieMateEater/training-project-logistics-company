package jp.co.hoge.orderhub.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("order_id") String orderId,
    @JsonProperty("partner_order_id") String partnerOrderId,
    @JsonProperty("shipment_request_id") String shipmentRequestId,
    @JsonProperty("order_source_code") String orderSourceCode,
    @JsonProperty("shipping_priority_class") String shippingPriorityClass,
    @JsonProperty("partner_priority_level") int partnerPriorityLevel,
    @JsonProperty("delivery_type") String deliveryType,
    @JsonProperty("service_level") String serviceLevel,
    @JsonProperty("temperature_zone") String temperatureZone,
    @JsonProperty("size_type") String sizeType,
    @JsonProperty("package_count") int packageCount,
    @JsonProperty("cash_on_delivery_amount") int cashOnDeliveryAmount,
    @JsonProperty("requested_ship_date") LocalDate requestedShipDate,
    @JsonProperty("requested_delivery_date") LocalDate requestedDeliveryDate,
    @JsonProperty("delivery_zip_code") String deliveryZipCode,
    @JsonProperty("delivery_address") String deliveryAddress,
    @JsonProperty("delivery_name") String deliveryName,
    @JsonProperty("delivery_phone") String deliveryPhone,
    @JsonProperty("special_instruction") String specialInstruction,
    @JsonProperty("items") List<Item> items) {

  /**
   * 出荷依頼電文内の商品明細。
   *
   * @param itemCode 商品コード
   * @param itemName 商品名
   * @param quantity 数量
   * @param sourceWarehouseLocationCode 出荷元倉庫場所コード
   * @param unitWeightGram 単位重量
   * @author Takuya Yamamoto
   */
  public record Item(
      @JsonProperty("item_code") String itemCode,
      @JsonProperty("item_name") String itemName,
      @JsonProperty("quantity") int quantity,
      @JsonProperty("source_warehouse_location_code") String sourceWarehouseLocationCode,
      @JsonProperty("unit_weight_gram") int unitWeightGram) {}
}
