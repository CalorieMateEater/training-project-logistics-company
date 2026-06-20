package jp.co.hoge.orderhubworker.controller;

import jp.co.hoge.orderhubworker.service.FooAckNotificationWorkerService;
import jp.co.hoge.orderhubworker.service.FooStatusNotificationWorkerService;
import jp.co.hoge.orderhubworker.service.ShipmentDispatchWorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Worker の手動起動 API を提供するコントローラー。 関連処理機能ID: PGD-001, PGD-002, PGD-003
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/internal/workers")
@RequiredArgsConstructor
public class WorkerController {

  /** 配送会社連携 Worker サービス。 */
  private final ShipmentDispatchWorkerService shipmentDispatchWorkerService;

  /** Foo向け受付通知 Worker サービス。 */
  private final FooAckNotificationWorkerService fooAckNotificationWorkerService;

  /** Foo向け配送結果通知 Worker サービス。 */
  private final FooStatusNotificationWorkerService fooStatusNotificationWorkerService;

  /**
   * Foo向け受付通知 Worker を起動する。
   *
   * @return 実行結果
   */
  @PostMapping("/foo-ack/run")
  public String runFooAck() {
    return "notified=" + fooAckNotificationWorkerService.publishPendingAckNotifications();
  }

  /**
   * 配送会社連携 Worker を起動する。
   *
   * @return 実行結果
   */
  @PostMapping("/dispatch/run")
  public String runDispatch() {
    return "dispatched=" + shipmentDispatchWorkerService.dispatchPendingShipments();
  }

  /**
   * Foo向け配送結果通知 Worker を起動する。
   *
   * @return 実行結果
   */
  @PostMapping("/foo-status/run")
  public String runFooStatus() {
    return "notified=" + fooStatusNotificationWorkerService.publishPendingStatusNotifications();
  }
}
