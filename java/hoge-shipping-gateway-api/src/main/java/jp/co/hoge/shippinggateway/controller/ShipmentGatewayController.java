package jp.co.hoge.shippinggateway.controller;

import jakarta.validation.Valid;
import jp.co.hoge.orderhub.common.dto.BarDeliveryResultRequest;
import jp.co.hoge.orderhub.common.dto.FugaShipmentAcceptedResponse;
import jp.co.hoge.orderhub.common.dto.FugaShipmentRequest;
import jp.co.hoge.orderhub.common.dto.ShipmentStatusResponse;
import jp.co.hoge.shippinggateway.service.BarDeliveryResultService;
import jp.co.hoge.shippinggateway.service.ShipmentRegistrationService;
import jp.co.hoge.shippinggateway.service.ShipmentStatusQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出荷依頼受付、出荷状態照会、Bar社配送結果受付を公開する API コントローラー。
 * 関連処理設計書ID: PDS-004, PDS-007, PDS-008
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShipmentGatewayController {

    /** Fuga社からの出荷依頼受付サービス。 */
    private final ShipmentRegistrationService shipmentRegistrationService;

    /** 出荷状態照会サービス。 */
    private final ShipmentStatusQueryService shipmentStatusQueryService;

    /** Bar社配送結果受付サービス。 */
    private final BarDeliveryResultService barDeliveryResultService;

    /**
     * Fuga社からの出荷依頼を受け付ける。
     *
     * @param request 出荷依頼要求
     * @param clientSystemId 呼出元システム ID
     * @param requestId リクエスト ID
     * @param traceId トレース ID
     * @return 出荷依頼受付応答
     */
    @PostMapping("/shipment-requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public FugaShipmentAcceptedResponse registerShipment(
            @Valid @RequestBody FugaShipmentRequest request,
            @RequestHeader(name = "X-Client-System-Id") String clientSystemId,
            @RequestHeader(name = "X-Request-Id", required = false) String requestId,
            @RequestHeader(name = "X-Trace-Id", required = false) String traceId
    ) {
        return shipmentRegistrationService.register(request, clientSystemId, requestId, traceId);
    }

    /**
     * 指定キーに対応する出荷状態を返却する。
     *
     * @param lookupKey 注文番号または出荷依頼番号
     * @param clientSystemId 呼出元システム ID
     * @param traceId トレース ID
     * @return 出荷状態照会応答
     */
    @GetMapping("/shipment-status/{lookupKey}")
    public ShipmentStatusResponse findStatus(
            @PathVariable String lookupKey,
            @RequestHeader(name = "X-Client-System-Id") String clientSystemId,
            @RequestHeader(name = "X-Trace-Id", required = false) String traceId
    ) {
        return shipmentStatusQueryService.findStatus(lookupKey, clientSystemId, traceId);
    }

    /**
     * Bar社からの配送結果通知を受け付ける。
     *
     * @param request 配送結果通知要求
     */
    @PostMapping("/delivery-results/bar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void acceptBarResult(@Valid @RequestBody BarDeliveryResultRequest request) {
        barDeliveryResultService.accept(request);
    }
}
