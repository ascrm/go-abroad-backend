package com.goAbroad.core.resource.mapper;

import com.goAbroad.core.resource.dto.ResourceCategoryResponse;
import com.goAbroad.core.resource.dto.ResourceResponse;
import com.goAbroad.core.resource.entity.Resource;
import com.goAbroad.core.resource.entity.ResourceCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    ResourceMapper INSTANCE = Mappers.getMapper(ResourceMapper.class);

    @Mapping(target = "categoryName", expression = "java(resource.getCategory() != null ? resource.getCategory().getName() : null)")
    ResourceResponse toResourceResponse(Resource resource);

    List<ResourceResponse> toResourceResponseList(List<Resource> resources);

    ResourceCategoryResponse toResourceCategoryResponse(ResourceCategory category);

    List<ResourceCategoryResponse> toResourceCategoryResponseList(List<ResourceCategory> categories);
}
