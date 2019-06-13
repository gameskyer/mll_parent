package com.xmcc.mll_loginlogout.core.properties;

import com.xmcc.mll_loginlogout.browser.response.LoginType;
import lombok.Data;

@Data
public class BrowserProperties {
    //设置默认的登陆界面
    private String loginPage = "/xmcc_login.html";

    //默认返回json
    private LoginType loginType = LoginType.JSON;
}
