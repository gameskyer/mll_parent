package com.xmcc.mll_miaosha.controller;

import com.xmcc.mll_miaosha.entity.MllMiaoshaProduct;
import com.xmcc.mll_miaosha.service.MiaoshaService;
import com.xmcc.mllcommon.result.ResultResponse;
import com.xmcc.mll_miaosha.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("miaosha")
public class MiaoshaController {
    @Autowired
    private MiaoshaService miaoshaService;
    @Autowired
    private RedisUtils redisUtils;
    @GetMapping("page/{productId}")
    public ModelAndView page(@PathVariable("productId")Long productId, Map map){
        String user = "admin";
        ResultResponse<MllMiaoshaProduct> mllMiaoshaProductResultResponse = miaoshaService.queryById(productId);
        MllMiaoshaProduct mllMiaoshaProduct = mllMiaoshaProductResultResponse.getData();

        //计算秒杀时间
        long startAt = mllMiaoshaProduct.getStartDate().getTime();
        long endAt = mllMiaoshaProduct.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;
        //秒杀还没开始，倒计时
        if(now < startAt){
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now)/1000);
        }else if(now >endAt){
            //秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {
            //正在秒杀
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        map.put("miaoshaStatus",miaoshaStatus );
        map.put("remainSeconds",remainSeconds );
        map.put("product",mllMiaoshaProduct );
        map.put("user",user );
        return new ModelAndView("freemarker/miaosha",map);
    }
    @PostMapping("do_miaosha/{productId}/{verifyCode}")
    @ResponseBody
    public ResultResponse doMiaosha(@PathVariable("productId") long productId,
                                    @PathVariable("verifyCode") long verifyCode) {
        //首先肯定时判断是否登录，这个功能在后面的登录模块再来添加,这儿先定义一个userId
        Integer code = (Integer) redisUtils.get(productId + "_code");
        if (verifyCode != code){
            return ResultResponse.fail("验证码输入错误请重新输入");
    }
        String userId = UUID.randomUUID().toString().replace("-","" );
//        String userId ="123456";

        return miaoshaService.doMiaosha(productId,userId);
    }
    @RequestMapping(value = "/verifyCode/{productId}",method = RequestMethod.GET)
    @ResponseBody
    public ResultResponse<String> getMiaoshaVerifyCod(HttpServletResponse response,
                                                      @PathVariable("productId") long productId){
        //判断用户是否登陆
        try {
            //获得验证码图片
            BufferedImage image = miaoshaService.createVerifyCode(productId);
            OutputStream out = response.getOutputStream();
            //写入输入流
            ImageIO.write(image,"JPEG" ,out );
            out.flush();
            out.close();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return ResultResponse.fail();
        }
    }

}
