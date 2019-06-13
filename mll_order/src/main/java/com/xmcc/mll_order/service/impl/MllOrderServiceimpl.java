package com.xmcc.mll_order.service.impl;

import com.google.common.collect.Lists;
import com.xmcc.mll_order.client.ProductApi;
import com.xmcc.mll_order.common.OrderEnums;
import com.xmcc.mll_order.dto.MllOrderItemDTO;
import com.xmcc.mll_order.entity.MllOrder;
import com.xmcc.mll_order.mapper.MllOrderItemMapper;
import com.xmcc.mll_order.mapper.MllOrderMapper;
import com.xmcc.mll_order.rabbitmq.RabbitSenderUtil;
import com.xmcc.mll_order.service.MllOrderService;
import com.xmcc.mllcommon.api.ProductOrderAPI;
import com.xmcc.mllcommon.dto.MllOrderDTO;
import com.xmcc.mllcommon.entity.MllOrderItem;
import com.xmcc.mllcommon.exception.CustomException;
import com.xmcc.mllcommon.result.CommonResultEnums;
import com.xmcc.mllcommon.result.ResultResponse;
import com.xmcc.mllcommon.util.BigDecimalUtil;
import com.xmcc.mllcommon.util.SnowFlakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
@Slf4j
public class MllOrderServiceimpl implements MllOrderService {
    @Autowired
    private MllOrderMapper mllOrderMapper;
    @Autowired
    private MllOrderItemMapper mllOrderItemMapper;
    @Autowired
    private ProductOrderAPI productOrderAPI;
    @Autowired
    private RabbitSenderUtil rabbitSenderUtil;

    @Autowired
    private SnowFlakeUtils snowFlakeUtils;
    @Override
    public ResultResponse<MllOrderDTO> createOrder(List<MllOrderItemDTO> mllOrderItemDTOS) {
      //组装商品微服务需要的参数MllOrderDTO
        MllOrderDTO mllOrderDTO = new MllOrderDTO();
        //通过Snwoflake生成Id
        long orderId = snowFlakeUtils.nextId();
        mllOrderDTO.setOrderId(orderId);
        //创建订单项
        List<MllOrderItem> mllOrderItems = Lists.newArrayList();
        for(MllOrderItemDTO mllOrderItemDTO:mllOrderItemDTOS){
            MllOrderItem mllOrderItem = new MllOrderItem();
            //设置ordierId
            mllOrderItem.setOrderId(orderId);
            //商品ID与数量
            mllOrderItem.setGoodsId(mllOrderItemDTO.getProductId());
            mllOrderItem.setNum(mllOrderItemDTO.getQuantity());
            mllOrderItems.add(mllOrderItem);
        }
        //组装参数
        mllOrderDTO.setMllOrderItemList(mllOrderItems);
        //调用商品微服务接口
        ResultResponse<MllOrderDTO> mllOrderDTOResultResponse = productOrderAPI.updateStock(mllOrderDTO);
//        ResultResponse<MllOrderDTO> mllOrderDTOResultResponse =new ResultResponse<>();
//        mllOrderDTOResultResponse.setMsg(CommonResultEnums.SERVER_EROOR.getMsg());
//        mllOrderDTOResultResponse.setData(mllOrderDTO);
//        mllOrderDTOResultResponse.setCode(CommonResultEnums.SERVER_EROOR.getCode());
        //判断是否失败，如果失败获取商品微服务那边的信息即可事务回滚
        if(mllOrderDTOResultResponse.getCode() == CommonResultEnums.FAIL.getCode()){
            log.error("创建订单失败，异常原因为为{}",mllOrderDTOResultResponse.getMsg() );
            throw new CustomException(mllOrderDTOResultResponse.getMsg());
        }
        MllOrderDTO data = mllOrderDTOResultResponse.getData();
        insertOrderByOrderDTO(data);
        return ResultResponse.success(data);
    }
    /**
     * 根据dto插入order表
     * @param data
     */
    public void insertOrderByOrderDTO(MllOrderDTO data) {
        try {
            MllOrder mllOrder = new MllOrder();
            mllOrder.setOrderId(data.getOrderId());
            //这儿数据库设计的时候 把状态用的varchar类型 请把数据库status字段varchar长度改为3
            //设置未支付状态
            mllOrder.setStatus(String.valueOf(OrderEnums.NO_PAY.getCode()));
            //循环插入订单项并计算总价格
            BigDecimal totalPrice = new BigDecimal("0");
            for (MllOrderItem mllOrderItem : data.getMllOrderItemList()) {
                totalPrice = BigDecimalUtil.add(totalPrice, mllOrderItem.getTotalFee());
                //插入订单项
                insertOrderItem(mllOrderItem);
            }
            //设置总价格
            mllOrder.setPayment(totalPrice);
            //订单还有很多其它信息 这儿用不到 暂时不去关注 重要一定的就是根据当前登录用户 获得用户的信息 收获地址等
            mllOrderMapper.insertOrder(mllOrder);
            //模拟异常
            throw  new RuntimeException("出现异常了哦");
        }catch (Exception e){
            log.error("扣减库存后，插入订单与订单项到数据库出现异常，异常信息为:{}",e);
            //发送错误消息通过rabbitmq 消息驱动 来实现最终一致性
            rabbitSenderUtil.OrderErrorToProduct(snowFlakeUtils.nextId(),data);
            //抛出异常让事务回滚MessageConverter
            throw  new RuntimeException(e);
        }
    }
    /**
     * 插入订单项
     */
    private void insertOrderItem(MllOrderItem mllOrderItem){
        //商品详情表
        mllOrderItem.setItemId(mllOrderItem.getGoodsId());
        mllOrderItemMapper.insertOrder(mllOrderItem);
    }
}
