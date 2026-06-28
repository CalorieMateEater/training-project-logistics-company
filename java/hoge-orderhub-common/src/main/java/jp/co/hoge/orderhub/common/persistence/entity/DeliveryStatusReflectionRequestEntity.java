package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.DeliveryStatusReflectionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 配送結果受付APIが起票する配送状態反映要求。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_delivery_status_reflection_request", schema = "orderhub")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryStatusReflectionRequestEntity {

  /** 反映要求ID。 */
  @Id
  @Column(name = "reflection_request_id", nullable = false, length = 40)
  private String reflectionRequestId;

  /** 配送会社コード。 */
  @Column(name = "carrier_code", nullable = false, length = 16)
  private String carrierCode;

  /** 配送会社側出荷ID。 */
  @Column(name = "carrier_shipment_id", nullable = false, length = 64)
  private String carrierShipmentId;

  /** 注文ID。 */
  @Column(name = "order_id", nullable = false, length = 32)
  private String orderId;

  /** 連携先注文ID。 */
  @Column(name = "partner_order_id", nullable = false, length = 64)
  private String partnerOrderId;

  /** 配送状態連番。 */
  @Column(name = "status_seq", nullable = false)
  private int statusSeq;

  /** 配送状態コード。 */
  @Column(name = "delivery_status", nullable = false, length = 32)
  private String deliveryStatus;

  /** 配送状態名称。 */
  @Column(name = "status_label", nullable = false, length = 64)
  private String statusLabel;

  /** 配送イベント発生日時。 */
  @Column(name = "event_occurred_at", nullable = false)
  private LocalDateTime eventOccurredAt;

  /** 温度帯。 */
  @Column(name = "temperature_zone", length = 16)
  private String temperatureZone;

  /** サイズ区分。 */
  @Column(name = "size_type", length = 16)
  private String sizeType;

  /** 拠点コード。 */
  @Column(name = "location_code", length = 32)
  private String locationCode;

  /** 理由コード。 */
  @Column(name = "reason_code", length = 32)
  private String reasonCode;

  /** 理由分類。 */
  @Column(name = "reason_category", length = 64)
  private String reasonCategory;

  /** 住所補正有無。 */
  @Column(name = "address_corrected")
  private Boolean addressCorrected;

  /** 住所補正レベル。 */
  @Column(name = "address_correction_level", length = 32)
  private String addressCorrectionLevel;

  /** ドライバーコメント。 */
  @Column(name = "driver_comment", length = 512)
  private String driverComment;

  /** 受信電文ハッシュ。 */
  @Column(name = "raw_payload_hash", nullable = false, length = 64)
  private String rawPayloadHash;

  /** 反映要求状態。 */
  @Enumerated(EnumType.STRING)
  @Column(name = "reflection_status", nullable = false, length = 16)
  private DeliveryStatusReflectionStatus reflectionStatus;

  /** エラーメッセージ。 */
  @Column(name = "error_message", length = 512)
  private String errorMessage;

  /** 作成日時。 */
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  /** 更新日時。 */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** 処理日時。 */
  @Column(name = "processed_at")
  private LocalDateTime processedAt;
}
