package jp.co.hoge.orderhubworker.mapper.model;

import java.time.LocalDateTime;

/**
 * Bar社冪等履歴登録用のコンテキスト。
 *
 * @param idempotencyKey 冪等キー
 * @param shipmentRequestId 出荷依頼 ID
 * @param requestHash 要求ハッシュ
 * @param barShipmentId Bar社出荷依頼 ID
 * @param createdAt 登録日時
 * @author Takuya Yamamoto
 */
public record BarIdempotencyContext(
        String idempotencyKey,
        String shipmentRequestId,
        String requestHash,
        String barShipmentId,
        LocalDateTime createdAt
) {
}
