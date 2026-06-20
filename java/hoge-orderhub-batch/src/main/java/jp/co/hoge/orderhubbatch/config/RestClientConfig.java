package jp.co.hoge.orderhubbatch.config;

import jp.co.hoge.orderhub.common.config.OrderHubInternalApiProperties;
import jp.co.hoge.orderhub.common.logging.ExternalHttpLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * OrderHub Batch 用の HTTP クライアント設定。
 *
 * @author Takuya Yamamoto
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({BatchFileProperties.class, OrderHubInternalApiProperties.class})
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
    return RestClient.builder().requestInterceptor(externalHttpLoggingInterceptor);
  }

  /**
   * 顧客マスタ管理 API 用 RestClient を生成する。
   *
   * @param builder RestClient ビルダー
   * @param properties 内部 API 接続設定
   * @return 顧客マスタ管理 API 用 RestClient
   */
  @Bean("customerRegistryRestClient")
  RestClient customerRegistryRestClient(
      RestClient.Builder builder, OrderHubInternalApiProperties properties) {
    return builder.baseUrl(properties.getCustomerBaseUrl()).build();
  }

  /**
   * 在庫管理 API 用 RestClient を生成する。
   *
   * @param builder RestClient ビルダー
   * @param properties 内部 API 接続設定
   * @return 在庫管理 API 用 RestClient
   */
  @Bean("stockKeeperRestClient")
  RestClient stockKeeperRestClient(
      RestClient.Builder builder, OrderHubInternalApiProperties properties) {
    return builder.baseUrl(properties.getStockBaseUrl()).build();
  }
}
