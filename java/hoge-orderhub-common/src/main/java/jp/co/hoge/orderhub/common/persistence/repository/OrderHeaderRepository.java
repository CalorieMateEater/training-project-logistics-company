package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.Optional;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 注文ヘッダを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface OrderHeaderRepository extends JpaRepository<OrderHeaderEntity, String> {

  /**
   * 連携先注文 ID から注文ヘッダを取得する。
   *
   * @param partnerOrderId 連携先注文 ID
   * @return 注文ヘッダ
   */
  Optional<OrderHeaderEntity> findByPartnerOrderId(String partnerOrderId);

  /**
   * 連携先要求 ID から注文ヘッダを取得する。
   *
   * @param partnerRequestId 連携先要求 ID
   * @return 注文ヘッダ
   */
  Optional<OrderHeaderEntity> findByPartnerRequestId(String partnerRequestId);
}
