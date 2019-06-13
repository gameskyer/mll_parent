package com.xmcc.mll_miaosha.service;

import com.xmcc.mll_miaosha.entity.MllMiaoshaProduct;
import com.xmcc.mllcommon.result.ResultResponse;

import java.awt.image.BufferedImage;

public interface MiaoshaService {
    ResultResponse<MllMiaoshaProduct> queryById(long productId);
    ResultResponse doMiaosha(long productId, String userId);

    BufferedImage createVerifyCode(long productId);
}
