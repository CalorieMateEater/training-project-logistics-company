package jp.co.hoge.orderhub.common.domain;

/**
 * 注文全体の進行状態。
 *
 * @author Takuya Yamamoto
 */
public enum OrderStatus {
  RECEIVED,
  WAITING_SHIPPING_RELEASE,
  WAITING_BAR_REQUEST,
  WAITING_FUGA_REQUEST,
  BAR_REQUESTED,
  BAR_ACCEPTED,
  FUGA_ACCEPTED,
  PREPARING_FOR_SHIPMENT,
  IN_DELIVERY_FLOW,
  COMPLETED,
  EXCEPTION,
  REDISPATCH_PENDING,
  CANCELLED
}
