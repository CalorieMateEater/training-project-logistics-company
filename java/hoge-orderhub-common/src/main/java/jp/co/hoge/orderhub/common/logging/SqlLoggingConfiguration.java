package jp.co.hoge.orderhub.common.logging;

import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SQL 実行前後のログを採取する DataSource Proxy 設定。
 *
 * @author Takuya Yamamoto
 */
@Slf4j
@Configuration
public class SqlLoggingConfiguration {

  /**
   * SQL 実行前後でクエリ内容と経過時間を記録するリスナーを生成する。
   *
   * @return SQL 実行リスナー
   */
  @Bean
  QueryExecutionListener sqlQueryExecutionListener() {
    return new QueryExecutionListener() {
      @Override
      public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {}

      @Override
      public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        log.info(
            "SQL_AFTER trackingId={} connection={} success={} elapsedMs={} queries={}",
            org.slf4j.MDC.get(MdcKeys.TRACKING_ID),
            execInfo.getConnectionId(),
            execInfo.isSuccess(),
            execInfo.getElapsedTime(),
            formatQueries(queryInfoList));
      }
    };
  }

  /**
   * 生成済み DataSource を ProxyDataSource へ差し替える後処理を登録する。
   *
   * @param listener SQL 実行リスナー
   * @return DataSource 差し替え用 BeanPostProcessor
   */
  @Bean
  BeanPostProcessor dataSourceProxyBeanPostProcessor(QueryExecutionListener listener) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName)
          throws BeansException {
        if (bean instanceof DataSource dataSource
            && !bean.getClass().getName().contains("ProxyDataSource")) {
          return ProxyDataSourceBuilder.create(dataSource)
              .name(beanName)
              .listener(listener)
              .build();
        }
        return bean;
      }
    };
  }

  /**
   * クエリ一覧をログ出力用文字列へ整形する。
   *
   * @param queryInfoList クエリ一覧
   * @return 整形済み文字列
   */
  private static String formatQueries(List<QueryInfo> queryInfoList) {
    return queryInfoList.stream()
        .map(queryInfo -> queryInfo.getQuery() + " params=" + formatParameters(queryInfo))
        .collect(Collectors.joining(" | "));
  }

  /**
   * クエリ引数をログ出力用文字列へ整形する。
   *
   * @param queryInfo クエリ情報
   * @return 整形済み文字列
   */
  private static String formatParameters(QueryInfo queryInfo) {
    return queryInfo.getParametersList().stream()
        .map(SqlLoggingConfiguration::formatParameterSet)
        .collect(Collectors.joining(", ", "[", "]"));
  }

  /**
   * パラメータセットをログ出力用文字列へ整形する。
   *
   * @param operations パラメータ操作一覧
   * @return 整形済み文字列
   */
  private static String formatParameterSet(List<ParameterSetOperation> operations) {
    return operations.stream()
        .map(
            operation ->
                operation.getMethod().getName() + "=" + String.valueOf(operation.getArgs()[1]))
        .collect(Collectors.joining(", ", "{", "}"));
  }
}
