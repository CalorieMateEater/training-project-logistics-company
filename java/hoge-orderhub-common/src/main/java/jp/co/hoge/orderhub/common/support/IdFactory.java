package jp.co.hoge.orderhub.common.support;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 業務・運用 ID を採番するサービス。
 *
 * @author Takuya Yamamoto
 */
@Component
@RequiredArgsConstructor
public class IdFactory {
  /** 日時フォーマッタ。 */
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  /** 現在時刻取得サービス。 */
  private final TimeProvider timeProvider;

  /**
   * 注文 ID を採番する。
   *
   * @return 注文 ID
   */
  public String orderId() {
    return "O" + DATE_TIME.format(timeProvider.now()) + shortToken();
  }

  /**
   * 出荷依頼 ID を採番する。
   *
   * @return 出荷依頼 ID
   */
  public String shipmentRequestId() {
    return "SHP-" + DATE_TIME.format(timeProvider.now()) + "-" + shortToken();
  }

  /**
   * 在庫引当 ID を採番する。
   *
   * @return 在庫引当 ID
   */
  public String reservationId() {
    return "RSV-" + DATE_TIME.format(timeProvider.now()) + "-" + shortToken();
  }

  /**
   * 通知 ID を採番する。
   *
   * @return 通知 ID
   */
  public String notificationId() {
    return "NTF-" + DATE_TIME.format(timeProvider.now()) + "-" + shortToken();
  }

  /**
   * IF 履歴 ID を採番する。
   *
   * @return IF 履歴 ID
   */
  public String interfaceHistoryId() {
    return "IFH-" + DATE_TIME.format(timeProvider.now()) + "-" + shortToken();
  }

  /**
   * アーカイブ実行 ID を採番する。
   *
   * @return アーカイブ実行 ID
   */
  public String archiveExecutionId() {
    return "ARC-" + DATE_TIME.format(timeProvider.now()) + "-" + shortToken();
  }

  /**
   * Bar 社向け冪等キーを生成する。
   *
   * @param shipmentRequestId 出荷依頼 ID
   * @return 冪等キー
   */
  public String idempotencyKey(String shipmentRequestId) {
    return "BAR-" + shipmentRequestId;
  }

  private String shortToken() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
  }
}
