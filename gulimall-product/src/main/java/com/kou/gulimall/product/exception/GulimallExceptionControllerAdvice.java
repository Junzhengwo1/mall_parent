package com.kou.gulimall.product.exception;

import com.kou.gulimall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理4所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.kou.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    /**
     * 所有参数校验的异常处理
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{},异常类型{}",e.getMessage(),e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach(fieldError -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        //这里的状态码最好是使用枚举
        return R.error(400,"数据校验出现问题").put("data",errorMap);

    }


    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e){
        log.error("异常",e);
        return R.error();
    }


}
