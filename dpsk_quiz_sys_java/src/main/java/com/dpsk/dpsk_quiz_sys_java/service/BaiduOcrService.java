package com.dpsk.dpsk_quiz_sys_java.service;
import com.dpsk.dpsk_quiz_sys_java.utils.BaiduUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
@Service
public class BaiduOcrService {

    public String recognizeImg(MultipartFile image) throws IOException {
        // 将图片文件转换为Base64字符串
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        //将字符串中的/，=，+
        //分别转换为为%2F，%3D，%2B
        //符合百度的要求
        base64Image = BaiduUtils.replaceSpecialCharacters(base64Image);

        return BaiduUtils.getImgResponse(base64Image);
    }

    public String recognizePdf(byte[] pdfBytes) throws IOException {
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        //将字符串中的/，=，+
        //分别转换为为%2F，%3D，%2B
        //符合百度的要求
        base64Pdf = BaiduUtils.replaceSpecialCharacters(base64Pdf);
        return BaiduUtils.getPdfResponse(base64Pdf);
    }

}
