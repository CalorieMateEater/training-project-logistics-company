package jp.co.hoge.orderhub.common.persistence.repository;

import java.util.List;
import jp.co.hoge.orderhub.common.domain.HulftSendStatus;
import jp.co.hoge.orderhub.common.persistence.entity.HulftSendRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * HULFT送信要求を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface HulftSendRequestRepository extends JpaRepository<HulftSendRequestEntity, String> {

  /**
   * 指定状態のHULFT送信要求を取得する。
   *
   * @param sendStatus 送信状態
   * @return HULFT送信要求一覧
   */
  List<HulftSendRequestEntity> findBySendStatusOrderByCreatedAtAsc(HulftSendStatus sendStatus);
}
