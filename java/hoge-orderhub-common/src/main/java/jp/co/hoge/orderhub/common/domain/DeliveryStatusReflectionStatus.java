package jp.co.hoge.orderhub.common.domain;

/**
 * 配送状態反映要求の処理状態。
 *
 * @author Takuya Yamamoto
 */
public enum DeliveryStatusReflectionStatus {
  PENDING,
  PROCESSING,
  PROCESSED,
  FAILED
}
