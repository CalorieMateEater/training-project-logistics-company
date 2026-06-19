package jp.co.hoge.orderhub.common.mapper.model;

import java.time.LocalDateTime;
import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;

/**
 * IF 履歴登録時の入力値を保持するレコード。
 *
 * @param interfaceHistoryId IF 履歴 ID
 * @param ifId IF 識別子
 * @param direction 連携方向
 * @param resultStatus 処理結果ステータス
 * @param requestKey 業務要求キー
 * @param traceId トレース ID
 * @param resultCode 結果コード
 * @param message 結果メッセージ
 * @param requestedAt 要求日時
 * @author Takuya Yamamoto
 */
public record InterfaceHistoryRecord(
        String interfaceHistoryId,
        String ifId,
        InterfaceDirection direction,
        InterfaceStatus resultStatus,
        String requestKey,
        String traceId,
        String resultCode,
        String message,
        LocalDateTime requestedAt
) {
}
