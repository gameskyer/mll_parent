package com.xmcc.mll_loginlogout.core.social.qq;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;

public class QQConnectionFactory extends OAuth2ConnectionFactory<QQ> {
    public QQConnectionFactory(String appId,String appSecurity){
        //我们这里直接写qq就可以了 如果有其他的服务提供商在提取出来就可以了
        super("qq",new QQServiceProvider(appId,appSecurity ),new QQAdapter());
    }
}
