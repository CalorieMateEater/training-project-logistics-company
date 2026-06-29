package jp.co.hoge.shippinggateway.service;

import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 顧客マスタ管理 API を呼び出すクライアント。 関連処理機能ID: PGD-004
 *
 * @author Takuya Yamamoto
 */
@Component
@Slf4j
public class CustomerRegistryClient {
  /** 顧客マスタ管理 API 用 REST クライアント。 */
  private final RestClient restClient;

  /**
   * 顧客マスタ管理APIクライアントを生成する。
   *
   * @param restClient 顧客マスタ管理API用RESTクライアント
   */
  public CustomerRegistryClient(@Qualifier("customerRegistryRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * 顧客状態を取得する。
   *
   * @param customerId 顧客 ID
   * @return 顧客状態応答
   */
  public CustomerStatusResponse findStatus(String customerId) {
    log.info("SEND_REQUEST customerStatus customerId={}", customerId);
    return restClient
        .get()
        .uri("/api/v1/customers/{customerId}/status", customerId)
        .retrieve()
        .body(CustomerStatusResponse.class);
  }
}
