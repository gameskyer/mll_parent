package com.xmcc.mll_miaosha.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration//表示为配置类
public class DruidConfig {



    // value=bean的名字  initMethod = 创建方法  destroyMethod = 销毁方法
    @Bean(value = "druidDataSource",initMethod = "init",destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.druid")
    public DruidDataSource druidDataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setProxyFilters(Lists.newArrayList(statFilter()));
        return druidDataSource;
    }
    @Bean
    public StatFilter statFilter(){//需要集合，创建集合配置
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(true);//慢查询是否记录日志
        statFilter.setSlowSqlMillis(5);//慢查询时间
        statFilter.setLogSlowSql(true);
        statFilter.setMergeSql(true);//格式化sql
        return statFilter;
    }
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        //durid监控平台
        return new ServletRegistrationBean(new StatViewServlet(),"/durid/*");
    }
}
