package jp.co.hoge.orderhubbatch.service;

import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 在庫引当 API を呼び出すクライアント。 関連処理機能ID: PGD-001
 *
 * @author Takuya Yamamoto
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StockKeeperClient {
  /** 在庫引当 API 用 REST クライアント。 */
  @Qualifier("stockKeeperRestClient")
  private final RestClient restClient;

  /**
   * 在庫引当を実行する。
   *
   * @param request 在庫引当要求
   * @return 在庫引当応答
   */
  public StockReservationResponse reserve(StockReservationRequest request) {
    log.info(
        "SEND_REQUEST stockReservation orderId={} itemCount={}",
        request.orderId(),
        request.items().size());
    return restClient
        .post()
        .uri("/api/v1/stocks/reservations")
        .body(request)
        .retrieve()
        .body(StockReservationResponse.class);
  }
}
