package jp.co.hoge.stockkeeper.config;

import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 在庫初期データ投入設定。
 *
 * @author Takuya Yamamoto
 */
@Configuration
public class StockSeedData {
  /**
   * 在庫初期データ投入 Runner を生成する。
   *
   * @param repository 在庫参照先
   * @return 初期データ投入 Runner
   */
  @Bean
  ApplicationRunner seedStocks(StockItemRepository repository) {
    return args -> {
      if (repository.count() > 0) {
        return;
      }

      repository.save(stock("ITM0000001", "StandardItemA", "WH-TYO-01", 60, 0, "AMBIENT"));
      repository.save(stock("ITM0000001", "StandardItemA", "WH-OSA-01", 40, 0, "AMBIENT"));
      repository.save(stock("ITM0000002", "StandardItemB", "WH-TYO-01", 50, 0, "AMBIENT"));
      repository.save(stock("ITM0000003", "CoolItemC", "WH-CHB-COOL", 30, 0, "COOL"));
    };
  }

  private StockItemEntity stock(
      String itemCode,
      String itemName,
      String warehouseLocationCode,
      int onHandQuantity,
      int reservedQuantity,
      String temperatureZone) {
    StockItemEntity entity = new StockItemEntity();
    entity.setItemCode(itemCode);
    entity.setItemName(itemName);
    entity.setWarehouseLocationCode(warehouseLocationCode);
    entity.setOnHandQuantity(onHandQuantity);
    entity.setReservedQuantity(reservedQuantity);
    entity.setAvailableQuantity(onHandQuantity - reservedQuantity);
    entity.setTemperatureZone(temperatureZone);
    return entity;
  }
}
