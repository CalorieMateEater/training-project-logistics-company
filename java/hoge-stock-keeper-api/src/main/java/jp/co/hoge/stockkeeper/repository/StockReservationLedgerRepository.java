package jp.co.hoge.stockkeeper.repository;

import java.util.List;
import jp.co.hoge.stockkeeper.entity.StockReservationLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫引当台帳を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockReservationLedgerRepository
    extends JpaRepository<StockReservationLedgerEntity, Long> {
  /**
   * 引当 ID に紐づく台帳を取得する。
   *
   * @param reservationId 引当 ID
   * @return 在庫引当台帳一覧
   */
  List<StockReservationLedgerEntity> findByReservationIdOrderByStockReservationLedgerIdAsc(
      String reservationId);
}
