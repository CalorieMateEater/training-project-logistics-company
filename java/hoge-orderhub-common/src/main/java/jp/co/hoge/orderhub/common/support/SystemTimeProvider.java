package jp.co.hoge.orderhub.common.support;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * システム時刻を返す TimeProvider 実装。
 *
 * @author Takuya Yamamoto
 */
@Component
@RequiredArgsConstructor
public class SystemTimeProvider implements TimeProvider {
  /** システムクロック。 */
  private final Clock clock;

  /**
   * 現在日時を返す。
   *
   * @return 現在日時
   */
  @Override
  public LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
