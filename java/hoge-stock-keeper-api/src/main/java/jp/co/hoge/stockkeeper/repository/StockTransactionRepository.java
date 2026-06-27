package jp.co.hoge.stockkeeper.repository;

import jp.co.hoge.stockkeeper.entity.StockTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫トランザクション履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockTransactionRepository extends JpaRepository<StockTransactionEntity, Long> {}
