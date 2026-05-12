package com.goAbroad.core.resource.controller;

import com.goAbroad.common.result.R;
import com.goAbroad.core.resource.dto.ResourceCategoryResponse;
import com.goAbroad.core.resource.dto.ResourceResponse;
import com.goAbroad.core.resource.service.MinioService;
import com.goAbroad.core.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final MinioService minioService;

    @GetMapping("/list")
    public R<List<ResourceResponse>> getResourceList(
            @RequestParam String country) {
        List<ResourceResponse> result = resourceService.getResourceList(country);
        return R.ok(result);
    }

    @GetMapping("/categories")
    public R<List<ResourceCategoryResponse>> getCategoryList() {
        List<ResourceCategoryResponse> result = resourceService.getCategoryList();
        return R.ok(result);
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadImage(file);
        return R.ok(url);
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file,
                           @RequestParam(required = false, defaultValue = "other") String folder) {
        String url = minioService.upload(file, folder);
        return R.ok(url);
    }
}
