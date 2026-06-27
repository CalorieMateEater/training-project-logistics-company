package jp.co.hoge.stockkeeper.repository;

import java.util.Optional;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫商品マスタを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockItemRepository extends JpaRepository<StockItemEntity, String> {
  /**
   * 商品コードに一致する有効な商品マスタを取得する。
   *
   * @param itemCode 商品コード
   * @return 商品マスタ
   */
  Optional<StockItemEntity> findByItemCodeAndActiveFlagTrue(String itemCode);

  /**
   * 商品コードに一致する有効な商品マスタが存在するか判定する。
   *
   * @param itemCode 商品コード
   * @return 存在する場合 true
   */
  boolean existsByItemCodeAndActiveFlagTrue(String itemCode);
}
