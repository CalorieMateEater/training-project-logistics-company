package jp.co.hoge.orderhub.common.dto;

/**
 * 出荷状態照会レスポンス。
 *
 * @param partnerOrderId 連携先注文 ID
 * @param partnerRequestId 連携先要求 ID
 * @param orderId 注文 ID
 * @param currentStatus 現在状態
 * @param deliveryCompanyCode 配送会社コード
 * @param latestStatusDatetime 最新状態更新日時
 * @param allocation 配送会社割当情報
 * @param latestEvent 最新イベント情報
 * @author Takuya Yamamoto
 */
public record ShipmentStatusResponse(
    String partnerOrderId,
    String partnerRequestId,
    String orderId,
    String currentStatus,
    String deliveryCompanyCode,
    String latestStatusDatetime,
    Allocation allocation,
    LatestEvent latestEvent) {

  /**
   * 配送会社割当情報。
   *
   * @param allocationStatus 割当状態
   * @param deliveryCompanyCode 配送会社コード
   * @author Takuya Yamamoto
   */
  public record Allocation(String allocationStatus, String deliveryCompanyCode) {}

  /**
   * 最新配送イベント情報。
   *
   * @param statusCode ステータスコード
   * @param statusLabel ステータス名称
   * @param reasonCategory 理由分類
   * @param displayStatusName 表示用状態名
   * @author Takuya Yamamoto
   */
  public record LatestEvent(
      String statusCode, String statusLabel, String reasonCategory, String displayStatusName) {}
}
