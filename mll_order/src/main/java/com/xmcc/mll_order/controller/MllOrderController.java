package com.xmcc.mll_order.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.xmcc.mll_order.dto.MllOrderItemDTO;
import com.xmcc.mll_order.service.MllOrderService;
import com.xmcc.mllcommon.result.CommonResultEnums;
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
import java.util.List;

@RestController
@RequestMapping("order")
@Api(description = "订单信息接口")//别忘了swagger2的注解 @EnableSwagger2
public class MllOrderController {
    @Autowired
    private MllOrderService mllOrderService;
    @PostMapping("create")
    @ApiOperation(value = "创建订单接口", httpMethod = "POST", response =ResultResponse.class)
    @HystrixCommand(fallbackMethod="createOrderFallback")
    public ResultResponse create(@Valid @ApiParam(name="订单项集合",value = "传入json格式",required = true)
                                 @RequestBody List<MllOrderItemDTO> mllOrderItemDTOList){
        return mllOrderService.createOrder(mllOrderItemDTOList);
    }
    private ResultResponse createOrderFallback(List<MllOrderItemDTO> mllOrderItemDTOS){
        return ResultResponse.fail(CommonResultEnums.SERVER_EROOR.getMsg(),CommonResultEnums.SERVER_EROOR.getCode());
    }
}
