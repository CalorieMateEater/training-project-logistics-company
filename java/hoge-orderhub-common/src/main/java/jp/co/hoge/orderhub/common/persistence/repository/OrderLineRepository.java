package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.List;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderLineId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 注文明細を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, OrderLineId> {

  /**
   * 指定注文の明細を明細番号順に取得する。
   *
   * @param orderId 注文 ID
   * @return 注文明細一覧
   */
  List<OrderLineEntity> findByOrderIdOrderByOrderLineNo(String orderId);
}
