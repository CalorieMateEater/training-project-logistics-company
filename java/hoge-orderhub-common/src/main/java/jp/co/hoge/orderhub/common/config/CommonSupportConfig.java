package jp.co.hoge.orderhub.common.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 共通サポートコンポーネント設定。
 *
 * @author Takuya Yamamoto
 */
@Configuration
@EnableConfigurationProperties(OrderHubInternalApiProperties.class)
public class CommonSupportConfig {
  /**
   * システム標準クロックを生成する。
   *
   * @return システム標準クロック
   */
  @Bean
  Clock systemClock() {
    return Clock.systemDefaultZone();
  }
}
