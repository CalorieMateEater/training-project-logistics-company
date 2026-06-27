package jp.co.hoge.stockkeeper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.stockkeeper.entity.StockBalanceEntity;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.entity.StockReservationLedgerEntity;
import jp.co.hoge.stockkeeper.repository.StockBalanceRepository;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.StockReservationLedgerRepository;
import jp.co.hoge.stockkeeper.repository.StockTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StockReservationServiceTest {
  @Mock private StockItemRepository stockItemRepository;
  @Mock private StockBalanceRepository stockBalanceRepository;
  @Mock private StockReservationLedgerRepository stockReservationLedgerRepository;
  @Mock private StockTransactionRepository stockTransactionRepository;
  @Mock private IdFactory idFactory;
  @Mock private TimeProvider timeProvider;

  @InjectMocks private StockReservationService stockReservationService;

  @Test
  void shouldReserveStock() {
    StockBalanceEntity stock = balance("ITM0000001", "WH-TYO-01", 10, 0);
    when(stockItemRepository.findByItemCodeAndActiveFlagTrue("ITM0000001"))
        .thenReturn(Optional.of(item("ITM0000001")));
    when(stockBalanceRepository.findByItemCodeOrderByAvailableQuantityDesc("ITM0000001"))
        .thenReturn(List.of(stock));
    when(stockBalanceRepository.findForUpdate("ITM0000001", "WH-TYO-01"))
        .thenReturn(Optional.of(stock));
    when(idFactory.reservationId()).thenReturn("RSV-20260620120000-ABC123");
    when(timeProvider.now()).thenReturn(LocalDateTime.parse("2026-06-20T12:00:00"));

    StockReservationResponse response =
        stockReservationService.reserve(
            new StockReservationRequest(
                "O1", List.of(new StockReservationRequest.ReservationItem("ITM0000001", 2))));

    assertThat(response.reservationId()).isEqualTo("RSV-20260620120000-ABC123");
    assertThat(response.status()).isEqualTo("RESERVED");
    assertThat(response.results().get(0).warehouseLocationCode()).isEqualTo("WH-TYO-01");
    assertThat(stock.getReservedQuantity()).isEqualTo(2);
    assertThat(stock.availableQuantity()).isEqualTo(8);
  }

  @Test
  void shouldReleaseReservedStock() {
    StockBalanceEntity stock = balance("ITM0000001", "WH-TYO-01", 10, 2);
    StockReservationLedgerEntity ledger = ledger("RSV-1", "RESERVED");

    when(stockReservationLedgerRepository.findByReservationIdOrderByStockReservationLedgerIdAsc(
            "RSV-1"))
        .thenReturn(List.of(ledger));
    when(stockBalanceRepository.findForUpdate("ITM0000001", "WH-TYO-01"))
        .thenReturn(Optional.of(stock));
    when(timeProvider.now()).thenReturn(LocalDateTime.parse("2026-06-20T12:00:00"));

    StockReservationOperationResponse response = stockReservationService.release("RSV-1");

    assertThat(response.status()).isEqualTo("RELEASED");
    assertThat(stock.getReservedQuantity()).isEqualTo(0);
    assertThat(stock.availableQuantity()).isEqualTo(10);
  }

  @Test
  void shouldShipConfirmReservedStock() {
    StockBalanceEntity stock = balance("ITM0000001", "WH-TYO-01", 10, 2);
    StockReservationLedgerEntity ledger = ledger("RSV-1", "RESERVED");

    when(stockReservationLedgerRepository.findByReservationIdOrderByStockReservationLedgerIdAsc(
            "RSV-1"))
        .thenReturn(List.of(ledger));
    when(stockBalanceRepository.findForUpdate("ITM0000001", "WH-TYO-01"))
        .thenReturn(Optional.of(stock));
    when(timeProvider.now()).thenReturn(LocalDateTime.parse("2026-06-20T12:00:00"));

    StockReservationOperationResponse response = stockReservationService.shipConfirm("RSV-1");

    assertThat(response.status()).isEqualTo("SHIPPED_CONFIRMED");
    assertThat(stock.getOnHandQuantity()).isEqualTo(8);
    assertThat(stock.getReservedQuantity()).isEqualTo(0);
    assertThat(stock.availableQuantity()).isEqualTo(8);
  }

  @Test
  void shouldFailWhenStockInsufficient() {
    when(stockItemRepository.findByItemCodeAndActiveFlagTrue("ITM0000001"))
        .thenReturn(Optional.of(item("ITM0000001")));
    when(stockBalanceRepository.findByItemCodeOrderByAvailableQuantityDesc("ITM0000001"))
        .thenReturn(List.of());

    assertThatThrownBy(
            () ->
                stockReservationService.reserve(
                    new StockReservationRequest(
                        "O1",
                        List.of(new StockReservationRequest.ReservationItem("ITM0000001", 2)))))
        .isInstanceOf(ResponseStatusException.class);
  }

  private StockBalanceEntity balance(
      String itemCode, String warehouseLocationCode, int onHandQuantity, int reservedQuantity) {
    StockBalanceEntity stock = new StockBalanceEntity();
    stock.setItemCode(itemCode);
    stock.setWarehouseLocationCode(warehouseLocationCode);
    stock.setOnHandQuantity(onHandQuantity);
    stock.setReservedQuantity(reservedQuantity);
    stock.setUpdatedAt(LocalDateTime.parse("2026-06-20T12:00:00"));
    return stock;
  }

  private StockItemEntity item(String itemCode) {
    StockItemEntity item = new StockItemEntity();
    item.setItemCode(itemCode);
    item.setItemName("StandardItemA");
    item.setTemperatureZone("AMBIENT");
    item.setSizeType("NORMAL");
    item.setUnitWeightGram(1000);
    item.setActiveFlag(true);
    return item;
  }

  private StockReservationLedgerEntity ledger(String reservationId, String status) {
    StockReservationLedgerEntity ledger = new StockReservationLedgerEntity();
    ledger.setReservationId(reservationId);
    ledger.setOrderId("O1");
    ledger.setItemCode("ITM0000001");
    ledger.setWarehouseLocationCode("WH-TYO-01");
    ledger.setReservedQuantity(2);
    ledger.setReservationStatus(status);
    return ledger;
  }
}
