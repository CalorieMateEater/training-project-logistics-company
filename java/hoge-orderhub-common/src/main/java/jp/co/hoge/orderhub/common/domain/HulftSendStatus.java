package jp.co.hoge.orderhub.common.domain;

/**
 * HULFT送信要求の処理状態。
 *
 * @author Takuya Yamamoto
 */
public enum HulftSendStatus {
  PENDING,
  REQUESTING,
  SENT,
  FAILED
}
