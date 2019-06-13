package com.xmcc.mll_product.controller;

import com.xmcc.mll_product.service.MllProductService;
import com.xmcc.mll_product.service.impl.MllProductServiceimpl;
import com.xmcc.mllcommon.api.ProductOrderAPI;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.result.ResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("product")
@Api(value = "商品接口" ,tags = {"操作商品接口"})
public class MllProductController implements ProductOrderAPI {
    @Autowired
    private MllProductService mllProductService;
    @PostMapping("updateStock")
    @ApiOperation(value = "修改商品库存",httpMethod = "POST",response = RestController.class)
    public ResultResponse<MllOrderDTO> updateStock(@Valid @ApiParam(name = "订单信息",value =
    "传入json格式",required = true)@RequestBody MllOrderDTO mllOrderDTO){
        return mllProductService.updateStock(mllOrderDTO);
    }

}
