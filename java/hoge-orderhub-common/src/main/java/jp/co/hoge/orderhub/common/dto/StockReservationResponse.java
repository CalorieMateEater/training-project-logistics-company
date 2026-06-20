package jp.co.hoge.orderhub.common.dto;

import java.util.List;

/**
 * 在庫引当 API レスポンス。
 *
 * @param reservationId 引当 ID
 * @param status 引当結果状態
 * @param results 商品別引当結果
 * @author Takuya Yamamoto
 */
public record StockReservationResponse(
    String reservationId, String status, List<ReservationResult> results) {

  /**
   * 商品別引当結果。
   *
   * @param itemCode 商品コード
   * @param requestedQuantity 要求数量
   * @param reservedQuantity 引当数量
   * @param warehouseLocationCode 倉庫場所コード
   * @param reservationStatus 引当状態
   * @param onHandQuantity 保有在庫数
   * @param reservedTotalQuantity 引当済在庫数
   * @param availableQuantity 利用可能在庫数
   * @author Takuya Yamamoto
   */
  public record ReservationResult(
      String itemCode,
      int requestedQuantity,
      int reservedQuantity,
      String warehouseLocationCode,
      String reservationStatus,
      int onHandQuantity,
      int reservedTotalQuantity,
      int availableQuantity) {}
}
