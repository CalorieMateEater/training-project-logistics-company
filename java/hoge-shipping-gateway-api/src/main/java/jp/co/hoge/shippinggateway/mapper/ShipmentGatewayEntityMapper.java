package jp.co.hoge.shippinggateway.mapper;

import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import jp.co.hoge.orderhub.common.mapper.CommonMapperConfig;
import jp.co.hoge.orderhub.common.persistence.entity.CustomerCheckResultEntity;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.shippinggateway.mapper.model.BarDeliveryResultContext;
import jp.co.hoge.shippinggateway.mapper.model.ShipmentRegistrationContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Shipping Gateway API の永続化エンティティ変換を担う MapStruct Mapper。
 *
 * @author Takuya Yamamoto
 */
@Mapper(
    config = CommonMapperConfig.class,
    imports = {CarrierCode.class, OrderSource.class, ShippingPriorityClass.class})
public interface ShipmentGatewayEntityMapper {
  /**
   * 直受注登録情報から注文ヘッダを生成する。
   *
   * @param source 出荷依頼受付情報
   * @return 注文ヘッダ
   */
  @Mapping(target = "partnerOrderId", source = "request.partnerOrderId")
  @Mapping(target = "partnerRequestId", source = "request.partnerRequestId")
  @Mapping(target = "orderSource", expression = "java(OrderSource.HOGE)")
  @Mapping(target = "partnerPriorityLevel", constant = "0")
  @Mapping(target = "shippingPriorityClass", expression = "java(ShippingPriorityClass.NORMAL)")
  @Mapping(target = "customerId", source = "request.customerId")
  @Mapping(target = "carrierCode", source = "carrierCode")
  @Mapping(target = "shipmentStatus", source = "orderStatus")
  @Mapping(target = "deliveryZipCode", source = "request.deliveryZipCode")
  @Mapping(target = "deliveryAddress", source = "request.deliveryAddress")
  @Mapping(target = "deliveryName", source = "request.deliveryName")
  @Mapping(target = "deliveryPhone", source = "request.deliveryPhone")
  @Mapping(target = "packageCount", source = "request.packageCount")
  @Mapping(target = "paymentMethod", source = "request.paymentMethod")
  @Mapping(target = "requestedDeliveryDate", source = "request.requestedDeliveryDate")
  @Mapping(target = "specialInstruction", source = "request.specialInstruction")
  @Mapping(
      target = "subtotalExcludingTax",
      expression = "java(source.request().unitPriceExcludingTax() * source.request().quantity())")
  @Mapping(
      target = "taxAmount",
      expression =
          "java(source.request().unitPriceExcludingTax() * source.request().quantity() * source.request().taxRate() / 100)")
  @Mapping(
      target = "billingAmount",
      expression =
          "java(source.request().unitPriceExcludingTax() * source.request().quantity() + source.request().unitPriceExcludingTax() * source.request().quantity() * source.request().taxRate() / 100)")
  @Mapping(target = "shippingReleaseAt", source = "request.shippingReleaseAt")
  @Mapping(target = "createdAt", source = "now")
  @Mapping(target = "updatedAt", source = "now")
  OrderHeaderEntity toOrderHeader(ShipmentRegistrationContext source);

  /**
   * 直受注登録情報から注文明細を生成する。
   *
   * @param source 出荷依頼受付情報
   * @return 注文明細
   */
  @Mapping(target = "orderId", source = "orderId")
  @Mapping(target = "orderLineNo", constant = "1")
  @Mapping(target = "itemCode", source = "request.itemCode")
  @Mapping(target = "quantity", source = "request.quantity")
  @Mapping(
      target = "itemName",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.request().itemCode() : source.reservationResponse().results().get(0).itemName())")
  @Mapping(target = "unitPriceExcludingTax", source = "request.unitPriceExcludingTax")
  @Mapping(target = "taxRate", source = "request.taxRate")
  @Mapping(
      target = "lineSubtotalExcludingTax",
      expression = "java(source.request().unitPriceExcludingTax() * source.request().quantity())")
  @Mapping(
      target = "lineTaxAmount",
      expression =
          "java(source.request().unitPriceExcludingTax() * source.request().quantity() * source.request().taxRate() / 100)")
  @Mapping(
      target = "unitWeightGramSnapshot",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? 0 : source.reservationResponse().results().get(0).unitWeightGram())")
  @Mapping(
      target = "temperatureZoneSnapshot",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.request().deliveryConstraint().temperatureZone() : source.reservationResponse().results().get(0).temperatureZone())")
  @Mapping(
      target = "sizeTypeSnapshot",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? \"UNKNOWN\" : source.reservationResponse().results().get(0).sizeType())")
  @Mapping(
      target = "sourceWarehouseLocationCode",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? null : source.reservationResponse().results().get(0).warehouseLocationCode())")
  OrderLineEntity toOrderLine(ShipmentRegistrationContext source);

  /**
   * 直受注登録情報から顧客確認結果を生成する。
   *
   * @param source 出荷依頼受付情報
   * @return 顧客確認結果
   */
  @Mapping(target = "orderId", source = "orderId")
  @Mapping(target = "customerId", source = "request.customerId")
  @Mapping(target = "checkStatus", source = "customerStatus.status")
  @Mapping(target = "memberRank", source = "customerStatus.memberRank")
  @Mapping(target = "checkedAt", source = "now")
  CustomerCheckResultEntity toCustomerCheckResult(ShipmentRegistrationContext source);

  /**
   * 直受注登録情報から在庫引当結果を生成する。
   *
   * @param source 出荷依頼受付情報
   * @return 在庫引当結果
   */
  @Mapping(target = "orderId", source = "orderId")
  @Mapping(target = "orderLineNo", constant = "1")
  @Mapping(target = "itemCode", source = "request.itemCode")
  @Mapping(target = "reservationId", source = "reservationResponse.reservationId")
  @Mapping(
      target = "reservationStatus",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.reservationResponse().status() : source.reservationResponse().results().get(0).reservationStatus())")
  @Mapping(
      target = "reservedQuantity",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.request().quantity() : source.reservationResponse().results().get(0).reservedQuantity())")
  @Mapping(
      target = "warehouseLocationCode",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? null : source.reservationResponse().results().get(0).warehouseLocationCode())")
  @Mapping(target = "releasedQuantity", constant = "0")
  @Mapping(target = "shippedConfirmedQuantity", constant = "0")
  StockReservationResultEntity toStockReservationResult(ShipmentRegistrationContext source);

  /**
   * 直受注登録情報から出荷依頼を生成する。
   *
   * @param source 出荷依頼受付情報
   * @return 出荷依頼
   */
  @Mapping(target = "carrierCode", source = "carrierCode")
  @Mapping(target = "orderSource", expression = "java(OrderSource.HOGE)")
  @Mapping(target = "partnerPriorityLevel", constant = "0")
  @Mapping(target = "shippingPriorityClass", expression = "java(ShippingPriorityClass.NORMAL)")
  @Mapping(target = "routingRuleId", source = "routingRuleId")
  @Mapping(target = "queueEnqueuedAt", source = "now")
  ShipmentRequestEntity toShipmentRequest(ShipmentRegistrationContext source);

  /**
   * Bar配送結果通知から配送状態履歴を生成する。
   *
   * @param source 配送結果通知情報
   * @return 配送状態履歴
   */
  @Mapping(target = "orderId", source = "request.orderId")
  @Mapping(target = "statusSeq", source = "request.statusSeq")
  @Mapping(target = "statusCode", source = "request.deliveryStatus")
  @Mapping(target = "statusName", source = "request.statusLabel")
  @Mapping(target = "eventOccurredAt", source = "occurredAt")
  @Mapping(target = "receivedAt", source = "now")
  @Mapping(target = "rawPayloadHash", source = "rawPayloadHash")
  @Mapping(target = "reasonCode", source = "request.reasonCode")
  @Mapping(target = "reasonCategory", source = "request.reasonCategory")
  @Mapping(target = "addressCorrected", source = "request.addressCorrected")
  @Mapping(target = "addressCorrectionLevel", source = "request.addressCorrectionLevel")
  DeliveryStatusHistoryEntity toDeliveryStatusHistoryEntity(BarDeliveryResultContext source);

  /**
   * Bar配送結果通知で配送状態最新を更新する。
   *
   * @param source 配送結果通知情報
   * @param target 配送状態最新
   */
  @Mapping(target = "orderId", source = "request.orderId")
  @Mapping(target = "latestStatusCode", source = "request.deliveryStatus")
  @Mapping(target = "latestStatusName", source = "request.statusLabel")
  @Mapping(target = "latestDisplayStatusName", source = "latestDisplayStatusName")
  @Mapping(target = "latestStatusSeq", source = "request.statusSeq")
  @Mapping(target = "latestStatusAt", source = "occurredAt")
  @Mapping(target = "lastReceivedAt", source = "now")
  @Mapping(target = "latestReasonCategory", source = "request.reasonCategory")
  @Mapping(target = "addressCorrected", source = "request.addressCorrected")
  @Mapping(target = "addressCorrectionLevel", source = "request.addressCorrectionLevel")
  void updateDeliveryStatusCurrentEntity(
      BarDeliveryResultContext source, @MappingTarget DeliveryStatusCurrentEntity target);
}
