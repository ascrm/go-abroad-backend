package com.goAbroad.core.resource.service;

import com.goAbroad.core.resource.dto.ResourceCategoryResponse;
import com.goAbroad.core.resource.dto.ResourceResponse;
import com.goAbroad.core.resource.entity.Resource;
import com.goAbroad.core.resource.entity.ResourceCategory;
import com.goAbroad.core.resource.mapper.ResourceMapper;
import com.goAbroad.core.resource.repository.ResourceCategoryRepository;
import com.goAbroad.core.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private static final String GLOBAL_COUNTRY = "全球";

    private final ResourceRepository resourceRepository;
    private final ResourceCategoryRepository resourceCategoryRepository;
    private final ResourceMapper resourceMapper;

    @Override
    public List<ResourceResponse> getResourceList(String country) {
        List<Resource> resources;
        if (GLOBAL_COUNTRY.equals(country)) {
            resources = resourceRepository.findFeaturedResources();
        } else {
            resources = resourceRepository.findByCondition(country, null);
        }
        return resourceMapper.toResourceResponseList(resources);
    }

    @Override
    public List<ResourceCategoryResponse> getCategoryList() {
        List<ResourceCategory> categories = resourceCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return resourceMapper.toResourceCategoryResponseList(categories);
    }
}
