package jp.co.hoge.orderhub.common.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 倉庫在庫照会 API レスポンス。
 *
 * @param warehouseLocationCode 倉庫場所コード
 * @param itemCode 検索商品コード
 * @param stocks 在庫一覧
 * @author Takuya Yamamoto
 */
public record WarehouseStockResponse(
    String warehouseLocationCode, String itemCode, List<Stock> stocks) {

  /**
   * 倉庫場所別在庫。
   *
   * @param itemCode 商品コード
   * @param itemName 商品名
   * @param warehouseLocationCode 倉庫場所コード
   * @param warehouseLocationName 倉庫場所名
   * @param onHandQuantity 保有在庫数
   * @param reservedQuantity 引当済在庫数
   * @param availableQuantity 利用可能在庫数
   * @param lastReceivedAt 最終入庫日時
   * @author Takuya Yamamoto
   */
  public record Stock(
      String itemCode,
      String itemName,
      String warehouseLocationCode,
      String warehouseLocationName,
      int onHandQuantity,
      int reservedQuantity,
      int availableQuantity,
      LocalDateTime lastReceivedAt) {}
}
