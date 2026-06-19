package jp.co.hoge.orderhubworker.service;

import jp.co.hoge.orderhub.common.domain.InterfaceDirection;
import jp.co.hoge.orderhub.common.domain.InterfaceStatus;
import jp.co.hoge.orderhub.common.logging.MdcKeys;
import jp.co.hoge.orderhub.common.mapper.InterfaceHistoryEntityMapper;
import jp.co.hoge.orderhub.common.mapper.model.InterfaceHistoryRecord;
import jp.co.hoge.orderhub.common.persistence.repository.InterfaceHistoryRepository;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * 対外インターフェース履歴を記録するサービス。
 * 関連処理設計書ID: PDS-002, PDS-003, PDS-006
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceHistoryService {
    /** IF 履歴リポジトリ。 */
    private final InterfaceHistoryRepository interfaceHistoryRepository;
    /** IF 履歴 ID 採番サービス。 */
    private final IdFactory idFactory;
    /** IF 履歴エンティティマッパー。 */
    private final InterfaceHistoryEntityMapper interfaceHistoryEntityMapper;
    /** 現在時刻提供サービス。 */
    private final TimeProvider timeProvider;

    /**
     * インターフェース履歴を登録する。
     *
     * @param ifId インターフェース ID
     * @param direction 送受信方向
     * @param status 実行結果ステータス
     * @param requestKey 要求識別キー
     * @param resultCode 結果コード
     * @param message 結果メッセージ
     */
    public void record(
            String ifId,
            InterfaceDirection direction,
            InterfaceStatus status,
            String requestKey,
            String resultCode,
            String message
    ) {
        interfaceHistoryRepository.save(interfaceHistoryEntityMapper.toEntity(
                new InterfaceHistoryRecord(
                        idFactory.interfaceHistoryId(),
                        ifId,
                        direction,
                        status,
                        requestKey,
                        MDC.get(MdcKeys.TRACE_ID),
                        resultCode,
                        message,
                        timeProvider.now()
                )
        ));
        log.info("APP_IF_HISTORY_RECORDED ifId={} requestKey={} resultCode={} status={}",
                ifId, requestKey, resultCode, status);
    }
}
