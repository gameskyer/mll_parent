package com.xmcc.mll_order.client;

import com.xmcc.mllcommon.api.ProductOrderAPI;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.result.ResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name="product")
public interface ProductApi extends ProductOrderAPI {
    @PostMapping("/product/updateStock")
    ResultResponse<MllOrderDTO> updateStock(MllOrderDTO mllOrderDTO);
}
