package jp.co.hoge.customerregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Customer Registry API アプリケーションの起動クラス。
 *
 * @author Takuya Yamamoto
 */
@SpringBootApplication
public class CustomerRegistryApplication {
  /**
   * アプリケーションを起動する。
   *
   * @param args 起動引数
   */
  public static void main(String[] args) {
    SpringApplication.run(CustomerRegistryApplication.class, args);
  }
}
