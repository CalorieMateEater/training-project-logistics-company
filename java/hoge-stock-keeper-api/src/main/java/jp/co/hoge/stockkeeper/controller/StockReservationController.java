package jp.co.hoge.stockkeeper.controller;

import jakarta.validation.Valid;
import jp.co.hoge.orderhub.common.dto.StockReservationOperationResponse;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.stockkeeper.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在庫管理 API を提供するコントローラー。
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockReservationController {
  /** 在庫管理サービス。 */
  private final StockReservationService stockReservationService;

  /**
   * 在庫引当を実行する。
   *
   * @param request 在庫引当要求
   * @return 在庫引当結果
   */
  @PostMapping("/reservations")
  public StockReservationResponse reserve(@Valid @RequestBody StockReservationRequest request) {
    return stockReservationService.reserve(request);
  }

  /**
   * 在庫引当解除を実行する。
   *
   * @param reservationId 引当 ID
   * @return 引当解除結果
   */
  @PostMapping("/reservations/{reservationId}/releases")
  public StockReservationOperationResponse release(@PathVariable String reservationId) {
    return stockReservationService.release(reservationId);
  }

  /**
   * 在庫出荷確定を実行する。
   *
   * @param reservationId 引当 ID
   * @return 出荷確定結果
   */
  @PostMapping("/reservations/{reservationId}/ship-confirms")
  public StockReservationOperationResponse shipConfirm(@PathVariable String reservationId) {
    return stockReservationService.shipConfirm(reservationId);
  }
}
