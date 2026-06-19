package jp.co.hoge.orderhub.common.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * アプリケーション起動完了時に起動ログを出力するリスナー。
 *
 * @author Takuya Yamamoto
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupLoggingListener {

    /** 実行環境情報。 */
    private final Environment environment;

    /**
     * 起動済みアプリケーション名と有効プロファイルをログへ出力する。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info(
                "APP_STARTUP application={} profiles={}",
                environment.getProperty("spring.application.name"),
                String.join(",", environment.getActiveProfiles())
        );
    }
}
