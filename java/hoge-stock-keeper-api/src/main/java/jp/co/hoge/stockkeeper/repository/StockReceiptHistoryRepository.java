package jp.co.hoge.stockkeeper.repository;

import java.util.Optional;
import jp.co.hoge.stockkeeper.entity.StockReceiptHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 入庫受付履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockReceiptHistoryRepository
    extends JpaRepository<StockReceiptHistoryEntity, Long> {
  /**
   * 倉庫場所コードと入庫受付番号に一致する入庫受付履歴を取得する。
   *
   * @param warehouseLocationCode 倉庫場所コード
   * @param receiptReferenceNo 入庫受付番号
   * @return 入庫受付履歴
   */
  Optional<StockReceiptHistoryEntity> findByWarehouseLocationCodeAndReceiptReferenceNo(
      String warehouseLocationCode, String receiptReferenceNo);
}
