package jp.co.hoge.orderhub.common.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 出荷依頼情報を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface ShipmentRequestRepository extends JpaRepository<ShipmentRequestEntity, String> {

    /**
     * 注文 ID から出荷依頼を取得する。
     *
     * @param orderId 注文 ID
     * @return 出荷依頼
     */
    Optional<ShipmentRequestEntity> findByOrderId(String orderId);

    /**
     * 指定状態かつ再送時刻到達済みの出荷依頼を再送予定時刻順に取得する。
     *
     * @param statuses 対象状態一覧
     * @param threshold 再送判定基準時刻
     * @return 再送対象出荷依頼一覧
     */
    List<ShipmentRequestEntity> findByShipmentRequestStatusInAndNextRequestAfterLessThanEqualOrderByNextRequestAfterAsc(
            Collection<ShipmentRequestStatus> statuses,
            LocalDateTime threshold
    );
}
