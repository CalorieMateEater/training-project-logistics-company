package jp.co.hoge.shippinggateway.service;

import java.util.List;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.domain.ShipmentRequestStatus;
import jp.co.hoge.orderhub.common.dto.ShipmentCancelResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.entity.ShipmentRequestEntity;
import jp.co.hoge.orderhub.common.persistence.entity.StockReservationResultEntity;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.persistence.repository.ShipmentRequestRepository;
import jp.co.hoge.orderhub.common.persistence.repository.StockReservationResultRepository;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 未出荷注文の取消を処理するサービス。 関連処理機能ID: PGD-008
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentCancellationService {
  /** 注文ヘッダリポジトリ。 */
  private final OrderHeaderRepository orderHeaderRepository;

  /** 出荷依頼リポジトリ。 */
  private final ShipmentRequestRepository shipmentRequestRepository;

  /** 在庫引当結果リポジトリ。 */
  private final StockReservationResultRepository stockReservationResultRepository;

  /** 在庫管理クライアント。 */
  private final StockKeeperClient stockKeeperClient;

  /** インターフェース履歴記録サービス。 */
  private final InterfaceHistoryService interfaceHistoryService;

  /** 現在時刻提供サービス。 */
  private final TimeProvider timeProvider;

  /**
   * 未出荷注文を取り消す。
   *
   * @param orderId 注文 ID
   * @param clientSystemId 呼出元システム ID
   * @param traceId トレース ID
   * @param cancelReason 取消理由
   * @return 取消結果
   */
  @Transactional
  public ShipmentCancelResponse cancel(
      String orderId, String clientSystemId, String traceId, String cancelReason) {
    ClientSystemId.requireHogeOperations(clientSystemId);
    log.info("APP_CANCEL_START orderId={} cancelReason={}", orderId, cancelReason);

    OrderHeaderEntity orderHeader =
        orderHeaderRepository
            .findById(orderId)
            .orElseThrow(() -> notFound(orderId, traceId, "order not found"));
    ShipmentRequestEntity shipmentRequest =
        shipmentRequestRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> notFound(orderId, traceId, "shipment request not found"));

    if (!isCancelable(orderHeader, shipmentRequest)) {
      interfaceHistoryService.record(
          "IF-HOGE-OPS-001",
          InterfaceDirection.INBOUND,
          InterfaceStatus.FAILED,
          orderId,
          traceId,
          "409",
          "order is not cancelable");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "order is not cancelable");
    }

    List<StockReservationResultEntity> reservationResults =
        stockReservationResultRepository.findByOrderIdOrderByOrderLineNo(orderId);
    for (StockReservationResultEntity reservationResult : reservationResults) {
      if ("RELEASED".equals(reservationResult.getReservationStatus())) {
        continue;
      }
      StockReservationOperationResponse response =
          stockKeeperClient.release(reservationResult.getReservationId());
      response.results().stream()
          .filter(result -> result.itemCode().equals(reservationResult.getItemCode()))
          .findFirst()
          .ifPresent(
              result -> {
                reservationResult.setReservationStatus(result.reservationStatus());
                reservationResult.setReleasedQuantity(result.processedQuantity());
                stockReservationResultRepository.save(reservationResult);
              });
    }

    orderHeader.setOrderStatus(OrderStatus.CANCELLED);
    orderHeader.setShipmentStatus(OrderStatus.CANCELLED);
    orderHeader.setUpdatedAt(timeProvider.now());
    orderHeaderRepository.save(orderHeader);

    shipmentRequest.setShipmentRequestStatus(ShipmentRequestStatus.CANCELLED);
    shipmentRequest.setNextRequestAfter(null);
    shipmentRequestRepository.save(shipmentRequest);

    interfaceHistoryService.record(
        "IF-HOGE-OPS-001",
        InterfaceDirection.INBOUND,
        InterfaceStatus.SUCCESS,
        orderId,
        traceId,
        "200",
        "shipment canceled: " + cancelReason);
    log.info(
        "APP_CANCEL_FINISH orderId={} currentStatus={}", orderId, orderHeader.getOrderStatus());
    return new ShipmentCancelResponse(orderId, "CANCELLED", orderHeader.getOrderStatus().name());
  }

  private boolean isCancelable(
      OrderHeaderEntity orderHeader, ShipmentRequestEntity shipmentRequest) {
    return List.of(
                OrderStatus.WAITING_SHIPPING_RELEASE,
                OrderStatus.WAITING_BAR_REQUEST,
                OrderStatus.RECEIVED)
            .contains(orderHeader.getOrderStatus())
        && List.of(
                ShipmentRequestStatus.PENDING,
                ShipmentRequestStatus.WAITING_BUSINESS_HOURS,
                ShipmentRequestStatus.FAILED)
            .contains(shipmentRequest.getShipmentRequestStatus());
  }

  private ResponseStatusException notFound(String orderId, String traceId, String message) {
    interfaceHistoryService.record(
        "IF-HOGE-OPS-001",
        InterfaceDirection.INBOUND,
        InterfaceStatus.FAILED,
        orderId,
        traceId,
        "404",
        message);
    return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
  }
}
