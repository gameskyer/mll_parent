package com.xmcc.mll_order.service;

import com.xmcc.mll_order.dto.MllOrderItemDTO;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.result.ResultResponse;

import java.util.List;

public interface MllOrderService {
    ResultResponse<MllOrderDTO> createOrder(List<MllOrderItemDTO> mllOrderItemDTOS);
}
