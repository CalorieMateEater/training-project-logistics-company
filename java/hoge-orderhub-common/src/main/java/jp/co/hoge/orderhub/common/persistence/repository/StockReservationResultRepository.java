package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.List;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫引当結果を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface StockReservationResultRepository extends JpaRepository<StockReservationResultEntity, StockReservationResultId> {

    /**
     * 指定注文の在庫引当結果を明細番号順に取得する。
     *
     * @param orderId 注文 ID
     * @return 在庫引当結果一覧
     */
    List<StockReservationResultEntity> findByOrderIdOrderByOrderLineNo(String orderId);
}
