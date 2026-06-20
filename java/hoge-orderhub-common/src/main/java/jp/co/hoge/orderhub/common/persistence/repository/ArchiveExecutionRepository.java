package jp.co.hoge.orderhub.common.persistence.repository;

import jp.co.hoge.orderhub.common.persistence.entity.ArchiveExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * アーカイブ実行履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface ArchiveExecutionRepository extends JpaRepository<ArchiveExecutionEntity, String> {}
