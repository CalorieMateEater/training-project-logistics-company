package jp.co.hoge.shippinggateway.controller;

import jp.co.hoge.orderhub.common.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Shipping Gateway API の例外応答を統一するハンドラー。 関連処理機能ID: PGD-004, PGD-005, PGD-006, PGD-008
 *
 * @author Takuya Yamamoto
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
  /**
   * 業務例外を HTTP ステータス付きで返却する。
   *
   * @param exception 業務例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorResponse> handleStatus(ResponseStatusException exception) {
    HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    log.error(
        "MONITORING_API_ERROR status={} message={}",
        status.value(),
        exception.getReason(),
        exception);
    return ResponseEntity.status(status)
        .body(new ApiErrorResponse("HSG-" + status.value(), exception.getReason()));
  }

  /**
   * 入力バリデーションエラーを 400 応答へ変換する。
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
    log.error("MONITORING_API_VALIDATION message={}", message, exception);
    return ResponseEntity.badRequest().body(new ApiErrorResponse("HSG-REQ-001", message));
  }

  /**
   * 想定外例外を 500 応答へ変換する。
   *
   * @param exception 想定外例外
   * @return エラーレスポンス
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
    log.error("MONITORING_API_UNEXPECTED message={}", exception.getMessage(), exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse("HSG-999", exception.getMessage()));
  }
}
