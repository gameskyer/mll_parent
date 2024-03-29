package com.xmcc.mll_miaosha.config;

import com.xmcc.mllcommon.util.SnowFlakeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowFlakeConfig {
    @Bean("miaoshaSnowFlake")
    public SnowFlakeUtils snowFlakeUtils(){
        //第6个机房的第11台机器
        return new SnowFlakeUtils(5,10);
    }
}
