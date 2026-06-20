package jp.co.hoge.customerregistry.controller;

import jp.co.hoge.customerregistry.service.CustomerStatusService;
import jp.co.hoge.orderhub.common.dto.CustomerStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 顧客状態照会 API を提供するコントローラー。
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerStatusController {
  /** 顧客状態照会サービス。 */
  private final CustomerStatusService customerStatusService;

  /**
   * 顧客 ID から顧客状態を取得する。
   *
   * @param customerId 顧客 ID
   * @return 顧客状態
   */
  @GetMapping("/{customerId}/status")
  public CustomerStatusResponse findStatus(@PathVariable String customerId) {
    return customerStatusService.findStatus(customerId);
  }
}
