package jp.co.hoge.orderhub.common.mapper;

import jp.co.hoge.orderhub.common.mapper.model.NotificationHistoryRecord;
import jp.co.hoge.orderhub.common.persistence.entity.NotificationHistoryEntity;
import org.mapstruct.Mapper;

/**
 * 通知履歴レコードを永続化エンティティへ変換する Mapper。
 *
 * @author Takuya Yamamoto
 */
@Mapper(config = CommonMapperConfig.class)
public interface NotificationHistoryEntityMapper {

  /**
   * 通知履歴記録を通知履歴エンティティへ変換する。
   *
   * @param source 変換元レコード
   * @return 通知履歴エンティティ
   */
  NotificationHistoryEntity toEntity(NotificationHistoryRecord source);
}
