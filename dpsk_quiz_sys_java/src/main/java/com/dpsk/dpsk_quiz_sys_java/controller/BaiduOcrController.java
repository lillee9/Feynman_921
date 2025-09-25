package com.dpsk.dpsk_quiz_sys_java.controller;
import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.service.BaiduOcrService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/baiduocr")
@CrossOrigin
public class BaiduOcrController {
    @Autowired
    private BaiduOcrService ocrService;

    @PostMapping("/recognize")
    public ResponseMessage<String> recognizeImage(@RequestParam("image") MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        try {
            // 1. 判断PDF
            if (extension.equals("pdf")) {
                byte[] fileBytes = file.getBytes(); // 一次性读取文件内容
                String result = ocrService.recognizePdf(fileBytes); // 调用 PDF 识别
                return new ResponseMessage<>(200, "success", result);
            }
            // 2. 判断普通图片
            else if (isSupportedImage(extension)) {
                String result = ocrService.recognizeImg(file); // 调用图片识别
                return new ResponseMessage<>(200, "success", result);
            }
            else {
                return new ResponseMessage<>(400, "error", "不支持的文件格式: " + extension);
            }
        } catch (Exception e) {
            return new ResponseMessage<>(400, "error", "识别失败: " + e.getMessage());
        }

    }

    // 判断是否是支持的图片格式
    private boolean isSupportedImage(String extension) {
        return Set.of("jpg", "jpeg", "png", "bmp").contains(extension);
    }
}
