package jp.co.hoge.orderhubworker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Worker スケジューリング有効化設定。
 *
 * @author Takuya Yamamoto
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(WorkerFileProperties.class)
@ConditionalOnProperty(
    name = "hoge.worker.scheduling.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SchedulingConfig {}
