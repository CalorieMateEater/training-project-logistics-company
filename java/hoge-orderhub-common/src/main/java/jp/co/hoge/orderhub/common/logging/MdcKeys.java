package jp.co.hoge.orderhub.common.logging;

/**
 * MDC に格納する共通キー定義。
 *
 * @author Takuya Yamamoto
 */
public final class MdcKeys {

  /** トラッキング ID。 */
  public static final String TRACKING_ID = "trackingId";

  /** リクエスト ID。 */
  public static final String REQUEST_ID = "requestId";

  /** トレース ID。 */
  public static final String TRACE_ID = "traceId";

  /** リクエストパス。 */
  public static final String REQUEST_PATH = "requestPath";

  /** HTTP メソッド。 */
  public static final String HTTP_METHOD = "httpMethod";

  /** 業務要求キー。 */
  public static final String REQUEST_KEY = "requestKey";

  /** 注文 ID。 */
  public static final String ORDER_ID = "orderId";

  /** 外部システム識別子。 */
  public static final String EXTERNAL_SYSTEM = "externalSystem";

  private MdcKeys() {}
}
