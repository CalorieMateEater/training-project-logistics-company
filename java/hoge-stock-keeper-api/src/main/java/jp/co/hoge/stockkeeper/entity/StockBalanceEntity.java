package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 倉庫場所別の在庫残高を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_stock_balance")
@Getter
@Setter
public class StockBalanceEntity {

  /** 在庫残高 ID。 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stock_balance_id", nullable = false)
  private Long stockBalanceId;

  /** 商品コード。 */
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** 保有在庫数。 */
  @Column(name = "on_hand_quantity", nullable = false)
  private int onHandQuantity;

  /** 引当済在庫数。 */
  @Column(name = "reserved_quantity", nullable = false)
  private int reservedQuantity;

  /** 最終入庫日時。 */
  @Column(name = "last_received_at")
  private LocalDateTime lastReceivedAt;

  /** 排他制御用バージョン番号。 */
  @Version
  @Column(name = "version_no", nullable = false)
  private long versionNo;

  /** 更新日時。 */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * 利用可能在庫数を返却する。
   *
   * @return 利用可能在庫数
   */
  public int availableQuantity() {
    return onHandQuantity - reservedQuantity;
  }
}
