package jp.co.hoge.shippinggateway.controller;

import jakarta.validation.Valid;
import jp.co.hoge.orderhub.common.dto.BarDeliveryResultRequest;
import jp.co.hoge.orderhub.common.dto.ShipmentCancelRequest;
import jp.co.hoge.orderhub.common.dto.ShipmentCancelResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationAcceptedResponse;
import jp.co.hoge.orderhub.common.dto.ShipmentRegistrationRequest;
import jp.co.hoge.orderhub.common.dto.ShipmentStatusResponse;
import jp.co.hoge.shippinggateway.service.BarDeliveryResultService;
import jp.co.hoge.shippinggateway.service.ShipmentCancellationService;
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
 * 出荷依頼受付、出荷状態照会、配送会社結果受付を公開する API コントローラー。 関連処理機能ID: PGD-004, PGD-005, PGD-006, PGD-008
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShipmentGatewayController {

  /** Hoge社直受注登録サービス。 */
  private final ShipmentRegistrationService shipmentRegistrationService;

  /** 出荷状態照会サービス。 */
  private final ShipmentStatusQueryService shipmentStatusQueryService;

  /** 配送会社結果受付サービス。 */
  private final BarDeliveryResultService barDeliveryResultService;

  /** 出荷取消サービス。 */
  private final ShipmentCancellationService shipmentCancellationService;

  /**
   * Hoge社業務部門の直受注を受け付ける。
   *
   * @param request 出荷依頼要求
   * @param clientSystemId 呼出元システム ID
   * @param requestId リクエスト ID
   * @param traceId トレース ID
   * @return 出荷依頼受付応答
   */
  @PostMapping("/shipment-requests")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ShipmentRegistrationAcceptedResponse registerShipment(
      @Valid @RequestBody ShipmentRegistrationRequest request,
      @RequestHeader(name = "X-Client-System-Id") String clientSystemId,
      @RequestHeader(name = "X-Request-Id", required = false) String requestId,
      @RequestHeader(name = "X-Trace-Id", required = false) String traceId) {
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
      @RequestHeader(name = "X-Trace-Id", required = false) String traceId) {
    return shipmentStatusQueryService.findStatus(lookupKey, clientSystemId, traceId);
  }

  /**
   * 配送会社からの配送結果通知を受け付ける。
   *
   * @param carrier 配送会社コード
   * @param request 配送結果通知要求
   */
  @PostMapping("/delivery-results/{carrier}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void acceptDeliveryResult(
      @PathVariable String carrier, @Valid @RequestBody BarDeliveryResultRequest request) {
    barDeliveryResultService.accept(carrier, request);
  }

  /**
   * 内部運用向けに未出荷注文を取り消す。
   *
   * @param orderId 注文 ID
   * @param request 取消要求
   * @param clientSystemId 呼出元システム ID
   * @param traceId トレース ID
   * @return 出荷依頼取消応答
   */
  @PostMapping("/internal/orders/{orderId}/cancel")
  public ShipmentCancelResponse cancelShipment(
      @PathVariable String orderId,
      @Valid @RequestBody ShipmentCancelRequest request,
      @RequestHeader(name = "X-Client-System-Id") String clientSystemId,
      @RequestHeader(name = "X-Trace-Id", required = false) String traceId) {
    return shipmentCancellationService.cancel(
        orderId, clientSystemId, traceId, request.cancelReason());
  }
}
