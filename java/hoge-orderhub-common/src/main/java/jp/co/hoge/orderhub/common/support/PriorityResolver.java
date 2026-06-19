package jp.co.hoge.orderhub.common.support;

import jp.co.hoge.orderhub.common.domain.OrderType;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import org.springframework.stereotype.Component;

/**
 * 配送優先区分判定サービス。
 *
 * @author Takuya Yamamoto
 */
@Component
public class PriorityResolver {
    /**
     * Foo 注文の配送優先区分を判定する。
     *
     * @param orderType 注文種別
     * @param priorityLevel 連携先優先度
     * @return 配送優先区分
     */
    public ShippingPriorityClass resolveFooPriority(OrderType orderType, int priorityLevel) {
        if (orderType == OrderType.RESERVED && priorityLevel >= 5) {
            return ShippingPriorityClass.PRIORITY;
        }
        return ShippingPriorityClass.NORMAL;
    }
}
