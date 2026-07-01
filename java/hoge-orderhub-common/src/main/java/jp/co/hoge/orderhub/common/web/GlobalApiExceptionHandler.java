package jp.co.hoge.orderhub.common.web;

import jp.co.hoge.orderhub.common.dto.ApiErrorResponse;
import jp.co.hoge.orderhub.common.logging.MdcKeys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API 例外を集約してエラーレスポンスと監視ログへ変換する共通ハンドラー。
 *
 * @author Takuya Yamamoto
 */
@RestControllerAdvice
@Slf4j
public class GlobalApiExceptionHandler {

  /**
   * 業務例外を HTTP ステータス付きエラーレスポンスへ変換する。
   *
   * @param exception 業務例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorResponse> handleStatus(ResponseStatusException exception) {
    HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    String message =
        exception.getReason() == null || exception.getReason().isBlank()
            ? exception.getMessage()
            : exception.getReason();
    String errorCode = "API-" + status.value();
    logMonitoringError(status, errorCode, message, exception);
    return ResponseEntity.status(status).body(new ApiErrorResponse(errorCode, message));
  }

  /**
   * 入力バリデーション例外を 400 エラーレスポンスへ変換する。
   *
   * @param exception バリデーション例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldError() != null
            ? exception.getBindingResult().getFieldError().getDefaultMessage()
            : exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    String errorCode = "API-REQ-001";
    logMonitoringError(HttpStatus.BAD_REQUEST, errorCode, message, exception);
    return ResponseEntity.badRequest().body(new ApiErrorResponse(errorCode, message));
  }

  /**
   * 想定外例外を 500 エラーレスポンスへ変換する。
   *
   * @param exception 想定外例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
    String message = exception.getMessage();
    String errorCode = "API-999";
    logMonitoringError(HttpStatus.INTERNAL_SERVER_ERROR, errorCode, message, exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse(errorCode, message));
  }

  private void logMonitoringError(
      HttpStatus status, String errorCode, String message, Exception exception) {
    log.error(
        "MONITORING_ERROR trackingId={} requestId={} traceId={} httpMethod={} path={} status={} errorCode={} message={}",
        MDC.get(MdcKeys.TRACKING_ID),
        MDC.get(MdcKeys.REQUEST_ID),
        MDC.get(MdcKeys.TRACE_ID),
        MDC.get(MdcKeys.HTTP_METHOD),
        MDC.get(MdcKeys.REQUEST_PATH),
        status.value(),
        errorCode,
        message,
        exception);
  }
}
