package com.xmcc.mll_miaosha.service.impl;

import com.xmcc.mll_miaosha.entity.MllMiaoshaOrder;
import com.xmcc.mll_miaosha.entity.MllMiaoshaProduct;
import com.xmcc.mll_miaosha.mapper.MiaoshaOrderMapper;
import com.xmcc.mll_miaosha.mapper.MiaoshaProductMapper;
import com.xmcc.mll_miaosha.service.MiaoshaService;
import com.xmcc.mllcommon.result.ResultResponse;
import com.xmcc.mllcommon.util.SnowFlakeUtils;
import com.xmcc.mll_miaosha.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class MiaoshaServiceimpl implements MiaoshaService {
    @Autowired
    private MiaoshaProductMapper miaoshaProductMapper;
    @Autowired
    private MiaoshaOrderMapper miaoshaOrderMapper;
    @Autowired
    private SnowFlakeUtils snowFlakeUtils;
    @Autowired
    private RedisUtils redisUtils;
    @Override
    public ResultResponse<MllMiaoshaProduct> queryById(long productId) {
        return ResultResponse.success(miaoshaProductMapper.queryById(productId));
    }
    @PostConstruct
    public  void  initRedisStock(){
        log.info("初始化执行方法" );
        List<MllMiaoshaProduct> mllMiaoshaProducts = miaoshaProductMapper.queryAll();
        if (mllMiaoshaProducts == null){
            return;
        }
        //商品的ID+固定的前缀为key 商品的库存为value
        for(MllMiaoshaProduct mllMiaoshaProduct:mllMiaoshaProducts){
            mllMiaoshaProduct.getStockCount();
            redisUtils.set("stock-"+mllMiaoshaProduct.getProductId(),mllMiaoshaProduct.getStockCount() );
            redisUtils.set("product"+mllMiaoshaProduct.getProductId(), mllMiaoshaProduct);
        }
    }
    @Override
    @Transactional
    public ResultResponse doMiaosha(long productId, String userId) {
        //先通过redis进行库存判断，减少数据库访问的次数 自减，如果库存小于或等于0，那么就直接返回
        Long decr = redisUtils.decr("stock-"+productId);
        if(decr < 0){
            return ResultResponse.fail("库存不足");
        }
        //1、判断库存是否足够
        MllMiaoshaProduct mllMiaoshaProduct = (MllMiaoshaProduct) redisUtils.get("product"+productId);
//        MllMiaoshaProduct mllMiaoshaProduct = miaoshaProductMapper.queryById(productId);
//        int stockCount = mllMiaoshaProduct.getStockCount();
//        if(stockCount<=0){
//            return ResultResponse.fail("库存不足");
//        }
        //2、判断用户是否已经秒杀过
        int result = miaoshaOrderMapper.queryByUserIdAndProductId(productId,userId );
        if(result>0){
            return ResultResponse.fail("用户已经秒杀过");
        }
        //3、减少秒杀商品库存
        int i = miaoshaProductMapper.updateStockById(productId);
        if(i == 0){
            return ResultResponse.fail("已经被抢完了");
        }
        //TODO 削峰限流
        //4、写入订单
        MllMiaoshaOrder mllMiaoshaOrder = new MllMiaoshaOrder();
        //订单id
        long orderId = snowFlakeUtils.nextId();
        mllMiaoshaOrder.setOrderId(orderId);
        //价格
        mllMiaoshaOrder.setPayment(mllMiaoshaProduct.getMiaoshaPrice());
        mllMiaoshaOrder.setProductId(productId);
        mllMiaoshaOrder.setUserId(userId);
        mllMiaoshaOrder.setStatus(1);
        miaoshaOrderMapper.insert(mllMiaoshaOrder);
        return ResultResponse.success(mllMiaoshaOrder);
    }

    @Override
    public BufferedImage createVerifyCode(long productId) {
        //验证码图片 宽度 高度
        int width = 80;
        int heigh = 32;
        //创建图片
        BufferedImage image = new BufferedImage(width,heigh ,BufferedImage.TYPE_3BYTE_BGR );
        //获得画笔
        Graphics g = image.getGraphics();
        //背景颜色等设置
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0,0 ,width ,heigh );
        //边框设置
        g.setColor(Color.black);
        g.drawRect(0, 0,width-1 ,heigh-1);
        //生成的数字
        Random rdm = new Random();
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(heigh);
            g.drawOval(x,y ,0 , 0);
        }
        //生成图片验证码的内容
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0,100,0));
        g.setFont(new Font("Candara", Font.BOLD,24 ));
        g.drawString(verifyCode,8 ,24 );
        g.dispose();
        //计算出验证码中数学表达式的结果 并放入redis中，并设置60秒过期
        int result = calc(verifyCode);
        redisUtils.setex(productId+"_code",result ,60 );
        //输出图片
        return image;
    }
    private static char[] ops = new char[] {'+', '-', '*'};

    private String generateVerifyCode(Random random){
        int num1 = random.nextInt(10);
        int num2 = random.nextInt(10);
        int num3 = random.nextInt(10);
        char op1 = ops[random.nextInt(3)];
        char op2 = ops[random.nextInt(3)];
        String exp = "" + num1+op1+num2+op2+num3;
        return exp;
    }

    private static int calc(String exp){
        try {
            //通过ScriptEngine引擎计算出结果
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
