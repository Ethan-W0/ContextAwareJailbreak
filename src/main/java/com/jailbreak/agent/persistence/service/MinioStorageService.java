package com.jailbreak.agent.persistence.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(@Value("${minio.endpoint}") String endpoint,
                               @Value("${minio.access-key}") String accessKey,
                               @Value("${minio.secret-key}") String secretKey,
                               @Value("${minio.bucket}") String bucketName) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
        ensureBucket();
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucketName, e);
        }
    }

    public String uploadPdf(String taskId, byte[] pdfBytes) {
        String objectName = "reports/" + taskId + "/" + UUID.randomUUID() + ".pdf";
        try (InputStream is = new ByteArrayInputStream(pdfBytes)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(is, pdfBytes.length, -1)
                    .contentType("application/pdf")
                    .build());
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(3600)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload PDF to MinIO", e);
        }
    }

    public String uploadJson(String taskId, byte[] jsonBytes) {
        String objectName = "reports/" + taskId + "/report.json";
        try (InputStream is = new ByteArrayInputStream(jsonBytes)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(is, jsonBytes.length, -1)
                    .contentType("application/json")
                    .build());
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(3600)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload JSON to MinIO", e);
        }
    }
}
