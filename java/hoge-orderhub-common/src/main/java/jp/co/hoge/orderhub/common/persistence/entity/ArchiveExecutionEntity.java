package jp.co.hoge.orderhub.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 日次アーカイブ実行履歴を保持するエンティティ。
 *
 * @author Takuya Yamamoto
 */
@Entity
@Table(name = "tm_archive_execution", schema = "orderhub")
public class ArchiveExecutionEntity {

    /** アーカイブ実行 ID。 */
    @Id
    @Column(name = "archive_execution_id", nullable = false, length = 40)
    private String archiveExecutionId;

    /** 開始日時。 */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /** 終了日時。 */
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    /** アーカイブ対象注文件数。 */
    @Column(name = "archived_orders", nullable = false)
    private int archivedOrders;

    /** 出力先パス。 */
    @Column(name = "output_path", length = 256)
    private String outputPath;

    /** 実行結果状態。 */
    @Column(name = "result_status", nullable = false, length = 16)
    private String resultStatus;

    /**
     * アーカイブ実行 ID を返却する。
     *
     * @return アーカイブ実行 ID
     */
    public String getArchiveExecutionId() {
        return archiveExecutionId;
    }

    /**
     * アーカイブ実行 ID を設定する。
     *
     * @param archiveExecutionId アーカイブ実行 ID
     */
    public void setArchiveExecutionId(String archiveExecutionId) {
        this.archiveExecutionId = archiveExecutionId;
    }

    /**
     * 開始日時を返却する。
     *
     * @return 開始日時
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 開始日時を設定する。
     *
     * @param startedAt 開始日時
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * 終了日時を返却する。
     *
     * @return 終了日時
     */
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * 終了日時を設定する。
     *
     * @param finishedAt 終了日時
     */
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * アーカイブ対象注文件数を返却する。
     *
     * @return アーカイブ対象注文件数
     */
    public int getArchivedOrders() {
        return archivedOrders;
    }

    /**
     * アーカイブ対象注文件数を設定する。
     *
     * @param archivedOrders アーカイブ対象注文件数
     */
    public void setArchivedOrders(int archivedOrders) {
        this.archivedOrders = archivedOrders;
    }

    /**
     * 出力先パスを返却する。
     *
     * @return 出力先パス
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * 出力先パスを設定する。
     *
     * @param outputPath 出力先パス
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * 実行結果状態を返却する。
     *
     * @return 実行結果状態
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * 実行結果状態を設定する。
     *
     * @param resultStatus 実行結果状態
     */
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}
