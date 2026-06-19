package jp.co.hoge.shippinggateway.mapper.model;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.dto.BarDeliveryResultRequest;

/**
 * Bar配送結果受付時のエンティティ変換用コンテキスト。
 *
 * @param request 配送結果要求
 * @param latestDisplayStatusName 画面表示用ステータス名
 * @param rawPayloadHash 受信ペイロードハッシュ
 * @param occurredAt イベント発生日時
 * @param now 受信日時
 * @author Takuya Yamamoto
 */
public record BarDeliveryResultContext(
        BarDeliveryResultRequest request,
        String latestDisplayStatusName,
        String rawPayloadHash,
        LocalDateTime occurredAt,
        LocalDateTime now
) {
}
