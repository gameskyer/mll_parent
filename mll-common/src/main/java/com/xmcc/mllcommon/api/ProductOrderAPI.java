package com.xmcc.mllcommon.api;

import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.result.ResultResponse;

public interface ProductOrderAPI {
    ResultResponse<MllOrderDTO> updateStock(MllOrderDTO mllOrderDTO);
}
