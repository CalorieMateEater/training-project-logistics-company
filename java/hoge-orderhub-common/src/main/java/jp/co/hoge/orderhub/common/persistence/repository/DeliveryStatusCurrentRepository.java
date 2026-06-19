package jp.co.hoge.orderhub.common.persistence.repository;

import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 注文ごとの最新配送状態を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface DeliveryStatusCurrentRepository extends JpaRepository<DeliveryStatusCurrentEntity, String> {
}
