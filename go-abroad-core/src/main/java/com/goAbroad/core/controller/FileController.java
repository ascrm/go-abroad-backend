package com.goAbroad.core.controller;

import com.goAbroad.common.result.R;
import com.goAbroad.core.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

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

    /**
     * 列出 Minio 桶中指定前缀的所有文件 URL
     */
    @GetMapping("/list")
    public R<List<String>> listFiles(@RequestParam(required = false, defaultValue = "resource") String prefix) {
        List<String> urls = minioService.listObjects(prefix);
        return R.ok(urls);
    }
}