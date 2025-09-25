package com.dpsk.dpsk_quiz_sys_java.exception;

import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    public ResponseMessage handlerException(Exception e, HttpServletRequest request, HttpServletResponse response){
        logger.error("统一异常",e);

        return new ResponseMessage(500,"error",e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseMessage handleCustomException(CustomException e, HttpServletRequest request, HttpServletResponse response) {
        logger.warn("自定义异常：{}", e.getMessage());
        return new ResponseMessage(400, e.getMessage(), e.getMessage());
    }
}
