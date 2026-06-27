package jp.co.hoge.stockkeeper.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.stockkeeper.entity.StockBalanceEntity;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.entity.StockReservationLedgerEntity;
import jp.co.hoge.stockkeeper.entity.StockTransactionEntity;
import jp.co.hoge.stockkeeper.repository.StockBalanceRepository;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.StockReservationLedgerRepository;
import jp.co.hoge.stockkeeper.repository.StockTransactionRepository;
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
  /** 商品マスタ参照先。 */
  private final StockItemRepository stockItemRepository;

  /** 在庫残高参照先。 */
  private final StockBalanceRepository stockBalanceRepository;

  /** 在庫引当台帳参照先。 */
  private final StockReservationLedgerRepository stockReservationLedgerRepository;

  /** 在庫トランザクション履歴参照先。 */
  private final StockTransactionRepository stockTransactionRepository;

  /** ID 採番サービス。 */
  private final IdFactory idFactory;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

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
      StockItemEntity itemMaster =
          stockItemRepository
              .findByItemCodeAndActiveFlagTrue(item.itemCode())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.UNPROCESSABLE_ENTITY, "item not found"));

      StockBalanceEntity balance = selectStock(item.itemCode(), item.quantity());
      int beforeOnHand = balance.getOnHandQuantity();
      int beforeReserved = balance.getReservedQuantity();
      balance.setReservedQuantity(balance.getReservedQuantity() + item.quantity());
      balance.setUpdatedAt(timeProvider.now());
      stockBalanceRepository.save(balance);

      stockReservationLedgerRepository.save(
          toReservationLedger(request.orderId(), reservationId, item, balance));
      stockTransactionRepository.save(
          toTransaction(
              "RESERVE",
              reservationId,
              null,
              request.orderId(),
              item.itemCode(),
              balance.getWarehouseLocationCode(),
              item.quantity(),
              beforeOnHand,
              beforeReserved,
              balance.getOnHandQuantity(),
              balance.getReservedQuantity()));

      results.add(
          new StockReservationResponse.ReservationResult(
              balance.getItemCode(),
              item.quantity(),
              item.quantity(),
              balance.getWarehouseLocationCode(),
              "RESERVED",
              balance.getOnHandQuantity(),
              balance.getReservedQuantity(),
              balance.availableQuantity(),
              itemMaster.getItemName(),
              itemMaster.getUnitWeightGram(),
              itemMaster.getTemperatureZone(),
              itemMaster.getSizeType()));
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

      StockBalanceEntity balance =
          findStock(ledger.getItemCode(), ledger.getWarehouseLocationCode());
      if (!"RELEASED".equals(ledger.getReservationStatus())) {
        int beforeOnHand = balance.getOnHandQuantity();
        int beforeReserved = balance.getReservedQuantity();
        balance.setReservedQuantity(
            Math.max(0, balance.getReservedQuantity() - ledger.getReservedQuantity()));
        balance.setUpdatedAt(timeProvider.now());
        stockBalanceRepository.save(balance);
        ledger.setReservationStatus("RELEASED");
        stockReservationLedgerRepository.save(ledger);
        stockTransactionRepository.save(
            toTransaction(
                "RELEASE",
                reservationId,
                null,
                ledger.getOrderId(),
                ledger.getItemCode(),
                ledger.getWarehouseLocationCode(),
                ledger.getReservedQuantity(),
                beforeOnHand,
                beforeReserved,
                balance.getOnHandQuantity(),
                balance.getReservedQuantity()));
      }

      results.add(toOperationResult(ledger, balance));
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

      StockBalanceEntity balance =
          findStock(ledger.getItemCode(), ledger.getWarehouseLocationCode());
      if (!"SHIPPED_CONFIRMED".equals(ledger.getReservationStatus())) {
        int beforeOnHand = balance.getOnHandQuantity();
        int beforeReserved = balance.getReservedQuantity();
        balance.setOnHandQuantity(balance.getOnHandQuantity() - ledger.getReservedQuantity());
        balance.setReservedQuantity(
            Math.max(0, balance.getReservedQuantity() - ledger.getReservedQuantity()));
        balance.setUpdatedAt(timeProvider.now());
        stockBalanceRepository.save(balance);
        ledger.setReservationStatus("SHIPPED_CONFIRMED");
        stockReservationLedgerRepository.save(ledger);
        stockTransactionRepository.save(
            toTransaction(
                "SHIP_CONFIRM",
                reservationId,
                null,
                ledger.getOrderId(),
                ledger.getItemCode(),
                ledger.getWarehouseLocationCode(),
                ledger.getReservedQuantity(),
                beforeOnHand,
                beforeReserved,
                balance.getOnHandQuantity(),
                balance.getReservedQuantity()));
      }

      results.add(toOperationResult(ledger, balance));
    }

    log.info(
        "APP_STOCK_SHIP_CONFIRM_FINISH reservationId={} resultCount={}",
        reservationId,
        results.size());
    return new StockReservationOperationResponse(reservationId, "SHIPPED_CONFIRMED", results);
  }

  private StockBalanceEntity selectStock(String itemCode, int quantity) {
    return stockBalanceRepository.findByItemCodeOrderByAvailableQuantityDesc(itemCode).stream()
        .filter(balance -> balance.availableQuantity() >= quantity)
        .findFirst()
        .flatMap(
            balance ->
                stockBalanceRepository.findForUpdate(
                    balance.getItemCode(), balance.getWarehouseLocationCode()))
        .filter(balance -> balance.availableQuantity() >= quantity)
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "insufficient stock"));
  }

  private StockBalanceEntity findStock(String itemCode, String warehouseLocationCode) {
    return stockBalanceRepository
        .findForUpdate(itemCode, warehouseLocationCode)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));
  }

  private StockReservationLedgerEntity toReservationLedger(
      String orderId,
      String reservationId,
      StockReservationRequest.ReservationItem item,
      StockBalanceEntity balance) {
    StockReservationLedgerEntity ledger = new StockReservationLedgerEntity();
    ledger.setReservationId(reservationId);
    ledger.setOrderId(orderId);
    ledger.setItemCode(item.itemCode());
    ledger.setWarehouseLocationCode(balance.getWarehouseLocationCode());
    ledger.setRequestedQuantity(item.quantity());
    ledger.setReservedQuantity(item.quantity());
    ledger.setReservationStatus("RESERVED");
    return ledger;
  }

  private StockReservationOperationResponse.OperationResult toOperationResult(
      StockReservationLedgerEntity ledger, StockBalanceEntity balance) {
    return new StockReservationOperationResponse.OperationResult(
        ledger.getItemCode(),
        ledger.getWarehouseLocationCode(),
        ledger.getReservedQuantity(),
        ledger.getReservationStatus(),
        balance.getOnHandQuantity(),
        balance.getReservedQuantity(),
        balance.availableQuantity());
  }

  private StockTransactionEntity toTransaction(
      String txType,
      String reservationId,
      String receiptReferenceNo,
      String orderId,
      String itemCode,
      String warehouseLocationCode,
      int quantity,
      int beforeOnHand,
      int beforeReserved,
      int afterOnHand,
      int afterReserved) {
    StockTransactionEntity transaction = new StockTransactionEntity();
    transaction.setTxType(txType);
    transaction.setReservationId(reservationId);
    transaction.setReceiptReferenceNo(receiptReferenceNo);
    transaction.setOrderId(orderId);
    transaction.setOrderLineNo(1);
    transaction.setItemCode(itemCode);
    transaction.setWarehouseLocationCode(warehouseLocationCode);
    transaction.setQuantity(quantity);
    transaction.setBeforeOnHandQuantity(beforeOnHand);
    transaction.setBeforeReservedQuantity(beforeReserved);
    transaction.setAfterOnHandQuantity(afterOnHand);
    transaction.setAfterReservedQuantity(afterReserved);
    transaction.setOccurredAt(LocalDateTime.from(timeProvider.now()));
    return transaction;
  }
}
