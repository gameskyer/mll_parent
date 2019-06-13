package com.xmcc.mll_order.mapper;

import com.xmcc.mllcommon.entity.MllOrderItem;
import org.mapstruct.Mapper;

@Mapper
public interface MllOrderItemMapper {
    int insertOrder(MllOrderItem mllOrderItem);
}