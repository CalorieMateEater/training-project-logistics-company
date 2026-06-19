package jp.co.hoge.orderhub.common.persistence.repository;

import jp.co.hoge.orderhub.common.persistence.entity.BarIdempotencyHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Bar社向け冪等性履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface BarIdempotencyHistoryRepository extends JpaRepository<BarIdempotencyHistoryEntity, String> {
}
