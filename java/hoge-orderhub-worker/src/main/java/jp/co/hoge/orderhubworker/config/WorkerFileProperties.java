package jp.co.hoge.orderhubworker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Worker ファイル出力設定。
 *
 * @author Takuya Yamamoto
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "hoge.files")
public class WorkerFileProperties {

  /** Foo向け受付通知ファイル出力ディレクトリ。 */
  private String fooAckDir;

  /** Foo向け配送結果通知ファイル出力ディレクトリ。 */
  private String fooStatusDir;
}
