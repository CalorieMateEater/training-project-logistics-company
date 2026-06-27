package jp.co.hoge.stockkeeper.repository;

import jp.co.hoge.stockkeeper.entity.WarehouseStaffScopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 倉庫担当者スコープを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface WarehouseStaffScopeRepository
    extends JpaRepository<WarehouseStaffScopeEntity, Long> {
  /**
   * 担当倉庫場所スコープが存在するか判定する。
   *
   * @param employeeId 従業員 ID
   * @param warehouseLocationCode 倉庫場所コード
   * @return 存在する場合 true
   */
  boolean existsByEmployeeIdAndWarehouseLocationCodeAndActiveFlagTrue(
      String employeeId, String warehouseLocationCode);
}
