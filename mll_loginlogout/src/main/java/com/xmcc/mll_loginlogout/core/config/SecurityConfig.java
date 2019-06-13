package com.xmcc.mll_loginlogout.core.config;

import com.xmcc.mll_loginlogout.core.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
//让SecurituProperties类的作用生效
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {
}
