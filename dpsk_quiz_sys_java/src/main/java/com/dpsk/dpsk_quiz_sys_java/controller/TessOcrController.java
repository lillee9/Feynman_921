package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.service.TessOcrService;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequestMapping("/tessocr")
@RestController
public class TessOcrController {

    //依赖注入
    private final TessOcrService tessOcrService;
    public TessOcrController(TessOcrService tessOcrService) {
        this.tessOcrService = tessOcrService;
    }

    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseMessage recognizeImage(@RequestParam("file") MultipartFile mfile) {
        try {
            // 基础验证
            if (mfile.isEmpty()) {
                return ResponseMessage.error("文件不能为空");
            }

            String fileName = StringUtils.cleanPath(Objects.requireNonNull(mfile.getOriginalFilename()));
            String extension = FilenameUtils.getExtension(fileName).toLowerCase();

            // 1. 先判断是否是压缩包（优先级最高）
            if (isArchiveFile(extension)) {
                List<String> results = tessOcrService.processImageArchive(mfile);
                return ResponseMessage.success(results);
            }
            // 2. 再判断PDF
            else if (extension.equals("pdf")) {
                Path tempDir = Files.createTempDirectory("pdf_ocr_");
                Path tempFile = tempDir.resolve("temp.pdf");
                try {
                    mfile.transferTo(tempFile.toFile());
                    String result = tessOcrService.processPdfWithHighAccuracy(tempFile.toFile());
                    return new ResponseMessage<>(200, "success", result);
                } finally {
                    Files.deleteIfExists(tempFile);
                    Files.deleteIfExists(tempDir);
                }
            }
            // 3. 最后判断普通图片
            else if (isSupportedImage(extension)) {
                String result = tessOcrService.ocrImageWithEnhancement(mfile);
                return new ResponseMessage<>(200, "success", result);
            }
            else {
                return new ResponseMessage<>(400, "error", "不支持的文件格式: " + extension);
            }
        } catch (Exception e) {
            return new ResponseMessage<>(400, "error", "识别失败: " + e.getMessage());
        }
    }

    // 判断是否是压缩包
    private boolean isArchiveFile(String extension) {
        return Set.of("zip", "rar").contains(extension);
    }

    // 判断是否是支持的图片格式
    private boolean isSupportedImage(String extension) {
        return Set.of("jpg", "jpeg", "png", "bmp", "tiff").contains(extension);
    }
}
