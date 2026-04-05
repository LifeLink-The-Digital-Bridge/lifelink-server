package com.healthservice.service;

import com.healthservice.dto.DocumentUploadResponse;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class HealthDocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final MinioClient minioClient;
    private final String bucketName;

    public HealthDocumentService(
            @Value("${health.documents.minio.endpoint}") String endpoint,
            @Value("${health.documents.minio.access-key}") String accessKey,
            @Value("${health.documents.minio.secret-key}") String secretKey,
            @Value("${health.documents.minio.bucket}") String bucketName) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
    }

    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        validateFile(file);

        try {
            ensureBucketExists();
            String sanitizedOriginalName = sanitizeFilename(file.getOriginalFilename());
            String extension = extractExtension(sanitizedOriginalName);
            String generatedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(generatedFileName)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());

            String documentUrl = String.format("/api/health/records/documents/%s", generatedFileName);
            return new DocumentUploadResponse(
                    documentUrl,
                    generatedFileName,
                    file.getContentType(),
                    file.getSize(),
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload document", e);
        }
    }

    public StoredDocument loadDocument(String fileName) {
        try {
            String sanitizedFileName = sanitizeFilename(fileName);
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sanitizedFileName)
                    .build());
            GetObjectResponse objectStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sanitizedFileName)
                    .build());
            Resource resource = new InputStreamResource(objectStream);
            String contentType = stat.contentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = resolveContentType(sanitizedFileName);
            }
            return new StoredDocument(resource, contentType);
        } catch (Exception e) {
            throw new IllegalStateException("Document not found");
        }
    }

    public String resolveContentType(String fileName) {
        String type = URLConnection.guessContentTypeFromName(fileName);
        if (type == null || type.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return type;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalStateException("Only PDF, JPG, and PNG files are allowed");
        }
    }

    private String sanitizeFilename(String fileName) {
        String cleaned = StringUtils.cleanPath(fileName == null ? "" : fileName).replace("..", "");
        if (cleaned.isBlank()) {
            throw new IllegalStateException("Invalid file name");
        }
        return cleaned;
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public record StoredDocument(Resource resource, String contentType) {
    }
}
