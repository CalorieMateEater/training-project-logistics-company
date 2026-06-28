package jp.co.hoge.orderhub.common.integration;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HULFT集配信サーバへの送信要求境界を表すゲートウェイ。
 *
 * @author Takuya Yamamoto
 */
@Component
@Slf4j
public class HulftSendGateway {

  /**
   * HULFT送信を要求する。
   *
   * @param sendRequestId HULFT送信要求ID
   * @param ifId IF ID
   * @param partnerCode 連携先コード
   * @param filePath 送信ファイルパス
   * @return HULFT転送ID
   */
  public String requestSend(String sendRequestId, String ifId, String partnerCode, Path filePath) {
    log.info(
        "SEND_BEFORE protocol=hulft ifId={} partnerCode={} sendRequestId={} filePath={}",
        ifId,
        partnerCode,
        sendRequestId,
        filePath);
    String transferId = "HULFT-" + sendRequestId;
    log.info(
        "SEND_AFTER protocol=hulft ifId={} partnerCode={} sendRequestId={} transferId={}",
        ifId,
        partnerCode,
        sendRequestId,
        transferId);
    return transferId;
  }
}
