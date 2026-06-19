package jp.co.hoge.orderhub.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 在庫引当 API リクエスト。
 *
 * @param orderId 注文 ID
 * @param items 引当対象商品一覧
 * @author Takuya Yamamoto
 */
public record StockReservationRequest(
        @NotBlank String orderId,
        @Valid @NotEmpty List<ReservationItem> items
) {

    /**
     * 在庫引当対象の商品明細。
     *
     * @param itemCode 商品コード
     * @param quantity 引当数量
     * @author Takuya Yamamoto
     */
    public record ReservationItem(
            @NotBlank String itemCode,
            int quantity
    ) {
    }
}
