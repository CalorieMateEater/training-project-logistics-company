package jp.co.hoge.orderhub.common.domain;

import java.util.Arrays;

/**
 * 注文種別。
 *
 * @author Takuya Yamamoto
 */
public enum OrderType {
  NORMAL("01"),
  RESERVED("02"),
  RETURN_EXCHANGE("03");

  /** 対外連携用コード値。 */
  private final String code;

  OrderType(String code) {
    this.code = code;
  }

  /**
   * 注文種別コードを返却する。
   *
   * @return 注文種別コード
   */
  public String getCode() {
    return code;
  }

  /**
   * コード値から注文種別を解決する。
   *
   * @param code 注文種別コード
   * @return 注文種別
   */
  public static OrderType fromCode(String code) {
    return Arrays.stream(values())
        .filter(value -> value.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported order_type: " + code));
  }
}
