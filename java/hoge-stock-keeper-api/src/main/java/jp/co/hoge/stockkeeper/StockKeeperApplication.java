package jp.co.hoge.stockkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Stock Keeper API アプリケーションの起動クラス。
 *
 * @author Takuya Yamamoto
 */
@SpringBootApplication
public class StockKeeperApplication {
    /**
     * アプリケーションを起動する。
     *
     * @param args 起動引数
     */
    public static void main(String[] args) {
        SpringApplication.run(StockKeeperApplication.class, args);
    }
}
