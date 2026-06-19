package jp.co.hoge.orderhubbatch.controller;

import jp.co.hoge.orderhubbatch.service.ArchiveService;
import jp.co.hoge.orderhubbatch.service.FooOrderImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * バッチ起動 API を提供するコントローラー。
 * 関連処理設計書ID: PDS-001, PDS-009
 *
 * @author Takuya Yamamoto
 */
@RestController
@RequestMapping("/internal/jobs")
@RequiredArgsConstructor
public class BatchJobController {
    /** Foo注文取込サービス。 */
    private final FooOrderImportService fooOrderImportService;
    /** 日次アーカイブサービス。 */
    private final ArchiveService archiveService;

    /**
     * Foo注文取込バッチを起動する。
     *
     * @param path 取込対象ファイルパス
     * @return 取込件数
     */
    @PostMapping("/foo-orders/import")
    public String importFooOrders(@RequestParam String path) {
        int imported = fooOrderImportService.importFile(path);
        return "imported=" + imported;
    }

    /**
     * 日次アーカイブバッチを起動する。
     *
     * @return アーカイブ実行 ID
     */
    @PostMapping("/archive/run")
    public String runArchive() {
        return archiveService.archiveCompletedOrders();
    }
}
