package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.exception.BusinessException;
import com.acg.community.util.ImageFileValidator;
import com.acg.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final long UPLOAD_WINDOW_SECONDS = 60;
    private static final long MAX_UPLOADS_PER_WINDOW = 20;

    private final RedisUtil redisUtil;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8081/api/uploads}")
    private String baseUrl;

    public FileUploadController(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        enforceRateLimit(userId);

        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 10MB");
        }

        String originalName = file.getOriginalFilename();
        try (InputStream input = file.getInputStream()) {
            if (!ImageFileValidator.hasMatchingImageSignature(originalName, input)) {
                throw new BusinessException(400, "只支持 jpg/jpeg/png/gif/webp/bmp 格式的真实图片文件");
            }
        } catch (IOException e) {
            throw new BusinessException(400, "无法读取上传文件");
        }

        String extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(fileName).normalize();
            if (!targetPath.startsWith(uploadPath)) {
                throw new BusinessException(400, "非法文件路径");
            }
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败");
        }

        String url = baseUrl.endsWith("/") ? baseUrl + fileName : baseUrl + "/" + fileName;
        return Result.success("上传成功", url);
    }

    private void enforceRateLimit(Long userId) {
        long count = redisUtil.increment("acg:rate:upload:" + userId, UPLOAD_WINDOW_SECONDS);
        if (count > MAX_UPLOADS_PER_WINDOW) {
            throw new BusinessException(429, "上传过于频繁，请稍后再试");
        }
    }
}
