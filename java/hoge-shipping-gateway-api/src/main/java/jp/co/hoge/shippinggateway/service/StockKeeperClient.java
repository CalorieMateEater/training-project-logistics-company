package jp.co.hoge.shippinggateway.service;

import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 在庫引当 API を呼び出すクライアント。 関連処理機能ID: PGD-004, PGD-006, PGD-008
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

  /**
   * 在庫出荷確定を実行する。
   *
   * @param reservationId 引当 ID
   * @return 在庫出荷確定応答
   */
  public StockReservationOperationResponse shipConfirm(String reservationId) {
    log.info("SEND_REQUEST stockShipConfirm reservationId={}", reservationId);
    return restClient
        .post()
        .uri("/api/v1/stocks/reservations/{reservationId}/ship-confirms", reservationId)
        .retrieve()
        .body(StockReservationOperationResponse.class);
  }

  /**
   * 在庫引当解除を実行する。
   *
   * @param reservationId 引当 ID
   * @return 在庫引当解除応答
   */
  public StockReservationOperationResponse release(String reservationId) {
    log.info("SEND_REQUEST stockRelease reservationId={}", reservationId);
    return restClient
        .post()
        .uri("/api/v1/stocks/reservations/{reservationId}/releases", reservationId)
        .retrieve()
        .body(StockReservationOperationResponse.class);
  }
}
