package com.xmcc.mll_loginlogout.core.controller;

import com.xmcc.mll_loginlogout.core.image.ImageCode;
import com.xmcc.mll_loginlogout.core.image.ImageCodeGenerator;
import com.xmcc.mll_loginlogout.core.vaidate.ValidateCode;
import com.xmcc.mll_loginlogout.core.vaidate.sms.SmsCodeGenerator;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;

@RestController
public class ValidateCodeController {

    public static final String SESSION_KEY = "SESSION_KEY_IMAGE_CODE";
    public static final String SESSION_SMS_KEY = "SESSION_KEY_SMS_CODE";

    @GetMapping("/code/image")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //生成ImageCode
        ImageCode imageCode = ImageCodeGenerator.generate(new ServletWebRequest(request));
        //存入session
        request.getSession().setAttribute(SESSION_KEY,imageCode );
        //写回前端
        ImageIO.write(imageCode.getImage(),"JPEG" ,response.getOutputStream() );

    }
    @GetMapping("/code/sms")
    public void createSmsCode(HttpServletRequest request,HttpServletResponse response) throws ServletRequestBindingException{
        ValidateCode smsCode = SmsCodeGenerator.generate(new ServletWebRequest(request));
        request.getSession().setAttribute(SESSION_SMS_KEY,smsCode );
        String mobile = ServletRequestUtils.getRequiredStringParameter(request,"mobile" );
        send(mobile,smsCode.getCode());
    }
    //对接短信服务商发送
    public void send(String mobile,String code){
        System.out.println("发送短信验证码：mobile:"+mobile+"code:"+code);
    }
}
