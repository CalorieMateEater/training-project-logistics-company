package jp.co.hoge.orderhubbatch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * バッチ出力ファイル設定。
 *
 * @author Takuya Yamamoto
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "hoge.files")
public class BatchFileProperties {

  /** アーカイブ出力ファイルディレクトリ。 */
  private String archiveDir;
}
