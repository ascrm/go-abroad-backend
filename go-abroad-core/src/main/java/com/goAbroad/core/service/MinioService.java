package com.goAbroad.core.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.bucket:GoAbroad}")
    private String bucket;

    @Value("${minio.presigned-expiry-seconds:604800}")
    private long presignedExpirySeconds;

    /**
     * 上传图片文件
     */
    public String uploadImage(MultipartFile file) {
        return upload(file, "images");
    }

    /**
     * 上传文件
     */
    public String upload(MultipartFile file, String folder) {
        try {
            ensureBucketExists();

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = folder + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回预签名URL，有效期7天
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(fileName)
                            .expiry((int) presignedExpirySeconds, TimeUnit.SECONDS)
                            .build()
            );
            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 列出指定前缀的所有文件对象
     */
    public List<String> listObjects(String prefix) {
        try {
            List<String> urls = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();
                // 获取预签名URL
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucket)
                                .object(objectName)
                                .expiry((int) presignedExpirySeconds, TimeUnit.SECONDS)
                                .build()
                );
                urls.add(url);
            }
            log.info("列出对象成功: prefix={}, count={}", prefix, urls.size());
            return urls;
        } catch (Exception e) {
            log.error("列出对象失败", e);
            throw new RuntimeException("列出对象失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件流
     */
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件失败", e);
            throw new RuntimeException("获取文件失败: " + e.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
                log.info("Bucket 创建成功: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Bucket 检查/创建失败", e);
            throw new RuntimeException("Bucket 初始化失败: " + e.getMessage());
        }
    }
}