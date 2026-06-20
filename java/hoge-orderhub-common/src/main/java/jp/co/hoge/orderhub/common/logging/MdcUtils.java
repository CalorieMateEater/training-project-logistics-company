package jp.co.hoge.orderhub.common.logging;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * MDC 操作を補助するユーティリティ。
 *
 * @author Takuya Yamamoto
 */
public final class MdcUtils {

  private MdcUtils() {}

  /**
   * MDC 上のトラッキング ID を取得し、未設定時は新規採番する。
   *
   * @return トラッキング ID
   */
  public static String getOrCreateTrackingId() {
    String trackingId = MDC.get(MdcKeys.TRACKING_ID);
    if (trackingId == null || trackingId.isBlank()) {
      trackingId =
          "TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
      MDC.put(MdcKeys.TRACKING_ID, trackingId);
    }
    return trackingId;
  }

  /**
   * 要求キーを MDC に設定するスコープを生成する。
   *
   * @param requestKey 要求キー
   * @return MDC スコープ
   */
  public static MdcScope withRequestKey(String requestKey) {
    return withEntries(Map.of(MdcKeys.REQUEST_KEY, requestKey));
  }

  /**
   * 注文 ID を MDC に設定するスコープを生成する。
   *
   * @param orderId 注文 ID
   * @return MDC スコープ
   */
  public static MdcScope withOrder(String orderId) {
    return withEntries(Map.of(MdcKeys.ORDER_ID, orderId));
  }

  /**
   * 外部システム識別子を MDC に設定するスコープを生成する。
   *
   * @param externalSystem 外部システム識別子
   * @return MDC スコープ
   */
  public static MdcScope withExternalSystem(String externalSystem) {
    return withEntries(Map.of(MdcKeys.EXTERNAL_SYSTEM, externalSystem));
  }

  /**
   * 任意キーの MDC 設定スコープを生成する。
   *
   * @param entries 設定するキー一覧
   * @return MDC スコープ
   */
  public static MdcScope withEntries(Map<String, String> entries) {
    return new MdcScope(new LinkedHashMap<>(entries));
  }
}
