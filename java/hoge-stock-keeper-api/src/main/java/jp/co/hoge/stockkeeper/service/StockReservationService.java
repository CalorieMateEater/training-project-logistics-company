package jp.co.hoge.stockkeeper.service;

import java.util.ArrayList;
import java.util.List;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.entity.StockReservationLedgerEntity;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.StockReservationLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 在庫引当処理を実行するサービス。
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockReservationService {
  /** 在庫参照先。 */
  private final StockItemRepository stockItemRepository;

  /** 在庫引当台帳参照先。 */
  private final StockReservationLedgerRepository stockReservationLedgerRepository;

  /** ID 採番サービス。 */
  private final IdFactory idFactory;

  /**
   * 在庫引当を実行する。
   *
   * @param request 在庫引当要求
   * @return 在庫引当結果
   */
  @Transactional
  public StockReservationResponse reserve(StockReservationRequest request) {
    List<StockReservationResponse.ReservationResult> results = new ArrayList<>();
    String reservationId = idFactory.reservationId();
    log.info(
        "APP_STOCK_RESERVE_START orderId={} reservationId={} itemCount={}",
        request.orderId(),
        reservationId,
        request.items().size());

    for (StockReservationRequest.ReservationItem item : request.items()) {
      if (item.quantity() < 1 || item.quantity() > 999) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "quantity out of range");
      }

      StockItemEntity stockItem = selectStock(item.itemCode(), item.quantity());
      if (stockItem == null) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "insufficient stock");
      }

      stockItem.setReservedQuantity(stockItem.getReservedQuantity() + item.quantity());
      stockItem.setAvailableQuantity(calculateAvailable(stockItem));
      stockItemRepository.save(stockItem);
      stockReservationLedgerRepository.save(
          toReservationLedger(request.orderId(), reservationId, item, stockItem));

      results.add(
          new StockReservationResponse.ReservationResult(
              stockItem.getItemCode(),
              item.quantity(),
              item.quantity(),
              stockItem.getWarehouseLocationCode(),
              "RESERVED",
              stockItem.getOnHandQuantity(),
              stockItem.getReservedQuantity(),
              stockItem.getAvailableQuantity()));
    }

    log.info(
        "APP_STOCK_RESERVE_FINISH orderId={} reservationId={} resultCount={}",
        request.orderId(),
        reservationId,
        results.size());
    return new StockReservationResponse(reservationId, "RESERVED", results);
  }

  /**
   * 在庫引当解除を実行する。
   *
   * @param reservationId 引当 ID
   * @return 引当解除結果
   */
  @Transactional
  public StockReservationOperationResponse release(String reservationId) {
    List<StockReservationLedgerEntity> ledgers =
        stockReservationLedgerRepository.findByReservationIdOrderByStockReservationLedgerIdAsc(
            reservationId);
    if (ledgers.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "reservation not found");
    }
    log.info(
        "APP_STOCK_RELEASE_START reservationId={} itemCount={}", reservationId, ledgers.size());

    List<StockReservationOperationResponse.OperationResult> results = new ArrayList<>();
    for (StockReservationLedgerEntity ledger : ledgers) {
      if ("SHIPPED_CONFIRMED".equals(ledger.getReservationStatus())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "already shipped confirmed");
      }

      StockItemEntity stockItem =
          findStock(ledger.getItemCode(), ledger.getWarehouseLocationCode());
      if (!"RELEASED".equals(ledger.getReservationStatus())) {
        stockItem.setReservedQuantity(
            Math.max(0, stockItem.getReservedQuantity() - ledger.getReservedQuantity()));
        stockItem.setAvailableQuantity(calculateAvailable(stockItem));
        stockItemRepository.save(stockItem);
        ledger.setReservationStatus("RELEASED");
        stockReservationLedgerRepository.save(ledger);
      }

      results.add(
          new StockReservationOperationResponse.OperationResult(
              ledger.getItemCode(),
              ledger.getWarehouseLocationCode(),
              ledger.getReservedQuantity(),
              ledger.getReservationStatus(),
              stockItem.getOnHandQuantity(),
              stockItem.getReservedQuantity(),
              stockItem.getAvailableQuantity()));
    }

    log.info(
        "APP_STOCK_RELEASE_FINISH reservationId={} resultCount={}", reservationId, results.size());
    return new StockReservationOperationResponse(reservationId, "RELEASED", results);
  }

  /**
   * 在庫出荷確定を実行する。
   *
   * @param reservationId 引当 ID
   * @return 出荷確定結果
   */
  @Transactional
  public StockReservationOperationResponse shipConfirm(String reservationId) {
    List<StockReservationLedgerEntity> ledgers =
        stockReservationLedgerRepository.findByReservationIdOrderByStockReservationLedgerIdAsc(
            reservationId);
    if (ledgers.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "reservation not found");
    }
    log.info(
        "APP_STOCK_SHIP_CONFIRM_START reservationId={} itemCount={}",
        reservationId,
        ledgers.size());

    List<StockReservationOperationResponse.OperationResult> results = new ArrayList<>();
    for (StockReservationLedgerEntity ledger : ledgers) {
      if ("RELEASED".equals(ledger.getReservationStatus())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "already released");
      }

      StockItemEntity stockItem =
          findStock(ledger.getItemCode(), ledger.getWarehouseLocationCode());
      if (!"SHIPPED_CONFIRMED".equals(ledger.getReservationStatus())) {
        stockItem.setOnHandQuantity(stockItem.getOnHandQuantity() - ledger.getReservedQuantity());
        stockItem.setReservedQuantity(
            Math.max(0, stockItem.getReservedQuantity() - ledger.getReservedQuantity()));
        stockItem.setAvailableQuantity(calculateAvailable(stockItem));
        stockItemRepository.save(stockItem);
        ledger.setReservationStatus("SHIPPED_CONFIRMED");
        stockReservationLedgerRepository.save(ledger);
      }

      results.add(
          new StockReservationOperationResponse.OperationResult(
              ledger.getItemCode(),
              ledger.getWarehouseLocationCode(),
              ledger.getReservedQuantity(),
              ledger.getReservationStatus(),
              stockItem.getOnHandQuantity(),
              stockItem.getReservedQuantity(),
              stockItem.getAvailableQuantity()));
    }

    log.info(
        "APP_STOCK_SHIP_CONFIRM_FINISH reservationId={} resultCount={}",
        reservationId,
        results.size());
    return new StockReservationOperationResponse(reservationId, "SHIPPED_CONFIRMED", results);
  }

  private StockItemEntity selectStock(String itemCode, int quantity) {
    return stockItemRepository.findByItemCodeOrderByAvailableQuantityDesc(itemCode).stream()
        .filter(stockItem -> stockItem.getAvailableQuantity() >= quantity)
        .findFirst()
        .orElse(null);
  }

  private StockItemEntity findStock(String itemCode, String warehouseLocationCode) {
    return stockItemRepository
        .findByItemCodeAndWarehouseLocationCode(itemCode, warehouseLocationCode)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));
  }

  private StockReservationLedgerEntity toReservationLedger(
      String orderId,
      String reservationId,
      StockReservationRequest.ReservationItem item,
      StockItemEntity stockItem) {
    StockReservationLedgerEntity ledger = new StockReservationLedgerEntity();
    ledger.setReservationId(reservationId);
    ledger.setOrderId(orderId);
    ledger.setItemCode(item.itemCode());
    ledger.setWarehouseLocationCode(stockItem.getWarehouseLocationCode());
    ledger.setRequestedQuantity(item.quantity());
    ledger.setReservedQuantity(item.quantity());
    ledger.setReservationStatus("RESERVED");
    return ledger;
  }

  private int calculateAvailable(StockItemEntity stockItem) {
    return stockItem.getOnHandQuantity() - stockItem.getReservedQuantity();
  }
}
