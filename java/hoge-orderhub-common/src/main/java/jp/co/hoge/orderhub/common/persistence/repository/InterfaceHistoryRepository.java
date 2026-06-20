package jp.co.hoge.orderhub.common.persistence.repository;

import jp.co.hoge.orderhub.common.persistence.entity.InterfaceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * IF履歴を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface InterfaceHistoryRepository extends JpaRepository<InterfaceHistoryEntity, String> {}
