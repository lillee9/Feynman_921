package com.dpsk.dpsk_quiz_sys_java.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractOcrConfig {

    @Value("${tess4j.data-path}")  // 从 application.properties 读取路径
    private String dataPath;

    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(dataPath);  // 设置 Tesseract 训练数据路径
        tesseract.setLanguage("chi_sim+eng+equ");  // 设置中文简体
        return tesseract;
    }
}
