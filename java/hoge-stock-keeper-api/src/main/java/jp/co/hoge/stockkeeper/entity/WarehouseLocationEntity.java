package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 倉庫場所マスタを保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "tm_warehouse_location")
@Getter
@Setter
public class WarehouseLocationEntity {

  /** 倉庫場所コード。 */
  @Id
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** 倉庫場所名。 */
  @Column(name = "warehouse_location_name", nullable = false, length = 128)
  private String warehouseLocationName;

  /** 地域コード。 */
  @Column(name = "region_code", nullable = false, length = 16)
  private String regionCode;

  /** 温度帯。 */
  @Column(name = "temperature_zone", nullable = false, length = 16)
  private String temperatureZone;

  /** 有効フラグ。 */
  @Column(name = "active_flag", nullable = false)
  private boolean activeFlag;
}
