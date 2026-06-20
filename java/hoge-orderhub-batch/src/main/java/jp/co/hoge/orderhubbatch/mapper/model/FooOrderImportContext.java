package jp.co.hoge.orderhubbatch.mapper.model;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;

/**
 * Foo注文取込時のエンティティ変換用コンテキスト。
 *
 * @param orderId 注文 ID
 * @param shipmentRequestId 出荷依頼 ID
 * @param notificationId 通知 ID
 * @param partnerOrderId 対向注文番号
 * @param priorityLevel 優先度
 * @param customerId 顧客 ID
 * @param itemCode 商品コード
 * @param quantity 数量
 * @param orderDatetime 注文日時
 * @param zipCode 郵便番号
 * @param address 配送先住所
 * @param shippingReleaseAt 出荷解放日時
 * @param carrierCode 配送会社コード
 * @param orderStatus 注文ステータス
 * @param shippingPriorityClass 配送優先区分
 * @param shipmentRequestStatus 出荷依頼ステータス
 * @param nextRequestAfter 次回送信予定日時
 * @param routingRuleId ルーティングルール ID
 * @param receiptStatus 受付ステータス
 * @param messageCode 受付メッセージコード
 * @param customerStatus 顧客状態
 * @param reservationResponse 在庫引当応答
 * @param now 登録日時
 * @author Takuya Yamamoto
 */
public record FooOrderImportContext(
    String orderId,
    String shipmentRequestId,
    String notificationId,
    String partnerOrderId,
    int priorityLevel,
    String customerId,
    String itemCode,
    int quantity,
    LocalDateTime orderDatetime,
    String zipCode,
    String address,
    LocalDateTime shippingReleaseAt,
    CarrierCode carrierCode,
    OrderStatus orderStatus,
    ShippingPriorityClass shippingPriorityClass,
    ShipmentRequestStatus shipmentRequestStatus,
    LocalDateTime nextRequestAfter,
    String routingRuleId,
    String receiptStatus,
    String messageCode,
    CustomerStatusResponse customerStatus,
    StockReservationResponse reservationResponse,
    LocalDateTime now) {}
