package com.xmcc.mll_order.mapper;

import com.xmcc.mllcommon.rabbit.MllMessage;
import org.apache.ibatis.annotations.Param;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

public interface MllMessageMapper {
    int insert(MllMessage message);
    void updateSendCount(@Param("expire") Date expire, @Param("status") int status, @Param("id")Long id);
    void updateStatusById(@Param("status")int status,@Param("id") Long id);
    MllMessage queryById(@Param("id")Long id);
    List<MllMessage> queryByStatus(@Param("status")int status);
}
