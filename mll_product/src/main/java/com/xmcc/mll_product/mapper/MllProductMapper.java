package com.xmcc.mll_product.mapper;

import com.xmcc.mll_product.entity.MllProduct;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;

@Mapper
public interface MllProductMapper {
    //根据商品id修改数量
    int updateStock(@Param("productId") Long productId, @Param("num") Integer num);

    MllProduct queryById(@Param("productId") Long productId);

    int stockRollback(@Param("productId")Long productId,@Param("num") Integer num);
}