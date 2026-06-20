package jp.co.hoge.orderhub.common.dto;

/**
 * 出荷依頼取消応答。
 *
 * @param orderId 注文 ID
 * @param cancelStatus 取消結果
 * @param currentStatus 現在状態
 * @author Takuya Yamamoto
 */
public record ShipmentCancelResponse(String orderId, String cancelStatus, String currentStatus) {}
