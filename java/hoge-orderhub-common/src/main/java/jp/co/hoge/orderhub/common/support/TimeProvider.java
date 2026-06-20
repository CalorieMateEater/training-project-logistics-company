package jp.co.hoge.orderhub.common.support;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 現在日時取得の抽象化インターフェース。
 *
 * @author Takuya Yamamoto
 */
public interface TimeProvider {
  /**
   * 現在日時を返す。
   *
   * @return 現在日時
   */
  LocalDateTime now();

  /**
   * 現在日付を返す。
   *
   * @return 現在日付
   */
  default LocalDate today() {
    return now().toLocalDate();
  }
}
