package jp.co.hoge.customerregistry.repository;

import jp.co.hoge.customerregistry.entity.CustomerMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 顧客マスタを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface CustomerMasterRepository extends JpaRepository<CustomerMasterEntity, String> {}
