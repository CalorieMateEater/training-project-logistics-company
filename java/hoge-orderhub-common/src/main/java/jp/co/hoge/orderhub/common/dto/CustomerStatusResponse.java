package jp.co.hoge.orderhub.common.dto;

/**
 * 顧客マスタ照会結果レスポンス。
 *
 * @param customerId 顧客 ID
 * @param status 顧客状態
 * @param memberRank 会員ランク
 * @author Takuya Yamamoto
 */
public record CustomerStatusResponse(
        String customerId,
        String status,
        String memberRank
) {
}
