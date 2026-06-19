package jp.co.hoge.orderhubbatch.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.persistence.repository.CustomerCheckResultRepository;
import jp.co.hoge.orderhub.common.persistence.repository.NotificationHistoryRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderLineRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.BusinessHoursService;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.PriorityResolver;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubbatch.mapper.FooOrderImportEntityMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.web.server.ResponseStatusException;

class FooOrderImportServiceTest {
    @Test
    void shouldFailWhenPrioritySpecifiedForNonReservedOrder() {
        TimeProvider timeProvider = () -> LocalDateTime.of(2026, 6, 17, 9, 0, 0);
        FooOrderImportService service = new FooOrderImportService(
                mock(OrderHeaderRepository.class),
                mock(OrderLineRepository.class),
                mock(ShipmentRequestRepository.class),
                mock(CustomerCheckResultRepository.class),
                mock(StockReservationResultRepository.class),
                mock(NotificationHistoryRepository.class),
                mock(CustomerRegistryClient.class),
                mock(StockKeeperClient.class),
                mock(InterfaceHistoryService.class),
                new IdFactory(timeProvider),
                new PriorityResolver(),
                new BusinessHoursService(),
                timeProvider,
                Mappers.getMapper(FooOrderImportEntityMapper.class)
        );

        assertThatThrownBy(() -> service.importRecord(
                "D,FO202606170001,01,8,C00000000001,ITM0000001,2,2026-06-17T09:00:00,1000001,Tokyo"
        )).isInstanceOf(ResponseStatusException.class);
    }
}
