package jp.co.hoge.orderhub.common.support;

import jp.co.hoge.orderhub.common.domain.DeliveryStatusCode;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * 配送状態コードから業務状態への変換サービス。
 *
 * @author Takuya Yamamoto
 */
@Component
public class StatusMapper {
    /**
     * 配送状態コードを注文状態へ変換する。
     *
     * @param statusCode 配送状態コード
     * @return 注文状態
     */
    public OrderStatus toOrderStatus(DeliveryStatusCode statusCode) {
        return switch (statusCode) {
            case WAITING_BAR_REQUEST -> OrderStatus.WAITING_BAR_REQUEST;
            case ACCEPTED -> OrderStatus.BAR_ACCEPTED;
            case PREPARING -> OrderStatus.PREPARING_FOR_SHIPMENT;
            case IN_TRANSIT -> OrderStatus.IN_DELIVERY_FLOW;
            case DELIVERED -> OrderStatus.COMPLETED;
            case DELIVERY_FAILED, RETURNED_TO_BASE, ADDRESS_ERROR -> OrderStatus.EXCEPTION;
            case REDISPATCH_PENDING -> OrderStatus.REDISPATCH_PENDING;
            case CANCELLED -> OrderStatus.CANCELLED;
        };
    }
}
