package jp.co.hoge.stockkeeper.service;

import java.util.List;
import jp.co.hoge.orderhub.common.dto.WarehouseStockResponse;
import jp.co.hoge.stockkeeper.entity.StockBalanceEntity;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.entity.WarehouseLocationEntity;
import jp.co.hoge.stockkeeper.repository.StockBalanceRepository;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.WarehouseLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 倉庫担当者向け在庫照会を処理するサービス。
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WarehouseInventoryService {
  /** 倉庫担当者スコープ判定サービス。 */
  private final WarehouseAuthorizationService warehouseAuthorizationService;

  /** 倉庫場所マスタ参照先。 */
  private final WarehouseLocationRepository warehouseLocationRepository;

  /** 商品マスタ参照先。 */
  private final StockItemRepository stockItemRepository;

  /** 在庫残高参照先。 */
  private final StockBalanceRepository stockBalanceRepository;

  /**
   * 倉庫場所別在庫を照会する。
   *
   * @param employeeId 従業員 ID
   * @param warehouseLocationCode 倉庫場所コード
   * @param itemCode 商品コード
   * @param traceId トレース ID
   * @return 倉庫在庫照会レスポンス
   */
  @Transactional(readOnly = true)
  public WarehouseStockResponse searchWarehouseStocks(
      String employeeId, String warehouseLocationCode, String itemCode, String traceId) {
    log.info(
        "APP_STOCK_SEARCH_START employeeId={} warehouseLocationCode={} itemCode={} traceId={}",
        employeeId,
        warehouseLocationCode,
        itemCode,
        traceId);
    WarehouseLocationEntity warehouse = findWarehouse(warehouseLocationCode);
    warehouseAuthorizationService.requireScope(employeeId, warehouseLocationCode);

    if (itemCode != null && !itemCode.isBlank()) {
      requireActiveItem(itemCode);
    }

    List<StockBalanceEntity> balances =
        itemCode == null || itemCode.isBlank()
            ? stockBalanceRepository.findByWarehouseLocationCodeOrderByItemCodeAsc(
                warehouseLocationCode)
            : stockBalanceRepository.findByWarehouseLocationCodeAndItemCodeOrderByItemCodeAsc(
                warehouseLocationCode, itemCode);
    List<WarehouseStockResponse.Stock> stocks =
        balances.stream().map(balance -> toStock(balance, warehouse)).toList();
    log.info(
        "APP_STOCK_SEARCH_FINISH employeeId={} warehouseLocationCode={} count={}",
        employeeId,
        warehouseLocationCode,
        stocks.size());
    return new WarehouseStockResponse(warehouseLocationCode, itemCode, stocks);
  }

  private WarehouseLocationEntity findWarehouse(String warehouseLocationCode) {
    if (warehouseLocationCode == null || warehouseLocationCode.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "warehouse_location_code required");
    }
    return warehouseLocationRepository
        .findByWarehouseLocationCodeAndActiveFlagTrue(warehouseLocationCode)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "warehouse_location_code invalid"));
  }

  private void requireActiveItem(String itemCode) {
    if (!stockItemRepository.existsByItemCodeAndActiveFlagTrue(itemCode)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "item_code invalid");
    }
  }

  private WarehouseStockResponse.Stock toStock(
      StockBalanceEntity balance, WarehouseLocationEntity warehouse) {
    StockItemEntity item =
        stockItemRepository
            .findByItemCodeAndActiveFlagTrue(balance.getItemCode())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "item invalid"));
    return new WarehouseStockResponse.Stock(
        balance.getItemCode(),
        item.getItemName(),
        balance.getWarehouseLocationCode(),
        warehouse.getWarehouseLocationName(),
        balance.getOnHandQuantity(),
        balance.getReservedQuantity(),
        balance.availableQuantity(),
        balance.getLastReceivedAt());
  }
}
