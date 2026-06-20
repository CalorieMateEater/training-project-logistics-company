package jp.co.hoge.orderhub.common.logging;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * REST Controller 呼び出しの開始・終了・異常終了を記録する Aspect。
 *
 * @author Takuya Yamamoto
 */
@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

  /**
   * Controller 呼び出しの前後で追跡用ログを出力する。
   *
   * @param joinPoint 呼び出し情報
   * @return Controller 実行結果
   * @throws Throwable Controller 実行時例外
   */
  @Around("within(@org.springframework.web.bind.annotation.RestController *)")
  public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
    MdcUtils.getOrCreateTrackingId();
    log.info(
        "APP_CONTROLLER_BEFORE trackingId={} requestId={} method={} args={}",
        org.slf4j.MDC.get(MdcKeys.TRACKING_ID),
        org.slf4j.MDC.get(MdcKeys.REQUEST_ID),
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs()));
    long startedAt = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      log.info(
          "APP_CONTROLLER_AFTER trackingId={} requestId={} method={} elapsedMs={}",
          org.slf4j.MDC.get(MdcKeys.TRACKING_ID),
          org.slf4j.MDC.get(MdcKeys.REQUEST_ID),
          joinPoint.getSignature().toShortString(),
          System.currentTimeMillis() - startedAt);
      return result;
    } catch (Throwable throwable) {
      log.error(
          "APP_CONTROLLER_ERROR trackingId={} requestId={} method={} elapsedMs={} message={}",
          org.slf4j.MDC.get(MdcKeys.TRACKING_ID),
          org.slf4j.MDC.get(MdcKeys.REQUEST_ID),
          joinPoint.getSignature().toShortString(),
          System.currentTimeMillis() - startedAt,
          throwable.getMessage(),
          throwable);
      throw throwable;
    }
  }
}
