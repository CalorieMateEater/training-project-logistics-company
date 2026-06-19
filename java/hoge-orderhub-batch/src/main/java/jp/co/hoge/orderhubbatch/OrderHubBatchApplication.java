package jp.co.hoge.orderhubbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * OrderHub Batch アプリケーションの起動クラス。
 *
 * @author Takuya Yamamoto
 */
@SpringBootApplication(scanBasePackages = {
        "jp.co.hoge.orderhubbatch",
        "jp.co.hoge.orderhub.common"
})
@EntityScan(basePackages = "jp.co.hoge.orderhub.common.persistence.entity")
@EnableJpaRepositories(basePackages = "jp.co.hoge.orderhub.common.persistence.repository")
public class OrderHubBatchApplication {
    /**
     * アプリケーションを起動する。
     *
     * @param args 起動引数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderHubBatchApplication.class, args);
    }
}
