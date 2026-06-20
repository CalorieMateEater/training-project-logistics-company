package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.List;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusHistoryEntity;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 配送状態履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface DeliveryStatusHistoryRepository
    extends JpaRepository<DeliveryStatusHistoryEntity, DeliveryStatusHistoryId> {

  /**
   * 指定注文の配送状態履歴をステータス連番順に取得する。
   *
   * @param orderId 注文 ID
   * @return 配送状態履歴一覧
   */
  List<DeliveryStatusHistoryEntity> findByOrderIdOrderByStatusSeqAsc(String orderId);
}
