package jp.co.hoge.shippinggateway.mapper.model;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;

/**
 * 直受注登録時のエンティティ変換用コンテキスト。
 *
 * @param orderId 注文 ID
 * @param shipmentRequestId 出荷依頼 ID
 * @param request 出荷依頼要求
 * @param customerStatus 顧客状態
 * @param reservationResponse 在庫引当応答
 * @param carrierCode 配送会社コード
 * @param orderStatus 注文ステータス
 * @param shipmentRequestStatus 出荷依頼ステータス
 * @param nextRequestAfter 次回送信予定日時
 * @param routingRuleId ルーティングルール ID
 * @param now 登録日時
 * @author Takuya Yamamoto
 */
public record ShipmentRegistrationContext(
    String orderId,
    String shipmentRequestId,
    ShipmentRegistrationRequest request,
    CustomerStatusResponse customerStatus,
    StockReservationResponse reservationResponse,
    CarrierCode carrierCode,
    OrderStatus orderStatus,
    ShipmentRequestStatus shipmentRequestStatus,
    LocalDateTime nextRequestAfter,
    String routingRuleId,
    LocalDateTime now) {}
