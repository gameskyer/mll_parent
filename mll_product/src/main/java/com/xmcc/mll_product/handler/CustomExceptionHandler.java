package com.xmcc.mll_product.handler;

import com.xmcc.mllcommon.exception.CustomException;
import com.xmcc.mllcommon.result.ResultEnums;
import com.xmcc.mllcommon.result.ResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//AOP拦截springboot的controller层
@ControllerAdvice
@RestController//返回json数据
@Slf4j
public class CustomExceptionHandler {
    @ExceptionHandler(Exception.class)
    /**
     * 如果不是前后端分离，在Controller路径中可以指定返回json数据用.json结尾,返回页面用.page结尾
     * 可以通过request获得请求路径来判断是要返回json还是page然后做相应的处理
     * */
    public ResultResponse<ResultEnums> exceptionHandler(HttpServletRequest request
    , HttpServletResponse response,Exception exception){
        log.error("request请求为:{},异常为{}",request.getRequestURL(),exception );
        //如果是自定义异常
        if(exception instanceof CustomException){
            CustomException customException = (CustomException) exception;
            return ResultResponse.fail(customException.getMessage());
        }
        return ResultResponse.fail(exception.getCause().getMessage());
    }
}
