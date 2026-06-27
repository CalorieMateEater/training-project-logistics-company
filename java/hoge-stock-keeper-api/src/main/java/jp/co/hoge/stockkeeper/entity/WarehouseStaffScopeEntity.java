package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 倉庫担当者の担当倉庫場所スコープを保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "tm_warehouse_staff_scope")
@Getter
@Setter
public class WarehouseStaffScopeEntity {

  /** 倉庫担当者スコープ ID。 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "warehouse_staff_scope_id", nullable = false)
  private Long warehouseStaffScopeId;

  /** 従業員 ID。 */
  @Column(name = "employee_id", nullable = false, length = 32)
  private String employeeId;

  /** 従業員名。 */
  @Column(name = "employee_name", nullable = false, length = 128)
  private String employeeName;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** ロールコード。 */
  @Column(name = "role_code", nullable = false, length = 32)
  private String roleCode;

  /** 有効フラグ。 */
  @Column(name = "active_flag", nullable = false)
  private boolean activeFlag;
}
