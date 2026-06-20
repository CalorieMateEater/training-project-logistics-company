package jp.co.hoge.orderhub.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * 共通 Mapper 向けの MapStruct 設定。
 *
 * @author Takuya Yamamoto
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommonMapperConfig {}
