package jp.co.hoge.orderhubworker.mapper;

import jp.co.hoge.orderhub.common.dto.BarShipmentRequestPayload;
import jp.co.hoge.orderhub.common.mapper.CommonMapperConfig;
import jp.co.hoge.orderhub.common.persistence.entity.BarIdempotencyHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhubworker.mapper.model.BarIdempotencyContext;
import jp.co.hoge.orderhubworker.mapper.model.ShipmentDispatchContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 出荷依頼送信時の業務・連携変換を担う MapStruct Mapper。
 *
 * @author Takuya Yamamoto
 */
@Mapper(config = CommonMapperConfig.class)
public interface ShipmentDispatchMapper {

    /**
     * 出荷依頼送信情報から Bar社向け出荷依頼ペイロードを生成する。
     *
     * @param source 出荷依頼送信情報
     * @return Bar社向け出荷依頼要求
     */
    @Mapping(target = "orderId", source = "orderHeader.orderId")
    @Mapping(target = "partnerOrderId", source = "orderHeader.partnerOrderId")
    @Mapping(target = "shipmentRequestId", source = "shipmentRequest.shipmentRequestId")
    @Mapping(target = "orderSourceCode", source = "orderHeader.orderSource")
    @Mapping(target = "shippingPriorityClass", source = "orderHeader.shippingPriorityClass")
    @Mapping(target = "partnerPriorityLevel", source = "orderHeader.partnerPriorityLevel")
    @Mapping(target = "deliveryType", constant = "NORMAL")
    @Mapping(target = "serviceLevel", constant = "NEXT_DAY")
    @Mapping(target = "temperatureZone", constant = "AMBIENT")
    @Mapping(target = "packageCount", expression = "java(source.orderLines().size())")
    @Mapping(target = "cashOnDeliveryAmount", constant = "0")
    @Mapping(target = "requestedShipDate", source = "requestedShipDate")
    @Mapping(target = "requestedDeliveryDate", source = "requestedDeliveryDate")
    @Mapping(target = "deliveryZipCode", source = "orderHeader.deliveryZipCode")
    @Mapping(target = "deliveryAddress", source = "orderHeader.deliveryAddress")
    @Mapping(target = "deliveryName", constant = "DESTINATION_NAME_UNSPECIFIED")
    @Mapping(target = "deliveryPhone", constant = "0000000000")
    @Mapping(target = "specialInstruction", constant = "AUTO_DISPATCH")
    @Mapping(target = "items", source = "orderLines")
    BarShipmentRequestPayload toBarShipmentRequestPayload(ShipmentDispatchContext source);

    /**
     * 注文明細から Bar社向け出荷依頼商品情報を生成する。
     *
     * @param source 注文明細
     * @return Bar社向け出荷依頼商品情報
     */
    @Mapping(target = "itemName", expression = "java(source.getItemName() == null ? source.getItemCode() : source.getItemName())")
    @Mapping(target = "unitWeightGram", constant = "1000")
    BarShipmentRequestPayload.Item toBarShipmentRequestItem(OrderLineEntity source);

    /**
     * 冪等履歴登録情報から Bar社冪等履歴エンティティを生成する。
     *
     * @param source 冪等履歴登録情報
     * @return Bar社冪等履歴エンティティ
     */
    BarIdempotencyHistoryEntity toBarIdempotencyHistoryEntity(BarIdempotencyContext source);
}
