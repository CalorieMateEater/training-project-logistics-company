package jp.co.hoge.orderhub.common.domain;

/**
 * 配送ステータスコード。
 *
 * @author Takuya Yamamoto
 */
public enum DeliveryStatusCode {
    WAITING_BAR_REQUEST,
    ACCEPTED,
    PREPARING,
    IN_TRANSIT,
    DELIVERED,
    DELIVERY_FAILED,
    RETURNED_TO_BASE,
    ADDRESS_ERROR,
    REDISPATCH_PENDING,
    CANCELLED
}
