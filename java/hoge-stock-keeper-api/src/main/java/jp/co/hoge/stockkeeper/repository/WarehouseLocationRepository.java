package jp.co.hoge.stockkeeper.repository;

import java.util.Optional;
import jp.co.hoge.stockkeeper.entity.WarehouseLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 倉庫場所マスタを管理するリポジトリ。
 *
 * @author Takuya Yamamoto
 */
public interface WarehouseLocationRepository
    extends JpaRepository<WarehouseLocationEntity, String> {
  /**
   * 有効な倉庫場所を取得する。
   *
   * @param warehouseLocationCode 倉庫場所コード
   * @return 倉庫場所
   */
  Optional<WarehouseLocationEntity> findByWarehouseLocationCodeAndActiveFlagTrue(
      String warehouseLocationCode);
}
