package com.goAbroad.core.resource.service;

import com.goAbroad.core.resource.dto.ResourceCategoryResponse;
import com.goAbroad.core.resource.dto.ResourceResponse;

import java.util.List;

public interface ResourceService {

    List<ResourceResponse> getResourceList(String country);

    List<ResourceCategoryResponse> getCategoryList();
}
