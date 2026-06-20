package jp.co.hoge.orderhubworker.mapper.model;

import java.time.LocalDate;
import java.util.List;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;

/**
 * 出荷依頼送信用エンティティ変換のためのコンテキスト。
 *
 * @param orderHeader 注文ヘッダ
 * @param shipmentRequest 出荷依頼
 * @param orderLines 注文明細一覧
 * @param requestedShipDate 出荷希望日
 * @param requestedDeliveryDate 配送希望日
 * @author Takuya Yamamoto
 */
public record ShipmentDispatchContext(
    OrderHeaderEntity orderHeader,
    ShipmentRequestEntity shipmentRequest,
    List<OrderLineEntity> orderLines,
    LocalDate requestedShipDate,
    LocalDate requestedDeliveryDate) {}
