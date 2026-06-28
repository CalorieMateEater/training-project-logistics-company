package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.DeliveryStatusReflectionStatus;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusReflectionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 配送状態反映要求を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface DeliveryStatusReflectionRequestRepository
    extends JpaRepository<DeliveryStatusReflectionRequestEntity, String> {

  /**
   * 指定状態の反映要求を作成日時順に取得する。
   *
   * @param reflectionStatus 反映要求状態
   * @return 反映要求一覧
   */
  List<DeliveryStatusReflectionRequestEntity> findByReflectionStatusOrderByCreatedAtAsc(
      DeliveryStatusReflectionStatus reflectionStatus);

  /**
   * 指定注文・状態連番の最新反映要求を取得する。
   *
   * @param orderId 注文ID
   * @param statusSeq 配送状態連番
   * @return 最新反映要求
   */
  Optional<DeliveryStatusReflectionRequestEntity>
      findFirstByOrderIdAndStatusSeqOrderByCreatedAtDesc(String orderId, int statusSeq);
}
