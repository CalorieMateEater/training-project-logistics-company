package jp.co.hoge.orderhub.common.dto;

import java.time.LocalDateTime;

/**
 * 入庫登録 API レスポンス。
 *
 * @param warehouseLocationCode 倉庫場所コード
 * @param itemCode 商品コード
 * @param receivedQuantity 入庫数量
 * @param receiptReferenceNo 入庫受付番号
 * @param onHandQuantity 更新後保有在庫数
 * @param reservedQuantity 更新後引当済在庫数
 * @param availableQuantity 更新後利用可能在庫数
 * @param registeredAt 登録日時
 * @author Takuya Yamamoto
 */
public record StockReceiptResponse(
    String warehouseLocationCode,
    String itemCode,
    int receivedQuantity,
    String receiptReferenceNo,
    int onHandQuantity,
    int reservedQuantity,
    int availableQuantity,
    LocalDateTime registeredAt) {}
