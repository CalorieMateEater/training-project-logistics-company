package jp.co.hoge.orderhubbatch.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import jp.co.hoge.orderhub.common.domain.OrderStatus;
import jp.co.hoge.orderhub.common.persistence.entity.ArchiveExecutionEntity;
import jp.co.hoge.orderhub.common.persistence.entity.OrderHeaderEntity;
import jp.co.hoge.orderhub.common.persistence.repository.ArchiveExecutionRepository;
import jp.co.hoge.orderhub.common.persistence.repository.OrderHeaderRepository;
import jp.co.hoge.orderhub.common.support.IdFactory;
import jp.co.hoge.orderhub.common.support.TimeProvider;
import jp.co.hoge.orderhubbatch.config.BatchFileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 日次アーカイブ処理を実行するサービス。
 * 関連処理設計書ID: PDS-009
 *
 * @author Takuya Yamamoto
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArchiveService {
    /** 注文ヘッダリポジトリ。 */
    private final OrderHeaderRepository orderHeaderRepository;
    /** アーカイブ実行履歴リポジトリ。 */
    private final ArchiveExecutionRepository archiveExecutionRepository;
    /** アーカイブ実行 ID 採番サービス。 */
    private final IdFactory idFactory;
    /** 現在時刻提供サービス。 */
    private final TimeProvider timeProvider;
    /** バッチファイル設定。 */
    private final BatchFileProperties batchFileProperties;

    /**
     * 完了済み注文をアーカイブ出力する。
     *
     * @return アーカイブ実行 ID
     */
    public String archiveCompletedOrders() {
        log.info("APP_BATCH_START function=dailyArchive");
        var startedAt = timeProvider.now();
        List<OrderHeaderEntity> targets = orderHeaderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED
                        || order.getOrderStatus() == OrderStatus.CANCELLED
                        || order.getOrderStatus() == OrderStatus.EXCEPTION)
                .collect(Collectors.toList());

        String archiveExecutionId = idFactory.archiveExecutionId();
        Path archiveDir = Path.of(batchFileProperties.getArchiveDir());
        Path output = archiveDir.resolve(
                "orderhub-archive-" + startedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");

        try {
            Files.createDirectories(archiveDir);
            String content = targets.stream()
                    .map(order -> String.join(",",
                            order.getOrderId(),
                            order.getPartnerOrderId(),
                            order.getOrderSource().name(),
                            order.getOrderStatus().name(),
                            order.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .collect(Collectors.joining(System.lineSeparator()));
            Files.writeString(output, content, StandardCharsets.UTF_8);

            ArchiveExecutionEntity execution = new ArchiveExecutionEntity();
            execution.setArchiveExecutionId(archiveExecutionId);
            execution.setStartedAt(startedAt);
            execution.setFinishedAt(timeProvider.now());
            execution.setArchivedOrders(targets.size());
            execution.setOutputPath(output.toString());
            execution.setResultStatus("SUCCESS");
            archiveExecutionRepository.save(execution);
            log.info("APP_BATCH_FINISH function=dailyArchive archiveExecutionId={} archivedOrders={}",
                    archiveExecutionId, targets.size());
            return archiveExecutionId;
        } catch (IOException exception) {
            ArchiveExecutionEntity execution = new ArchiveExecutionEntity();
            execution.setArchiveExecutionId(archiveExecutionId);
            execution.setStartedAt(startedAt);
            execution.setFinishedAt(timeProvider.now());
            execution.setArchivedOrders(targets.size());
            execution.setOutputPath(output.toString());
            execution.setResultStatus("FAILED");
            archiveExecutionRepository.save(execution);
            log.error("MONITORING_BATCH_ERROR function=dailyArchive archiveExecutionId={} message={}",
                    archiveExecutionId, exception.getMessage(), exception);
            throw new IllegalStateException(exception);
        }
    }
}
