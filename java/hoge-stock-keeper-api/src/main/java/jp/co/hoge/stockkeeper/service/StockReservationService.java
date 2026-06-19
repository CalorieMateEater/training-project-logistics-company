package jp.co.hoge.stockkeeper.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jp.co.hoge.orderhub.common.dto.StockReservationRequest;
import jp.co.hoge.orderhub.common.dto.StockReservationResponse;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 在庫引当処理を実行するサービス。
 *
 * @author Takuya Yamamoto
 */
@Service
@RequiredArgsConstructor
public class StockReservationService {
    /** 在庫参照先。 */
    private final StockItemRepository stockItemRepository;

    /**
     * 在庫引当を実行する。
     *
     * @param request 在庫引当要求
     * @return 在庫引当結果
     */
    @Transactional
    public StockReservationResponse reserve(StockReservationRequest request) {
        List<StockReservationResponse.ReservationResult> results = new ArrayList<>();
        String reservationId = "RSV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        for (StockReservationRequest.ReservationItem item : request.items()) {
            StockItemEntity stockItem = stockItemRepository.findById(item.itemCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "unknown item"));

            if (item.quantity() < 1 || item.quantity() > 999) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "quantity out of range");
            }

            if (stockItem.getAvailableQuantity() < item.quantity()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "insufficient stock");
            }

            stockItem.setAvailableQuantity(stockItem.getAvailableQuantity() - item.quantity());
            stockItemRepository.save(stockItem);

            results.add(new StockReservationResponse.ReservationResult(
                    stockItem.getItemCode(),
                    item.quantity(),
                    item.quantity(),
                    "RESERVED"
            ));
        }

        return new StockReservationResponse(reservationId, "RESERVED", results);
    }
}
