package com.app.pdf.config;

import com.app.pdf.model.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return Result.error("文件大小超出限制");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result handleGeneralException(Exception ex) {
        return Result.error(500, "服务器内部错误: " + ex.getMessage());
    }
}