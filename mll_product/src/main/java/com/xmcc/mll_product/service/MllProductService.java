package com.xmcc.mll_product.service;

import com.xmcc.mll_product.entity.MllProduct;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.result.ResultResponse;
import org.springframework.stereotype.Service;

@Service
public interface MllProductService {
    //修改库存
    ResultResponse<MllOrderDTO> updateStock(MllOrderDTO mllOrderDTO);
    //根据id查询商品
    ResultResponse<MllProduct> queryById(Long id);

    void stockRollback(MllOrderDTO mllOrderDTO);
}
