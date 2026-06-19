package jp.co.hoge.orderhub.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 監視向け異常ログを統一形式で出力する Aspect。
 *
 * @author Takuya Yamamoto
 */
@Slf4j
@Aspect
@Component
public class MonitoringLoggingAspect {

    /**
     * Service / Controller 層で送出された例外を監視ログへ出力する。
     *
     * @param joinPoint 例外発生箇所
     * @param throwable 発生例外
     */
    @AfterThrowing(
            pointcut = "within(@org.springframework.stereotype.Service *) || within(@org.springframework.web.bind.annotation.RestController *)",
            throwing = "throwable"
    )
    public void logMonitoringError(JoinPoint joinPoint, Throwable throwable) {
        log.error(
                "MONITORING_ERROR trackingId={} requestId={} traceId={} point={} message={}",
                org.slf4j.MDC.get(MdcKeys.TRACKING_ID),
                org.slf4j.MDC.get(MdcKeys.REQUEST_ID),
                org.slf4j.MDC.get(MdcKeys.TRACE_ID),
                joinPoint.getSignature().toShortString(),
                throwable.getMessage(),
                throwable
        );
    }
}
