package jp.co.hoge.orderhub.common.support;

import static org.assertj.core.api.Assertions.assertThat;

import jp.co.hoge.orderhub.common.domain.OrderType;
import jp.co.hoge.orderhub.common.domain.ShippingPriorityClass;
import org.junit.jupiter.api.Test;

class PriorityResolverTest {
    private final PriorityResolver priorityResolver = new PriorityResolver();

    @Test
    void shouldResolvePriorityForReservedOrder() {
        assertThat(priorityResolver.resolveFooPriority(OrderType.RESERVED, 8)).isEqualTo(ShippingPriorityClass.PRIORITY);
    }

    @Test
    void shouldResolveNormalForNonReservedOrder() {
        assertThat(priorityResolver.resolveFooPriority(OrderType.NORMAL, 8)).isEqualTo(ShippingPriorityClass.NORMAL);
    }
}
