package jp.co.hoge.orderhub.common.persistence.repository;

import jp.co.hoge.orderhub.common.persistence.entity.CustomerCheckResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 顧客確認結果を管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface CustomerCheckResultRepository
    extends JpaRepository<CustomerCheckResultEntity, String> {}
