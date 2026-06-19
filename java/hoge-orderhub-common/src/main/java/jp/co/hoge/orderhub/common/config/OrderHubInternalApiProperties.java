package jp.co.hoge.orderhub.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部 API 接続先プロパティ。
 *
 * @author Takuya Yamamoto
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "hoge.internal")
public class OrderHubInternalApiProperties {
    /** 顧客マスタ管理 API ベース URL。 */
    private String customerBaseUrl;
    /** 在庫引当 API ベース URL。 */
    private String stockBaseUrl;
}
