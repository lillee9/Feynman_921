package com.dpsk.dpsk_quiz_sys_java.exception;


public class CustomException extends RuntimeException{
    private String message;
    public CustomException(String message){
        this.message = message;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
}