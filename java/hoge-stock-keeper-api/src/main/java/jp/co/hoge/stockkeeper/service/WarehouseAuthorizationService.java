package jp.co.hoge.stockkeeper.service;

import jp.co.hoge.stockkeeper.repository.WarehouseStaffScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 倉庫担当者の担当倉庫場所スコープを判定するサービス。
 *
 * @author Takuya Yamamoto
 */
@Service
@RequiredArgsConstructor
public class WarehouseAuthorizationService {
  /** 倉庫担当者スコープ参照先。 */
  private final WarehouseStaffScopeRepository warehouseStaffScopeRepository;

  /**
   * 担当倉庫場所へのアクセス権限を検証する。
   *
   * @param employeeId 従業員 ID
   * @param warehouseLocationCode 倉庫場所コード
   */
  public void requireScope(String employeeId, String warehouseLocationCode) {
    if (employeeId == null || employeeId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "employee_id required");
    }
    boolean authorized =
        warehouseStaffScopeRepository.existsByEmployeeIdAndWarehouseLocationCodeAndActiveFlagTrue(
            employeeId, warehouseLocationCode);
    if (!authorized) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "warehouse scope denied");
    }
  }
}
