package com.starwar.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        // 输出详细错误信息到控制台，方便排查
        String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
        System.err.println("系统错误详情: " + errorMsg);
        if (e.getCause() != null) {
            System.err.println("错误原因: " + e.getCause().getMessage());
        }
        // 返回通用错误信息，详细错误已在日志中记录
        return Result.error("系统错误: " + e.getMessage());
    }
}


