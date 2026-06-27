package jp.co.hoge.stockkeeper.config;

import java.time.LocalDateTime;
import jp.co.hoge.stockkeeper.entity.StockBalanceEntity;
import jp.co.hoge.stockkeeper.entity.StockItemEntity;
import jp.co.hoge.stockkeeper.entity.WarehouseLocationEntity;
import jp.co.hoge.stockkeeper.entity.WarehouseStaffScopeEntity;
import jp.co.hoge.stockkeeper.repository.StockBalanceRepository;
import jp.co.hoge.stockkeeper.repository.StockItemRepository;
import jp.co.hoge.stockkeeper.repository.WarehouseLocationRepository;
import jp.co.hoge.stockkeeper.repository.WarehouseStaffScopeRepository;
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
   * @param itemRepository 商品マスタ参照先
   * @param balanceRepository 在庫残高参照先
   * @param warehouseRepository 倉庫場所マスタ参照先
   * @param scopeRepository 倉庫担当者スコープ参照先
   * @return 初期データ投入 Runner
   */
  @Bean
  ApplicationRunner seedStocks(
      StockItemRepository itemRepository,
      StockBalanceRepository balanceRepository,
      WarehouseLocationRepository warehouseRepository,
      WarehouseStaffScopeRepository scopeRepository) {
    return args -> {
      if (itemRepository.count() > 0 || balanceRepository.count() > 0) {
        return;
      }

      warehouseRepository.save(warehouse("WH-TYO-01", "東京標準倉庫", "KANTO", "AMBIENT"));
      warehouseRepository.save(warehouse("WH-OSA-01", "大阪標準倉庫", "KANSAI", "AMBIENT"));
      warehouseRepository.save(warehouse("WH-CHB-COOL", "千葉冷蔵倉庫", "KANTO", "COOL"));

      itemRepository.save(item("ITM0000001", "StandardItemA", "AMBIENT", "NORMAL", 1000));
      itemRepository.save(item("ITM0000002", "StandardItemB", "AMBIENT", "NORMAL", 1500));
      itemRepository.save(item("ITM0000003", "CoolItemC", "COOL", "COOL", 800));

      LocalDateTime now = LocalDateTime.now();
      balanceRepository.save(balance("ITM0000001", "WH-TYO-01", 60, 0, now));
      balanceRepository.save(balance("ITM0000001", "WH-OSA-01", 40, 0, now));
      balanceRepository.save(balance("ITM0000002", "WH-TYO-01", 50, 0, now));
      balanceRepository.save(balance("ITM0000003", "WH-CHB-COOL", 30, 0, now));

      scopeRepository.save(scope("EMP-WH-TYO-001", "東京倉庫担当A", "WH-TYO-01"));
      scopeRepository.save(scope("EMP-WH-OSA-001", "大阪倉庫担当A", "WH-OSA-01"));
      scopeRepository.save(scope("EMP-WH-COOL-001", "冷蔵倉庫担当A", "WH-CHB-COOL"));
      scopeRepository.save(scope("EMP-WH-ALL-001", "全倉庫担当", "WH-TYO-01"));
      scopeRepository.save(scope("EMP-WH-ALL-001", "全倉庫担当", "WH-OSA-01"));
      scopeRepository.save(scope("EMP-WH-ALL-001", "全倉庫担当", "WH-CHB-COOL"));
    };
  }

  private WarehouseLocationEntity warehouse(
      String warehouseLocationCode,
      String warehouseLocationName,
      String regionCode,
      String temperatureZone) {
    WarehouseLocationEntity entity = new WarehouseLocationEntity();
    entity.setWarehouseLocationCode(warehouseLocationCode);
    entity.setWarehouseLocationName(warehouseLocationName);
    entity.setRegionCode(regionCode);
    entity.setTemperatureZone(temperatureZone);
    entity.setActiveFlag(true);
    return entity;
  }

  private StockItemEntity item(
      String itemCode,
      String itemName,
      String temperatureZone,
      String sizeType,
      int unitWeightGram) {
    StockItemEntity entity = new StockItemEntity();
    entity.setItemCode(itemCode);
    entity.setItemName(itemName);
    entity.setTemperatureZone(temperatureZone);
    entity.setSizeType(sizeType);
    entity.setUnitWeightGram(unitWeightGram);
    entity.setActiveFlag(true);
    return entity;
  }

  private StockBalanceEntity balance(
      String itemCode,
      String warehouseLocationCode,
      int onHandQuantity,
      int reservedQuantity,
      LocalDateTime now) {
    StockBalanceEntity entity = new StockBalanceEntity();
    entity.setItemCode(itemCode);
    entity.setWarehouseLocationCode(warehouseLocationCode);
    entity.setOnHandQuantity(onHandQuantity);
    entity.setReservedQuantity(reservedQuantity);
    entity.setLastReceivedAt(now);
    entity.setUpdatedAt(now);
    return entity;
  }

  private WarehouseStaffScopeEntity scope(
      String employeeId, String employeeName, String warehouseLocationCode) {
    WarehouseStaffScopeEntity entity = new WarehouseStaffScopeEntity();
    entity.setEmployeeId(employeeId);
    entity.setEmployeeName(employeeName);
    entity.setWarehouseLocationCode(warehouseLocationCode);
    entity.setRoleCode("WAREHOUSE_OPERATOR");
    entity.setActiveFlag(true);
    return entity;
  }
}
