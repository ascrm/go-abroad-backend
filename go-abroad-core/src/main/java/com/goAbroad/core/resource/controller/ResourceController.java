package com.goAbroad.core.resource.controller;

import com.goAbroad.core.resource.dto.ResourceCategoryResponse;
import com.goAbroad.core.resource.dto.ResourceResponse;
import com.goAbroad.core.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/list")
    public com.goAbroad.common.result.R<List<ResourceResponse>> getResourceList(
            @RequestParam String country) {
        List<ResourceResponse> result = resourceService.getResourceList(country);
        return com.goAbroad.common.result.R.ok(result);
    }

    @GetMapping("/categories")
    public com.goAbroad.common.result.R<List<ResourceCategoryResponse>> getCategoryList() {
        List<ResourceCategoryResponse> result = resourceService.getCategoryList();
        return com.goAbroad.common.result.R.ok(result);
    }
}
