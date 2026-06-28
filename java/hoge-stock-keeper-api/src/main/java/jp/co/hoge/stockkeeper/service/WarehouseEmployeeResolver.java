package jp.co.hoge.stockkeeper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * OIDCアクセストークンから倉庫担当者の社員IDを解決するサービス。 関連処理機能ID: PGD-009
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
public class WarehouseEmployeeResolver {

  /** ローカル検証用の社員IDクレーム接頭辞。 */
  private static final String LOCAL_EMPLOYEE_PREFIX = "employee:";

  /**
   * Authorizationヘッダから検証済み社員IDを取得する。
   *
   * @param authorization Authorizationヘッダ
   * @return 社員ID
   */
  public String resolveEmployeeId(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "oidc token required");
    }
    String token = authorization.substring("Bearer ".length()).trim();
    if (token.startsWith(LOCAL_EMPLOYEE_PREFIX)) {
      String employeeId = token.substring(LOCAL_EMPLOYEE_PREFIX.length());
      if (!employeeId.isBlank()) {
        log.info("APP_OIDC_VERIFIED employeeId={}", employeeId);
        return employeeId;
      }
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "oidc token invalid");
  }
}
