package jp.co.hoge.orderhub.common.integration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SQSメッセージ送受信の境界を表すゲートウェイ。
 *
 * @author Takuya Yamamoto
 */
@Component
@Slf4j
public class SqsMessageGateway {

  /**
   * SQSへメッセージを送信する。
   *
   * @param queueName キュー名
   * @param messageGroupId FIFOメッセージグループID
   * @param deduplicationId 重複排除ID
   * @param payload 送信ペイロード
   * @return 送信メッセージID
   */
  public String send(
      String queueName, String messageGroupId, String deduplicationId, String payload) {
    log.info(
        "SEND_BEFORE protocol=sqs queueName={} messageGroupId={} deduplicationId={}",
        queueName,
        messageGroupId,
        deduplicationId);
    String messageId = "SQS-" + hash(queueName + "|" + deduplicationId + "|" + payload);
    log.info("SEND_AFTER protocol=sqs queueName={} messageId={}", queueName, messageId);
    return messageId;
  }

  /**
   * SQSメッセージを削除する。
   *
   * @param queueName キュー名
   * @param receiptHandle 受信ハンドル
   */
  public void delete(String queueName, String receiptHandle) {
    log.info(
        "SEND_AFTER protocol=sqs action=delete queueName={} receiptHandle={}",
        queueName,
        receiptHandle);
  }

  private String hash(String source) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte value : hash) {
        builder.append(String.format("%02x", value));
      }
      return builder.substring(0, 24);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
