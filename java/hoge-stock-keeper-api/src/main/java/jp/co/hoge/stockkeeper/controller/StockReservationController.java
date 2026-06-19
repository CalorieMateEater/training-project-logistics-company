package jp.co.hoge.stockkeeper.controller;

import jakarta.validation.Valid;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.stockkeeper.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在庫引当 API を提供するコントローラー。
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockReservationController {
    /** 在庫引当サービス。 */
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
}
