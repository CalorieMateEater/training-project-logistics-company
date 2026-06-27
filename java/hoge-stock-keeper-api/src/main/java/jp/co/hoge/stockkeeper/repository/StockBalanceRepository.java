package jp.co.hoge.stockkeeper.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import jp.co.hoge.stockkeeper.entity.StockBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 在庫残高を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockBalanceRepository extends JpaRepository<StockBalanceEntity, Long> {
  /**
   * 商品コードに紐づく在庫残高を利用可能在庫数降順で取得する。
   *
   * @param itemCode 商品コード
   * @return 在庫残高一覧
   */
  @Query(
      """
      select b
      from StockBalanceEntity b
      where b.itemCode = :itemCode
      order by (b.onHandQuantity - b.reservedQuantity) desc
      """)
  List<StockBalanceEntity> findByItemCodeOrderByAvailableQuantityDesc(
      @Param("itemCode") String itemCode);

  /**
   * 商品コードと倉庫場所コードに一致する在庫残高を取得する。
   *
   * @param itemCode 商品コード
   * @param warehouseLocationCode 倉庫場所コード
   * @return 在庫残高
   */
  Optional<StockBalanceEntity> findByItemCodeAndWarehouseLocationCode(
      String itemCode, String warehouseLocationCode);

  /**
   * 商品コードと倉庫場所コードに一致する在庫残高を排他取得する。
   *
   * @param itemCode 商品コード
   * @param warehouseLocationCode 倉庫場所コード
   * @return 在庫残高
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select b
      from StockBalanceEntity b
      where b.itemCode = :itemCode
        and b.warehouseLocationCode = :warehouseLocationCode
      """)
  Optional<StockBalanceEntity> findForUpdate(
      @Param("itemCode") String itemCode,
      @Param("warehouseLocationCode") String warehouseLocationCode);

  /**
   * 倉庫場所コードに一致する在庫残高を取得する。
   *
   * @param warehouseLocationCode 倉庫場所コード
   * @return 在庫残高一覧
   */
  List<StockBalanceEntity> findByWarehouseLocationCodeOrderByItemCodeAsc(
      String warehouseLocationCode);

  /**
   * 倉庫場所コードと商品コードに一致する在庫残高を取得する。
   *
   * @param warehouseLocationCode 倉庫場所コード
   * @param itemCode 商品コード
   * @return 在庫残高一覧
   */
  List<StockBalanceEntity> findByWarehouseLocationCodeAndItemCodeOrderByItemCodeAsc(
      String warehouseLocationCode, String itemCode);
}
