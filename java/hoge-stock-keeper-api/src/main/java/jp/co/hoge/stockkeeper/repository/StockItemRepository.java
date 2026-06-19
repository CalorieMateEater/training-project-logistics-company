package jp.co.hoge.stockkeeper.repository;

import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫商品マスタを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockItemRepository extends JpaRepository<StockItemEntity, String> {
}
