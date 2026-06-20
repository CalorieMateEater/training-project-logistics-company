package jp.co.hoge.orderhubworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * OrderHub Worker アプリケーションの起動クラス。
 *
 * @author Takuya Yamamoto
 */
@SpringBootApplication(
    scanBasePackages = {"jp.co.hoge.orderhubworker", "jp.co.hoge.orderhub.common"})
@EntityScan(basePackages = "jp.co.hoge.orderhub.common.persistence.entity")
@EnableJpaRepositories(basePackages = "jp.co.hoge.orderhub.common.persistence.repository")
public class OrderHubWorkerApplication {
  /**
   * アプリケーションを起動する。
   *
   * @param args 起動引数
   */
  public static void main(String[] args) {
    SpringApplication.run(OrderHubWorkerApplication.class, args);
  }
}
