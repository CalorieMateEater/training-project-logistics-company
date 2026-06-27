package jp.co.hoge.orderhub.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 入庫登録 API リクエスト。
 *
 * @param warehouseLocationCode 倉庫場所コード
 * @param itemCode 商品コード
 * @param receivedQuantity 入庫数量
 * @param receiptReferenceNo 入庫受付番号
 * @author Takuya Yamamoto
 */
public record StockReceiptRequest(
    @NotBlank String warehouseLocationCode,
    @NotBlank String itemCode,
    @Min(1) @Max(999999) int receivedQuantity,
    @NotBlank String receiptReferenceNo) {}
