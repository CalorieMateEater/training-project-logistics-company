package jp.co.hoge.shippinggateway.service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.dto.ShipmentStatusResponse;
import jp.co.hoge.orderhub.common.persistence.entity.DeliveryStatusCurrentEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.repository.DeliveryStatusCurrentRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 出荷状態照会を処理するサービス。 関連処理機能ID: PGD-005
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentStatusQueryService {
  /** レスポンス日時フォーマッタ。 */
  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** 出荷依頼リポジトリ。 */
  private final ShipmentRequestRepository shipmentRequestRepository;

  /** 配送状態最新リポジトリ。 */
  private final DeliveryStatusCurrentRepository deliveryStatusCurrentRepository;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /**
   * 指定された照会キーに対応する出荷状態を返却する。
   *
   * @param lookupKey 注文番号または対向依頼番号
   * @param clientSystemIdRaw 呼出元システム ID
   * @param traceId トレース ID
   * @return 出荷状態照会応答
   */
  public ShipmentStatusResponse findStatus(
      String lookupKey, String clientSystemIdRaw, String traceId) {
    log.info(
        "APP_STATUS_LOOKUP_START lookupKey={} clientSystemId={}", lookupKey, clientSystemIdRaw);
    ClientSystemId clientSystemId = ClientSystemId.parse(clientSystemIdRaw);
    String ifId = clientSystemId.shipmentStatusIfId();

    OrderHeaderEntity orderHeader =
        findOrderHeader(lookupKey, clientSystemId)
            .orElseThrow(() -> notFound(ifId, lookupKey, traceId, "shipment not found"));

    ShipmentRequestEntity shipmentRequest =
        shipmentRequestRepository
            .findByOrderId(orderHeader.getOrderId())
            .orElseThrow(() -> notFound(ifId, lookupKey, traceId, "shipment request not found"));

    DeliveryStatusCurrentEntity current =
        deliveryStatusCurrentRepository.findById(orderHeader.getOrderId()).orElse(null);
    interfaceHistoryService.record(
        ifId,
        InterfaceDirection.INBOUND,
        InterfaceStatus.SUCCESS,
        lookupKey,
        traceId,
        "200",
        "status lookup");

    String currentStatus =
        current == null ? orderHeader.getShipmentStatus().name() : current.getLatestStatusCode();
    String latestStatusAt =
        current == null
            ? orderHeader.getUpdatedAt().format(ISO)
            : current.getLatestStatusAt().format(ISO);

    ShipmentStatusResponse response =
        new ShipmentStatusResponse(
            orderHeader.getPartnerOrderId(),
            orderHeader.getPartnerRequestId(),
            orderHeader.getOrderId(),
            currentStatus,
            orderHeader.getCarrierCode().name(),
            latestStatusAt,
            new ShipmentStatusResponse.Allocation(
                shipmentRequest.getShipmentRequestStatus().name(),
                shipmentRequest.getCarrierCode().name()),
            new ShipmentStatusResponse.LatestEvent(
                current == null
                    ? orderHeader.getShipmentStatus().name()
                    : current.getLatestStatusCode(),
                current == null
                    ? orderHeader.getShipmentStatus().name()
                    : current.getLatestStatusName(),
                current == null ? null : current.getLatestReasonCategory(),
                current == null
                    ? orderHeader.getShipmentStatus().name()
                    : current.getLatestDisplayStatusName() == null
                        ? current.getLatestStatusName()
                        : current.getLatestDisplayStatusName()));
    log.info(
        "APP_STATUS_LOOKUP_FINISH lookupKey={} clientSystemId={} orderId={}",
        lookupKey,
        clientSystemIdRaw,
        response.orderId());
    return response;
  }

  private Optional<OrderHeaderEntity> findOrderHeader(
      String lookupKey, ClientSystemId clientSystemId) {
    Optional<OrderHeaderEntity> byPartnerOrderId =
        orderHeaderRepository.findByPartnerOrderId(lookupKey);
    if (byPartnerOrderId.isPresent()) {
      return byPartnerOrderId;
    }
    if (!clientSystemId.allowsPartnerRequestLookup()) {
      return Optional.empty();
    }
    return orderHeaderRepository.findByPartnerRequestId(lookupKey);
  }

  private ResponseStatusException notFound(
      String ifId, String lookupKey, String traceId, String reason) {
    interfaceHistoryService.record(
        ifId,
        InterfaceDirection.INBOUND,
        InterfaceStatus.FAILED,
        lookupKey,
        traceId,
        "404",
        reason);
    return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
  }
}
