package com.dpsk.dpsk_quiz_sys_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = "com.dpsk.dpsk_quiz_sys_java")
@EnableTransactionManagement
public class DpskQuizSysJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DpskQuizSysJavaApplication.class, args);
    }

}
