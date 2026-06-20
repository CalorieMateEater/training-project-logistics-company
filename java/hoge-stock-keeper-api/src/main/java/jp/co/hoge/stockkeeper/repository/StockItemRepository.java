package jp.co.hoge.stockkeeper.repository;

import java.util.List;
import java.util.Optional;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫商品マスタを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockItemRepository extends JpaRepository<StockItemEntity, Long> {
  /**
   * 商品コードに紐づく在庫を利用可能在庫数降順で取得する。
   *
   * @param itemCode 商品コード
   * @return 在庫一覧
   */
  List<StockItemEntity> findByItemCodeOrderByAvailableQuantityDesc(String itemCode);

  /**
   * 商品コードと倉庫場所コードに一致する在庫を取得する。
   *
   * @param itemCode 商品コード
   * @param warehouseLocationCode 倉庫場所コード
   * @return 在庫
   */
  Optional<StockItemEntity> findByItemCodeAndWarehouseLocationCode(
      String itemCode, String warehouseLocationCode);
}
