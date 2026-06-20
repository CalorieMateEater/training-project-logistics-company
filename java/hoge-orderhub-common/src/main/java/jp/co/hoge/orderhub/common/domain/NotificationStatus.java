package jp.co.hoge.orderhub.common.domain;

/**
 * 通知送信処理の状態。
 *
 * @author Takuya Yamamoto
 */
public enum NotificationStatus {
  PENDING,
  SENT,
  SKIPPED,
  ERROR
}
