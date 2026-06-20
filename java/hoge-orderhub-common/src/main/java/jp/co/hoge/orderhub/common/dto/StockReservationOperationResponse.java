package jp.co.hoge.orderhub.common.dto;

import java.util.List;

/**
 * 在庫引当後続処理 API レスポンス。
 *
 * @param reservationId 引当 ID
 * @param status 処理結果状態
 * @param results 商品別処理結果
 * @author Takuya Yamamoto
 */
public record StockReservationOperationResponse(
    String reservationId, String status, List<OperationResult> results) {

  /**
   * 商品別処理結果。
   *
   * @param itemCode 商品コード
   * @param warehouseLocationCode 倉庫場所コード
   * @param processedQuantity 処理数量
   * @param reservationStatus 引当状態
   * @param onHandQuantity 保有在庫数
   * @param reservedTotalQuantity 引当済在庫数
   * @param availableQuantity 利用可能在庫数
   * @author Takuya Yamamoto
   */
  public record OperationResult(
      String itemCode,
      String warehouseLocationCode,
      int processedQuantity,
      String reservationStatus,
      int onHandQuantity,
      int reservedTotalQuantity,
      int availableQuantity) {}
}
