package jp.co.hoge.orderhubworker.service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import jp.co.hoge.orderhub.common.dto.BarShipmentAcceptedResponse;
import jp.co.hoge.orderhub.common.dto.BarShipmentRequestPayload;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bar社配送 API のモッククライアント。
 * 関連処理設計書ID: PDS-003
 *
 * @author Takuya Yamamoto
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MockBarDeliveryClient {

    /** 現在時刻提供サービス。 */
    private final TimeProvider timeProvider;

    /**
     * Bar社向け出荷依頼を模擬的に受け付ける。
     *
     * @param idempotencyKey 冪等キー
     * @param payload 出荷依頼ペイロード
     * @return 出荷依頼受付応答
     */
    public BarShipmentAcceptedResponse requestShipment(String idempotencyKey, BarShipmentRequestPayload payload) {
        log.info(
                "SEND_BEFORE externalSystem=BAR shipmentRequestId={} idempotencyKey={}",
                payload.shipmentRequestId(),
                idempotencyKey
        );
        String barShipmentId = "BARS" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        BarShipmentAcceptedResponse response = new BarShipmentAcceptedResponse(
                barShipmentId,
                payload.shipmentRequestId(),
                "ACCEPTED",
                timeProvider.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                false
        );
        log.info(
                "SEND_AFTER externalSystem=BAR shipmentRequestId={} barShipmentId={} result={}",
                payload.shipmentRequestId(),
                response.barShipmentId(),
                response.acceptanceStatus()
        );
        return response;
    }
}
