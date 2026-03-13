package com.goAbroad.common.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 通用Mapper接口，提供实体与DTO之间的转换方法
 * @param <E> 实体类型
 * @param <D> DTO类型
 */
public interface BaseMapper<E, D> {

    /**
     * DTO转实体
     */
    E toEntity(D dto);

    /**
     * 实体转DTO
     */
    D toDto(E entity);

    /**
     * 实体列表转DTO列表
     */
    List<D> toDtoList(List<E> entityList);

    /**
     * DTO列表转实体列表
     */
    List<E> toEntityList(List<D> dtoList);

    /**
     * 更新实体（使用DTO中的非空字段）
     */
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
