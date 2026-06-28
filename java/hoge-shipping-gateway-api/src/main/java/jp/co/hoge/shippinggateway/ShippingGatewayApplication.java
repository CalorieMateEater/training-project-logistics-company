package jp.co.hoge.shippinggateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Shipping Gateway API アプリケーションの起動クラス。
 *
 * @author Takuya Yamamoto
 */
@SpringBootApplication(
    scanBasePackages = {"jp.co.hoge.shippinggateway", "jp.co.hoge.orderhub.common"})
@EntityScan(basePackages = "jp.co.hoge.orderhub.common.persistence.entity")
@EnableJpaRepositories(basePackages = "jp.co.hoge.orderhub.common.persistence.repository")
@EnableScheduling
public class ShippingGatewayApplication {
  /**
   * アプリケーションを起動する。
   *
   * @param args 起動引数
   */
  public static void main(String[] args) {
    SpringApplication.run(ShippingGatewayApplication.class, args);
  }
}
