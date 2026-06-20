package jp.co.hoge.stockkeeper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.entity.StockReservationLedgerEntity;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.StockReservationLedgerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StockReservationServiceTest {
  @Mock private StockItemRepository stockItemRepository;
  @Mock private StockReservationLedgerRepository stockReservationLedgerRepository;
  @Mock private IdFactory idFactory;

  @InjectMocks private StockReservationService stockReservationService;

  @Test
  void shouldReserveStock() {
    StockItemEntity stock = new StockItemEntity();
    stock.setItemCode("ITM0000001");
    stock.setWarehouseLocationCode("WH-TYO-01");
    stock.setOnHandQuantity(10);
    stock.setReservedQuantity(0);
    stock.setAvailableQuantity(10);
    when(stockItemRepository.findByItemCodeOrderByAvailableQuantityDesc("ITM0000001"))
        .thenReturn(List.of(stock));
    when(idFactory.reservationId()).thenReturn("RSV-20260620120000-ABC123");

    StockReservationResponse response =
        stockReservationService.reserve(
            new StockReservationRequest(
                "O1", List.of(new StockReservationRequest.ReservationItem("ITM0000001", 2))));

    assertThat(response.reservationId()).isEqualTo("RSV-20260620120000-ABC123");
    assertThat(response.status()).isEqualTo("RESERVED");
    assertThat(response.results().get(0).warehouseLocationCode()).isEqualTo("WH-TYO-01");
    assertThat(stock.getReservedQuantity()).isEqualTo(2);
    assertThat(stock.getAvailableQuantity()).isEqualTo(8);
  }

  @Test
  void shouldReleaseReservedStock() {
    StockItemEntity stock = new StockItemEntity();
    stock.setItemCode("ITM0000001");
    stock.setWarehouseLocationCode("WH-TYO-01");
    stock.setOnHandQuantity(10);
    stock.setReservedQuantity(2);
    stock.setAvailableQuantity(8);

    StockReservationLedgerEntity ledger = new StockReservationLedgerEntity();
    ledger.setReservationId("RSV-1");
    ledger.setItemCode("ITM0000001");
    ledger.setWarehouseLocationCode("WH-TYO-01");
    ledger.setReservedQuantity(2);
    ledger.setReservationStatus("RESERVED");

    when(stockReservationLedgerRepository.findByReservationIdOrderByStockReservationLedgerIdAsc(
            "RSV-1"))
        .thenReturn(List.of(ledger));
    when(stockItemRepository.findByItemCodeAndWarehouseLocationCode("ITM0000001", "WH-TYO-01"))
        .thenReturn(java.util.Optional.of(stock));

    StockReservationOperationResponse response = stockReservationService.release("RSV-1");

    assertThat(response.status()).isEqualTo("RELEASED");
    assertThat(stock.getReservedQuantity()).isEqualTo(0);
    assertThat(stock.getAvailableQuantity()).isEqualTo(10);
  }

  @Test
  void shouldShipConfirmReservedStock() {
    StockItemEntity stock = new StockItemEntity();
    stock.setItemCode("ITM0000001");
    stock.setWarehouseLocationCode("WH-TYO-01");
    stock.setOnHandQuantity(10);
    stock.setReservedQuantity(2);
    stock.setAvailableQuantity(8);

    StockReservationLedgerEntity ledger = new StockReservationLedgerEntity();
    ledger.setReservationId("RSV-1");
    ledger.setItemCode("ITM0000001");
    ledger.setWarehouseLocationCode("WH-TYO-01");
    ledger.setReservedQuantity(2);
    ledger.setReservationStatus("RESERVED");

    when(stockReservationLedgerRepository.findByReservationIdOrderByStockReservationLedgerIdAsc(
            "RSV-1"))
        .thenReturn(List.of(ledger));
    when(stockItemRepository.findByItemCodeAndWarehouseLocationCode("ITM0000001", "WH-TYO-01"))
        .thenReturn(java.util.Optional.of(stock));

    StockReservationOperationResponse response = stockReservationService.shipConfirm("RSV-1");

    assertThat(response.status()).isEqualTo("SHIPPED_CONFIRMED");
    assertThat(stock.getOnHandQuantity()).isEqualTo(8);
    assertThat(stock.getReservedQuantity()).isEqualTo(0);
    assertThat(stock.getAvailableQuantity()).isEqualTo(8);
  }

  @Test
  void shouldFailWhenStockInsufficient() {
    when(stockItemRepository.findByItemCodeOrderByAvailableQuantityDesc("ITM0000001"))
        .thenReturn(List.of());

    assertThatThrownBy(
            () ->
                stockReservationService.reserve(
                    new StockReservationRequest(
                        "O1",
                        List.of(new StockReservationRequest.ReservationItem("ITM0000001", 2)))))
        .isInstanceOf(ResponseStatusException.class);
  }
}
