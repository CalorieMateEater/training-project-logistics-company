package jp.co.hoge.orderhub.common.mapper;

import jp.co.hoge.orderhub.common.mapper.model.InterfaceHistoryRecord;
import jp.co.hoge.orderhub.common.persistence.entity.InterfaceHistoryEntity;
import org.mapstruct.Mapper;

/**
 * IF 履歴レコードを永続化エンティティへ変換する Mapper。
 *
 * @author Takuya Yamamoto
 */
@Mapper(config = CommonMapperConfig.class)
public interface InterfaceHistoryEntityMapper {

    /**
     * IF 履歴記録を IF 履歴エンティティへ変換する。
     *
     * @param source 変換元レコード
     * @return IF 履歴エンティティ
     */
    InterfaceHistoryEntity toEntity(InterfaceHistoryRecord source);
}
