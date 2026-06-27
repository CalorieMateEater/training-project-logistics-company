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
 * 在庫トランザクション履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "th_stock_transaction")
@Getter
@Setter
public class StockTransactionEntity {

  /** 在庫トランザクション ID。 */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stock_tx_id", nullable = false)
  private Long stockTxId;

  /** 引当 ID。 */
  @Column(name = "reservation_id", length = 64)
  private String reservationId;

  /** 入庫受付番号。 */
  @Column(name = "receipt_reference_no", length = 64)
  private String receiptReferenceNo;

  /** 注文 ID。 */
  @Column(name = "order_id", length = 32)
  private String orderId;

  /** 注文明細番号。 */
  @Column(name = "order_line_no")
  private Integer orderLineNo;

  /** 商品コード。 */
  @Column(name = "item_code", nullable = false, length = 32)
  private String itemCode;

  /** 倉庫場所コード。 */
  @Column(name = "warehouse_location_code", nullable = false, length = 32)
  private String warehouseLocationCode;

  /** 在庫取引種別。 */
  @Column(name = "tx_type", nullable = false, length = 32)
  private String txType;

  /** 数量。 */
  @Column(name = "quantity", nullable = false)
  private int quantity;

  /** 更新前保有在庫数。 */
  @Column(name = "before_on_hand_quantity", nullable = false)
  private int beforeOnHandQuantity;

  /** 更新前引当済在庫数。 */
  @Column(name = "before_reserved_quantity", nullable = false)
  private int beforeReservedQuantity;

  /** 更新後保有在庫数。 */
  @Column(name = "after_on_hand_quantity", nullable = false)
  private int afterOnHandQuantity;

  /** 更新後引当済在庫数。 */
  @Column(name = "after_reserved_quantity", nullable = false)
  private int afterReservedQuantity;

  /** 発生日時。 */
  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;
}
