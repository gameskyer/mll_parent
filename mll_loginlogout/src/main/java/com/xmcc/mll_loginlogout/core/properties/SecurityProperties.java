package com.xmcc.mll_loginlogout.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xmcc.security")
@Data
public class SecurityProperties {
    BrowserProperties browser = new BrowserProperties();
}
