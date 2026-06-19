package jp.co.hoge.stockkeeper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StockReservationServiceTest {
    @Mock
    private StockItemRepository stockItemRepository;

    @InjectMocks
    private StockReservationService stockReservationService;

    @Test
    void shouldReserveStock() {
        StockItemEntity stock = new StockItemEntity();
        stock.setItemCode("ITM0000001");
        stock.setAvailableQuantity(10);
        when(stockItemRepository.findById("ITM0000001")).thenReturn(Optional.of(stock));

        StockReservationResponse response = stockReservationService.reserve(
                new StockReservationRequest("O1", List.of(new StockReservationRequest.ReservationItem("ITM0000001", 2)))
        );

        assertThat(response.status()).isEqualTo("RESERVED");
        assertThat(stock.getAvailableQuantity()).isEqualTo(8);
    }

    @Test
    void shouldFailWhenStockInsufficient() {
        StockItemEntity stock = new StockItemEntity();
        stock.setItemCode("ITM0000001");
        stock.setAvailableQuantity(1);
        when(stockItemRepository.findById("ITM0000001")).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> stockReservationService.reserve(
                new StockReservationRequest("O1", List.of(new StockReservationRequest.ReservationItem("ITM0000001", 2)))
        )).isInstanceOf(ResponseStatusException.class);
    }
}
