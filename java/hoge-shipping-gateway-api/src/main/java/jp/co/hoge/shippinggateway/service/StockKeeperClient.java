package jp.co.hoge.shippinggateway.service;

import jp.co.hoge.orderhub.common.config.OrderHubInternalApiProperties;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 在庫引当 API を呼び出すクライアント。
 * 関連処理設計書ID: PDS-007
 *
 * @author Takuya Yamamoto
 */
@Component
@Slf4j
public class StockKeeperClient {
    /** 在庫引当 API 用 REST クライアント。 */
    private final RestClient restClient;

    /**
     * 在庫引当 API クライアントを生成する。
     *
     * @param builder RestClient ビルダー
     * @param properties 内部 API 接続設定
     */
    public StockKeeperClient(RestClient.Builder builder, OrderHubInternalApiProperties properties) {
        this.restClient = builder.baseUrl(properties.getStockBaseUrl()).build();
    }

    /**
     * 在庫引当を実行する。
     *
     * @param request 在庫引当要求
     * @return 在庫引当応答
     */
    public StockReservationResponse reserve(StockReservationRequest request) {
        log.info("SEND_REQUEST stockReservation orderId={} itemCount={}", request.orderId(), request.items().size());
        return restClient.post()
                .uri("/api/v1/stocks/reservations")
                .body(request)
                .retrieve()
                .body(StockReservationResponse.class);
    }
}
