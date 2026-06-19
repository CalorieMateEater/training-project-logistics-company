package jp.co.hoge.shippinggateway.config;

import jp.co.hoge.orderhub.common.logging.ExternalHttpLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Shipping Gateway API 用の HTTP クライアント設定。
 *
 * @author Takuya Yamamoto
 */
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    /** 外部 HTTP 通信ログインターセプター。 */
    private final ExternalHttpLoggingInterceptor externalHttpLoggingInterceptor;

    /**
     * 共通 RestClient ビルダーを生成する。
     *
     * @return RestClient ビルダー
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestInterceptor(externalHttpLoggingInterceptor);
    }
}
