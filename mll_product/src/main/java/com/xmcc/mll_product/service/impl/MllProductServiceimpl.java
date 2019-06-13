package com.xmcc.mll_product.service.impl;

import com.xmcc.mll_product.entity.MllProduct;
import com.xmcc.mll_product.mapper.MllProductMapper;
import com.xmcc.mll_product.service.MllProductService;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.entity.MllOrderItem;
import com.xmcc.mllcommon.exception.CustomException;
import com.xmcc.mllcommon.result.ResultResponse;
import com.xmcc.mllcommon.util.BigDecimalUtil;
import com.xmcc.mllcommon.util.SnowFlakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class MllProductServiceimpl implements MllProductService {
    @Autowired
    private MllProductMapper mllProductMapper;
    @Autowired
    private SnowFlakeUtils productSnowFlake;
    @Override
    @Transactional
    public ResultResponse<MllOrderDTO> updateStock(MllOrderDTO mllOrderDTO) {
        //获得订单的商品列表
        List<MllOrderItem> mllOrderItems = mllOrderDTO.getMllOrderItemList();
        //循环根据Id修改库存
        for (MllOrderItem mllOrderItem:mllOrderItems){
            //根据Id查询商品
            ResultResponse<MllProduct> mllProductResultResponse = queryById(mllOrderItem.getGoodsId());
            //根据ID查询到商品结果
            MllProduct mllProduct = mllProductResultResponse.getData();
            //商品不存在直接抛异常事务回滚
            if(mllProduct == null){
                log.error("商品库存扣减中出现商品I的对应的商品不存在，商品ID{}",mllOrderItem.getGoodsId() );
                //TODO:缺少ProductEnums
                throw new CustomException("商品库存扣减中出现商品I的对应的商品不存在");
            }
            int i = mllProductMapper.updateStock(mllOrderItem.getGoodsId(),mllOrderItem.getNum() );
            //说明修改不成功，库存不足，抛出异常事务回滚
            if (i<1){
                log.error("商品库存扣减中出现库存不足，商品id：{}",mllOrderItem.getGoodsId());
                //TODO:缺少ProductEnums
                throw new CustomException("商品库存扣减中出现库存不足");
            }
            //修改成功就将信息设置进入MllOrderDTO 返回给订单微服务去存储
            //设置ID
            mllOrderItem.setId(productSnowFlake.nextId());
//            价格
            mllOrderItem.setPrice(mllProduct.getPrice());
//            总价格
            mllOrderItem.setTotalFee(BigDecimalUtil.multi(mllProduct.getPrice(),
                    mllOrderItem.getNum()));
            //标题
            mllOrderItem.setTitle(mllProduct.getTitle());
            //图片
            mllOrderItem.setPicPath(mllProduct.getImage());

        }
        return ResultResponse.success(mllOrderDTO);
    }

    @Override
    public ResultResponse<MllProduct> queryById(Long id) {
        MllProduct mllProduct = mllProductMapper.queryById(id);
        if (mllProduct == null){
            return ResultResponse.fail("没有该商品");
        }
        return ResultResponse.success(mllProduct);
    }

    @Override
    @Transactional
    public void stockRollback(MllOrderDTO mllOrderDTO) {
        List<MllOrderItem> mllOrderItemList = mllOrderDTO.getMllOrderItemList();
        for (MllOrderItem mllOrderItem : mllOrderItemList) {
            mllProductMapper.stockRollback(mllOrderItem.getGoodsId(),mllOrderItem.getNum());
        }
    }
}
