package jp.co.hoge.shippinggateway.service;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Shipping Gateway API を利用する外部システム種別。
 *
 * @author Takuya Yamamoto
 */
enum ClientSystemId {
  HOGE_DIRECT_PORTAL("HOGE-DIRECT-PORTAL", "IF-HOGE-OPS-002", true),
  FOO_STATUS_CLIENT("FOO-STATUS-CLIENT", "IF-FOO-HOGE-003", false),
  HOGE_OPERATIONS("HOGE-OPS-PORTAL", "IF-HOGE-OPS-001", false);

  /** ヘッダー受信値。 */
  private final String value;

  /** 出荷状態照会 IF ID。 */
  private final String shipmentStatusIfId;

  /** 依頼番号照会可否。 */
  private final boolean partnerRequestLookupAllowed;

  ClientSystemId(String value, String shipmentStatusIfId, boolean partnerRequestLookupAllowed) {
    this.value = value;
    this.shipmentStatusIfId = shipmentStatusIfId;
    this.partnerRequestLookupAllowed = partnerRequestLookupAllowed;
  }

  /**
   * 出荷状態照会 IF ID を返す。
   *
   * @return IF ID
   */
  String shipmentStatusIfId() {
    return shipmentStatusIfId;
  }

  /**
   * 依頼番号での照会可否を返す。
   *
   * @return true の場合は依頼番号照会可
   */
  boolean allowsPartnerRequestLookup() {
    return partnerRequestLookupAllowed;
  }

  /**
   * ヘッダー値からクライアントシステムを解決する。
   *
   * @param rawValue ヘッダー受信値
   * @return クライアントシステム種別
   */
  static ClientSystemId parse(String rawValue) {
    return Arrays.stream(values())
        .filter(clientSystemId -> clientSystemId.value.equals(rawValue))
        .findFirst()
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "client_system_id not allowed"));
  }

  /**
   * Hoge直受注ポータルからの呼出であることを検証する。
   *
   * @param rawValue ヘッダー受信値
   */
  static void requireHogeDirectPortal(String rawValue) {
    if (parse(rawValue) != HOGE_DIRECT_PORTAL) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "client_system_id not allowed");
    }
  }

  /**
   * Hoge 運用ポータルからの呼出であることを検証する。
   *
   * @param rawValue ヘッダー受信値
   */
  static void requireHogeOperations(String rawValue) {
    if (parse(rawValue) != HOGE_OPERATIONS) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "client_system_id not allowed");
    }
  }
}
