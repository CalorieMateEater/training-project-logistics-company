package jp.co.hoge.stockkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 入庫受付履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "th_stock_receipt_history")
@Getter
@Setter
public class StockReceiptHistoryEntity {

  /** 入庫受付履歴 ID。 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stock_receipt_history_id", nullable = false)
  private Long stockReceiptHistoryId;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** 入庫受付番号。 */
  @Column(name = "receipt_reference_no", nullable = false, length = 64)
  private String receiptReferenceNo;

  /** 従業員 ID。 */
  @Column(name = "employee_id", nullable = false, length = 32)
  private String employeeId;

  /** 商品コード。 */
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 入庫数量。 */
  @Column(name = "received_quantity", nullable = false)
  private int receivedQuantity;

  /** 更新後保有在庫数。 */
  @Column(name = "after_on_hand_quantity", nullable = false)
  private int afterOnHandQuantity;

  /** 更新後利用可能在庫数。 */
  @Column(name = "after_available_quantity", nullable = false)
  private int afterAvailableQuantity;

  /** 登録日時。 */
  @Column(name = "registered_at", nullable = false)
  private LocalDateTime registeredAt;
}
