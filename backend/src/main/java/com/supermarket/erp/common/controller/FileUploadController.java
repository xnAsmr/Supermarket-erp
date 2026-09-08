package com.supermarket.erp.common.controller;

import com.supermarket.erp.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        System.out.println("[FileUpload] 上传目录: " + this.uploadPath);
    }

    @Operation(summary = "上传图片")
    @PostMapping("/image/{category}")
    public Result<String> uploadImage(
            @PathVariable String category,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return Result.fail("文件名不能为空");
        }

        String ext = getExtension(originalName).toLowerCase();
        if (!IMAGE_EXTENSIONS.contains(ext)) {
            return Result.fail("只支持 jpg/png/gif/webp 格式");
        }

        try {
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path categoryDir = uploadPath.resolve(category);
            Files.createDirectories(categoryDir);

            Path filePath = categoryDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            String url = "/api/v1/upload/file/" + category + "/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "读取文件")
    @GetMapping("/file/{category}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String category,
            @PathVariable String filename) {
        try {
            Path filePath = uploadPath.resolve(category).resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
