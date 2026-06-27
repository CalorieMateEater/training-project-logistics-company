package jp.co.hoge.orderhubbatch.mapper;

import jp.co.hoge.orderhub.common.domain.CarrierCode;
import jp.co.hoge.orderhub.common.domain.NotificationStatus;
import jp.co.hoge.orderhub.common.domain.NotificationType;
import jp.co.hoge.orderhub.common.domain.OrderSource;
import jp.co.hoge.orderhub.common.mapper.CommonMapperConfig;
import jp.co.hoge.orderhub.common.persistence.entity.CustomerCheckResultEntity;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.orderhubbatch.mapper.model.FooOrderImportContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Foo注文取込時の永続化エンティティ変換を担う MapStruct Mapper。
 *
 * @author Takuya Yamamoto
 */
@Mapper(
    config = CommonMapperConfig.class,
    imports = {
      CarrierCode.class,
      OrderSource.class,
      NotificationStatus.class,
      NotificationType.class
    })
public interface FooOrderImportEntityMapper {
  /**
   * 取込情報から注文ヘッダを生成する。
   *
   * @param source 取込情報
   * @return 注文ヘッダ
   */
  @Mapping(target = "orderSource", expression = "java(OrderSource.FOO)")
  @Mapping(target = "shipmentStatus", source = "orderStatus")
  @Mapping(target = "carrierCode", source = "carrierCode")
  @Mapping(target = "deliveryZipCode", source = "zipCode")
  @Mapping(target = "deliveryAddress", source = "address")
  @Mapping(target = "deliveryName", source = "deliveryName")
  @Mapping(target = "deliveryPhone", source = "deliveryPhone")
  @Mapping(target = "packageCount", source = "packageCount")
  @Mapping(target = "paymentMethod", source = "paymentMethod")
  @Mapping(target = "requestedDeliveryDate", source = "requestedDeliveryDate")
  @Mapping(target = "specialInstruction", source = "specialInstruction")
  @Mapping(target = "subtotalExcludingTax", source = "subtotalExcludingTax")
  @Mapping(target = "taxAmount", source = "taxAmount")
  @Mapping(target = "billingAmount", source = "billingAmount")
  @Mapping(target = "createdAt", source = "orderDatetime")
  @Mapping(target = "updatedAt", source = "now")
  OrderHeaderEntity toOrderHeader(FooOrderImportContext source);

  /**
   * 取込情報から注文明細を生成する。
   *
   * @param source 取込情報
   * @return 注文明細
   */
  @Mapping(target = "orderLineNo", constant = "1")
  @Mapping(
      target = "itemName",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.itemCode() : source.reservationResponse().results().get(0).itemName())")
  @Mapping(target = "unitPriceExcludingTax", source = "unitPriceExcludingTax")
  @Mapping(target = "taxRate", source = "taxRate")
  @Mapping(target = "lineSubtotalExcludingTax", source = "subtotalExcludingTax")
  @Mapping(target = "lineTaxAmount", source = "taxAmount")
  @Mapping(
      target = "unitWeightGramSnapshot",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? 0 : source.reservationResponse().results().get(0).unitWeightGram())")
  @Mapping(
      target = "temperatureZoneSnapshot",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? \"UNKNOWN\" : source.reservationResponse().results().get(0).temperatureZone())")
  @Mapping(
      target = "sizeTypeSnapshot",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? \"UNKNOWN\" : source.reservationResponse().results().get(0).sizeType())")
  @Mapping(
      target = "sourceWarehouseLocationCode",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? null : source.reservationResponse().results().get(0).warehouseLocationCode())")
  OrderLineEntity toOrderLine(FooOrderImportContext source);

  /**
   * 取込情報から顧客確認結果を生成する。
   *
   * @param source 取込情報
   * @return 顧客確認結果
   */
  @Mapping(target = "checkStatus", source = "customerStatus.status")
  @Mapping(target = "memberRank", source = "customerStatus.memberRank")
  @Mapping(target = "checkedAt", source = "now")
  CustomerCheckResultEntity toCustomerCheckResult(FooOrderImportContext source);

  /**
   * 取込情報から在庫引当結果を生成する。
   *
   * @param source 取込情報
   * @return 在庫引当結果
   */
  @Mapping(target = "orderLineNo", constant = "1")
  @Mapping(target = "reservationId", source = "reservationResponse.reservationId")
  @Mapping(
      target = "reservationStatus",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.reservationResponse().status() : source.reservationResponse().results().get(0).reservationStatus())")
  @Mapping(
      target = "reservedQuantity",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? source.quantity() : source.reservationResponse().results().get(0).reservedQuantity())")
  @Mapping(
      target = "warehouseLocationCode",
      expression =
          "java(source.reservationResponse().results().isEmpty() ? null : source.reservationResponse().results().get(0).warehouseLocationCode())")
  @Mapping(target = "releasedQuantity", constant = "0")
  @Mapping(target = "shippedConfirmedQuantity", constant = "0")
  StockReservationResultEntity toStockReservationResult(FooOrderImportContext source);

  /**
   * 取込情報から出荷依頼を生成する。
   *
   * @param source 取込情報
   * @return 出荷依頼
   */
  @Mapping(target = "carrierCode", source = "carrierCode")
  @Mapping(target = "orderSource", expression = "java(OrderSource.FOO)")
  @Mapping(target = "queueEnqueuedAt", source = "now")
  ShipmentRequestEntity toShipmentRequest(FooOrderImportContext source);

  /**
   * 取込情報から Foo向け受付通知履歴を生成する。
   *
   * @param source 取込情報
   * @return 通知履歴
   */
  @Mapping(target = "notificationType", expression = "java(NotificationType.FOO_ACK)")
  @Mapping(target = "notificationStatus", expression = "java(NotificationStatus.PENDING)")
  @Mapping(target = "eventType", constant = "ORDER_ACCEPTED")
  @Mapping(target = "notificationKey", expression = "java(\"foo-ack:\" + source.partnerOrderId())")
  @Mapping(
      target = "payloadSummary",
      expression =
          "java(source.receiptStatus() + \"|\" + source.messageCode() + \"|\" + source.now())")
  @Mapping(target = "destination", constant = "foo-ack-file")
  @Mapping(target = "createdAt", source = "now")
  @Mapping(target = "updatedAt", source = "now")
  NotificationHistoryEntity toFooAckNotification(FooOrderImportContext source);
}
