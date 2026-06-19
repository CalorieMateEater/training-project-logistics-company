package jp.co.hoge.orderhub.common.domain;

/**
 * 出荷依頼実行状態。
 *
 * @author Takuya Yamamoto
 */
public enum ShipmentRequestStatus {
    PENDING,
    WAITING_BUSINESS_HOURS,
    REQUESTING,
    ACCEPTED,
    FAILED
}
