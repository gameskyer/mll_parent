package com.xmcc.mll_miaosha.mapper;

import com.xmcc.mll_miaosha.entity.MllMiaoshaProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MiaoshaProductMapper {
    MllMiaoshaProduct queryById(long productId);
    int updateStockById(long productId);
    List<MllMiaoshaProduct> queryAll();
}
