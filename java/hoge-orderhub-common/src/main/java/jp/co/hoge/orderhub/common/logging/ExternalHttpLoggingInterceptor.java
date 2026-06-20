package jp.co.hoge.orderhub.common.logging;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * 外部 HTTP 通信の送受信ログを記録するインターセプタ。
 *
 * @author Takuya Yamamoto
 */
@Slf4j
@Component
public class ExternalHttpLoggingInterceptor implements ClientHttpRequestInterceptor {

  /** リクエスト ID ヘッダー名。 */
  private static final String HEADER_REQUEST_ID = "X-Request-Id";

  /** トレース ID ヘッダー名。 */
  private static final String HEADER_TRACE_ID = "X-Trace-Id";

  /**
   * 外部通信の前後で送信ログを出力し、必要な追跡ヘッダーを付与する。
   *
   * @param request リクエスト情報
   * @param body リクエストボディ
   * @param execution 実行オブジェクト
   * @return 応答情報
   * @throws IOException 通信失敗時例外
   */
  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    String trackingId = MdcUtils.getOrCreateTrackingId();
    String requestId = org.slf4j.MDC.get(MdcKeys.REQUEST_ID);
    String traceId = org.slf4j.MDC.get(MdcKeys.TRACE_ID);

    if (!request.getHeaders().containsKey(HEADER_REQUEST_ID) && requestId != null) {
      request.getHeaders().add(HEADER_REQUEST_ID, requestId);
    }
    if (!request.getHeaders().containsKey(HEADER_TRACE_ID) && traceId != null) {
      request.getHeaders().add(HEADER_TRACE_ID, traceId);
    }

    log.info(
        "SEND_BEFORE trackingId={} requestId={} traceId={} method={} uri={} bodySize={}",
        trackingId,
        requestId,
        traceId,
        request.getMethod(),
        request.getURI(),
        body.length);
    long startedAt = System.currentTimeMillis();
    try {
      ClientHttpResponse response = execution.execute(request, body);
      log.info(
          "SEND_AFTER trackingId={} requestId={} traceId={} method={} uri={} status={} elapsedMs={}",
          trackingId,
          requestId,
          traceId,
          request.getMethod(),
          request.getURI(),
          response.getStatusCode().value(),
          System.currentTimeMillis() - startedAt);
      return response;
    } catch (IOException exception) {
      log.error(
          "SEND_ERROR trackingId={} requestId={} traceId={} method={} uri={} elapsedMs={} message={}",
          trackingId,
          requestId,
          traceId,
          request.getMethod(),
          request.getURI(),
          System.currentTimeMillis() - startedAt,
          exception.getMessage(),
          exception);
      throw exception;
    }
  }
}
