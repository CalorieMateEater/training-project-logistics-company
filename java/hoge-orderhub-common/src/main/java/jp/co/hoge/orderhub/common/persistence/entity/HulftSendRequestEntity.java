package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.HulftSendStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HULFT集配信サーバへの送信要求を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "t_hulft_send_request", schema = "orderhub")
@Getter
@Setter
@NoArgsConstructor
public class HulftSendRequestEntity {

  /** HULFT送信要求ID。 */
  @Id
  @Column(name = "hulft_send_request_id", nullable = false, length = 40)
  private String hulftSendRequestId;

  /** IF ID。 */
  @Column(name = "if_id", nullable = false, length = 32)
  private String ifId;

  /** 連携先コード。 */
  @Column(name = "partner_code", nullable = false, length = 32)
  private String partnerCode;

  /** 送信ファイルパス。 */
  @Column(name = "file_path", nullable = false, length = 512)
  private String filePath;

  /** 関連通知ID。 */
  @Column(name = "notification_id", length = 40)
  private String notificationId;

  /** HULFT転送ID。 */
  @Column(name = "hulft_transfer_id", length = 64)
  private String hulftTransferId;

  /** 送信状態。 */
  @Enumerated(EnumType.STRING)
  @Column(name = "send_status", nullable = false, length = 16)
  private HulftSendStatus sendStatus;

  /** エラーメッセージ。 */
  @Column(name = "error_message", length = 512)
  private String errorMessage;

  /** 作成日時。 */
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  /** 更新日時。 */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** 送信要求日時。 */
  @Column(name = "requested_at")
  private LocalDateTime requestedAt;
}
