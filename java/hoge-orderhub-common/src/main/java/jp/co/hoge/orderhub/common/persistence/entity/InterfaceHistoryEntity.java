package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;

/**
 * 外部インターフェース履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "th_interface_history", schema = "orderhub")
public class InterfaceHistoryEntity {

  /** IF履歴 ID。 */
  @Id
  @Column(name = "interface_history_id", nullable = false, length = 40)
  private String interfaceHistoryId;

  /** IF ID。 */
  @Column(name = "if_id", nullable = false, length = 32)
  private String ifId;

  /** 連携方向。 */
  @Enumerated(EnumType.STRING)
  @Column(name = "direction", nullable = false, length = 16)
  private InterfaceDirection direction;

  /** 処理結果状態。 */
  @Enumerated(EnumType.STRING)
  @Column(name = "result_status", nullable = false, length = 16)
  private InterfaceStatus resultStatus;

  /** 要求キー。 */
  @Column(name = "request_key", length = 64)
  private String requestKey;

  /** トレース ID。 */
  @Column(name = "trace_id", length = 64)
  private String traceId;

  /** 結果コード。 */
  @Column(name = "result_code", length = 32)
  private String resultCode;

  /** 結果メッセージ。 */
  @Column(name = "message", length = 512)
  private String message;

  /** 要求日時。 */
  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  /**
   * IF履歴 ID を返却する。
   *
   * @return IF履歴 ID
   */
  public String getInterfaceHistoryId() {
    return interfaceHistoryId;
  }

  /**
   * IF履歴 ID を設定する。
   *
   * @param interfaceHistoryId IF履歴 ID
   */
  public void setInterfaceHistoryId(String interfaceHistoryId) {
    this.interfaceHistoryId = interfaceHistoryId;
  }

  /**
   * IF ID を返却する。
   *
   * @return IF ID
   */
  public String getIfId() {
    return ifId;
  }

  /**
   * IF ID を設定する。
   *
   * @param ifId IF ID
   */
  public void setIfId(String ifId) {
    this.ifId = ifId;
  }

  /**
   * 連携方向を返却する。
   *
   * @return 連携方向
   */
  public InterfaceDirection getDirection() {
    return direction;
  }

  /**
   * 連携方向を設定する。
   *
   * @param direction 連携方向
   */
  public void setDirection(InterfaceDirection direction) {
    this.direction = direction;
  }

  /**
   * 処理結果状態を返却する。
   *
   * @return 処理結果状態
   */
  public InterfaceStatus getResultStatus() {
    return resultStatus;
  }

  /**
   * 処理結果状態を設定する。
   *
   * @param resultStatus 処理結果状態
   */
  public void setResultStatus(InterfaceStatus resultStatus) {
    this.resultStatus = resultStatus;
  }

  /**
   * 要求キーを返却する。
   *
   * @return 要求キー
   */
  public String getRequestKey() {
    return requestKey;
  }

  /**
   * 要求キーを設定する。
   *
   * @param requestKey 要求キー
   */
  public void setRequestKey(String requestKey) {
    this.requestKey = requestKey;
  }

  /**
   * トレース ID を返却する。
   *
   * @return トレース ID
   */
  public String getTraceId() {
    return traceId;
  }

  /**
   * トレース ID を設定する。
   *
   * @param traceId トレース ID
   */
  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  /**
   * 結果コードを返却する。
   *
   * @return 結果コード
   */
  public String getResultCode() {
    return resultCode;
  }

  /**
   * 結果コードを設定する。
   *
   * @param resultCode 結果コード
   */
  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }

  /**
   * 結果メッセージを返却する。
   *
   * @return 結果メッセージ
   */
  public String getMessage() {
    return message;
  }

  /**
   * 結果メッセージを設定する。
   *
   * @param message 結果メッセージ
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * 要求日時を返却する。
   *
   * @return 要求日時
   */
  public LocalDateTime getRequestedAt() {
    return requestedAt;
  }

  /**
   * 要求日時を設定する。
   *
   * @param requestedAt 要求日時
   */
  public void setRequestedAt(LocalDateTime requestedAt) {
    this.requestedAt = requestedAt;
  }
}
