package jp.co.hoge.stockkeeper.service;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.dto.StockReceiptRequest;
import jp.co.hoge.orderhub.common.dto.StockReceiptResponse;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.stockkeeper.entity.StockBalanceEntity;
import jp.co.hoge.stockkeeper.entity.StockReceiptHistoryEntity;
import jp.co.hoge.stockkeeper.entity.StockTransactionEntity;
import jp.co.hoge.stockkeeper.repository.StockBalanceRepository;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.StockReceiptHistoryRepository;
import jp.co.hoge.stockkeeper.repository.StockTransactionRepository;
import jp.co.hoge.stockkeeper.repository.WarehouseLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 倉庫担当者向け入庫登録を処理するサービス。
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockReceiptService {
  /** 倉庫担当者スコープ判定サービス。 */
  private final WarehouseAuthorizationService warehouseAuthorizationService;

  /** 倉庫場所マスタ参照先。 */
  private final WarehouseLocationRepository warehouseLocationRepository;

  /** 商品マスタ参照先。 */
  private final StockItemRepository stockItemRepository;

  /** 在庫残高参照先。 */
  private final StockBalanceRepository stockBalanceRepository;

  /** 入庫受付履歴参照先。 */
  private final StockReceiptHistoryRepository stockReceiptHistoryRepository;

  /** 在庫トランザクション履歴参照先。 */
  private final StockTransactionRepository stockTransactionRepository;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /**
   * 入庫登録を実行する。
   *
   * @param request 入庫登録リクエスト
   * @param employeeId 従業員 ID
   * @param traceId トレース ID
   * @return 入庫登録結果
   */
  @Transactional
  public RegistrationResult registerStockReceipt(
      StockReceiptRequest request, String employeeId, String traceId) {
    log.info(
        "APP_STOCK_RECEIPT_START employeeId={} warehouseLocationCode={} itemCode={} receiptReferenceNo={} traceId={}",
        employeeId,
        request.warehouseLocationCode(),
        request.itemCode(),
        request.receiptReferenceNo(),
        traceId);
    validateRequest(request);
    warehouseAuthorizationService.requireScope(employeeId, request.warehouseLocationCode());

    StockReceiptHistoryEntity existing =
        stockReceiptHistoryRepository
            .findByWarehouseLocationCodeAndReceiptReferenceNo(
                request.warehouseLocationCode(), request.receiptReferenceNo())
            .orElse(null);
    if (existing != null) {
      return handleDuplicate(request, existing);
    }

    LocalDateTime now = timeProvider.now();
    StockBalanceEntity balance =
        stockBalanceRepository
            .findForUpdate(request.itemCode(), request.warehouseLocationCode())
            .orElseGet(() -> newBalance(request, now));
    int beforeOnHand = balance.getOnHandQuantity();
    int beforeReserved = balance.getReservedQuantity();
    balance.setOnHandQuantity(balance.getOnHandQuantity() + request.receivedQuantity());
    balance.setLastReceivedAt(now);
    balance.setUpdatedAt(now);
    stockBalanceRepository.save(balance);

    StockReceiptHistoryEntity history = toReceiptHistory(request, employeeId, balance, now);
    stockReceiptHistoryRepository.save(history);
    stockTransactionRepository.save(
        toTransaction(request, balance, beforeOnHand, beforeReserved, now));

    StockReceiptResponse response = toResponse(request, balance, now);
    log.info(
        "APP_STOCK_RECEIPT_FINISH employeeId={} warehouseLocationCode={} itemCode={} receiptReferenceNo={} result=created",
        employeeId,
        request.warehouseLocationCode(),
        request.itemCode(),
        request.receiptReferenceNo());
    return new RegistrationResult(response, true);
  }

  private void validateRequest(StockReceiptRequest request) {
    if (request.warehouseLocationCode() == null || request.warehouseLocationCode().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "warehouse_location_code required");
    }
    if (request.itemCode() == null || request.itemCode().isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "item_code required");
    }
    if (request.receivedQuantity() < 1 || request.receivedQuantity() > 999999) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "received_quantity invalid");
    }
    if (request.receiptReferenceNo() == null || request.receiptReferenceNo().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "receipt_reference_no required");
    }
    warehouseLocationRepository
        .findByWarehouseLocationCodeAndActiveFlagTrue(request.warehouseLocationCode())
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "warehouse_location_code invalid"));
    if (!stockItemRepository.existsByItemCodeAndActiveFlagTrue(request.itemCode())) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "item_code invalid");
    }
  }

  private RegistrationResult handleDuplicate(
      StockReceiptRequest request, StockReceiptHistoryEntity existing) {
    boolean sameContent =
        existing.getItemCode().equals(request.itemCode())
            && existing.getReceivedQuantity() == request.receivedQuantity();
    if (!sameContent) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "receipt_reference_no conflict");
    }
    int reservedQuantity = existing.getAfterOnHandQuantity() - existing.getAfterAvailableQuantity();
    StockReceiptResponse response =
        new StockReceiptResponse(
            existing.getWarehouseLocationCode(),
            existing.getItemCode(),
            existing.getReceivedQuantity(),
            existing.getReceiptReferenceNo(),
            existing.getAfterOnHandQuantity(),
            reservedQuantity,
            existing.getAfterAvailableQuantity(),
            existing.getRegisteredAt());
    log.info(
        "APP_STOCK_RECEIPT_FINISH warehouseLocationCode={} itemCode={} receiptReferenceNo={} result=duplicate",
        request.warehouseLocationCode(),
        request.itemCode(),
        request.receiptReferenceNo());
    return new RegistrationResult(response, false);
  }

  private StockBalanceEntity newBalance(StockReceiptRequest request, LocalDateTime now) {
    StockBalanceEntity balance = new StockBalanceEntity();
    balance.setItemCode(request.itemCode());
    balance.setWarehouseLocationCode(request.warehouseLocationCode());
    balance.setOnHandQuantity(0);
    balance.setReservedQuantity(0);
    balance.setLastReceivedAt(null);
    balance.setUpdatedAt(now);
    return balance;
  }

  private StockReceiptHistoryEntity toReceiptHistory(
      StockReceiptRequest request,
      String employeeId,
      StockBalanceEntity balance,
      LocalDateTime registeredAt) {
    StockReceiptHistoryEntity history = new StockReceiptHistoryEntity();
    history.setWarehouseLocationCode(request.warehouseLocationCode());
    history.setReceiptReferenceNo(request.receiptReferenceNo());
    history.setEmployeeId(employeeId);
    history.setItemCode(request.itemCode());
    history.setReceivedQuantity(request.receivedQuantity());
    history.setAfterOnHandQuantity(balance.getOnHandQuantity());
    history.setAfterAvailableQuantity(balance.availableQuantity());
    history.setRegisteredAt(registeredAt);
    return history;
  }

  private StockTransactionEntity toTransaction(
      StockReceiptRequest request,
      StockBalanceEntity balance,
      int beforeOnHand,
      int beforeReserved,
      LocalDateTime occurredAt) {
    StockTransactionEntity transaction = new StockTransactionEntity();
    transaction.setTxType("RECEIPT");
    transaction.setReceiptReferenceNo(request.receiptReferenceNo());
    transaction.setItemCode(request.itemCode());
    transaction.setWarehouseLocationCode(request.warehouseLocationCode());
    transaction.setQuantity(request.receivedQuantity());
    transaction.setBeforeOnHandQuantity(beforeOnHand);
    transaction.setBeforeReservedQuantity(beforeReserved);
    transaction.setAfterOnHandQuantity(balance.getOnHandQuantity());
    transaction.setAfterReservedQuantity(balance.getReservedQuantity());
    transaction.setOccurredAt(occurredAt);
    return transaction;
  }

  private StockReceiptResponse toResponse(
      StockReceiptRequest request, StockBalanceEntity balance, LocalDateTime registeredAt) {
    return new StockReceiptResponse(
        request.warehouseLocationCode(),
        request.itemCode(),
        request.receivedQuantity(),
        request.receiptReferenceNo(),
        balance.getOnHandQuantity(),
        balance.getReservedQuantity(),
        balance.availableQuantity(),
        registeredAt);
  }

  /**
   * 入庫登録結果。
   *
   * @param response レスポンス
   * @param created 新規登録の場合 true
   * @author Takuya Yamamoto
   */
  public record RegistrationResult(StockReceiptResponse response, boolean created) {}
}
