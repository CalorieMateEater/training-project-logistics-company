package jp.co.hoge.stockkeeper.controller;

import jakarta.validation.Valid;
import jp.co.hoge.orderhub.common.dto.StockReceiptRequest;
import jp.co.hoge.orderhub.common.dto.StockReceiptResponse;
import jp.co.hoge.orderhub.common.dto.WarehouseStockResponse;
import jp.co.hoge.stockkeeper.service.StockReceiptService;
import jp.co.hoge.stockkeeper.service.WarehouseEmployeeResolver;
import jp.co.hoge.stockkeeper.service.WarehouseInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 倉庫担当者向けの在庫照会・入庫登録 API を提供するコントローラー。 関連処理機能ID: PGD-009
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class WarehouseStockController {
  /** 倉庫在庫照会サービス。 */
  private final WarehouseInventoryService warehouseInventoryService;

  /** 入庫登録サービス。 */
  private final StockReceiptService stockReceiptService;

  /** OIDC社員ID解決サービス。 */
  private final WarehouseEmployeeResolver warehouseEmployeeResolver;

  /**
   * 倉庫担当者の担当倉庫場所に対する在庫を照会する。
   *
   * @param authorization OIDCアクセストークン
   * @param traceId トレース ID
   * @param warehouseLocationCode 倉庫場所コード
   * @param itemCode 商品コード
   * @return 倉庫在庫照会レスポンス
   */
  @GetMapping("/inventories")
  public WarehouseStockResponse searchWarehouseStocks(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
      @RequestParam("warehouse_location_code") String warehouseLocationCode,
      @RequestParam(value = "item_code", required = false) String itemCode) {
    String employeeId = warehouseEmployeeResolver.resolveEmployeeId(authorization);
    return warehouseInventoryService.searchWarehouseStocks(
        employeeId, warehouseLocationCode, itemCode, traceId);
  }

  /**
   * 倉庫担当者が実入庫を登録する。
   *
   * @param authorization OIDCアクセストークン
   * @param traceId トレース ID
   * @param request 入庫登録リクエスト
   * @return 入庫登録レスポンス
   */
  @PostMapping("/receipts")
  public ResponseEntity<StockReceiptResponse> registerStockReceipt(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
      @Valid @RequestBody StockReceiptRequest request) {
    String employeeId = warehouseEmployeeResolver.resolveEmployeeId(authorization);
    StockReceiptService.RegistrationResult result =
        stockReceiptService.registerStockReceipt(request, employeeId, traceId);
    return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(result.response());
  }
}
