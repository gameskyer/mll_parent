package com.xmcc.mll_loginlogout.core.social.qq;

import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;

public class QQAdapter implements ApiAdapter<QQ> {

    @Override
    //测试qq链接是否可用
    public boolean test(QQ api) {
        return false;
    }
    /**
     * 将不同服务商的不同数据结构适配为springsecurity social通用的数据结构
     * @param api
     * @param values
     */
    @Override
    public void setConnectionValues(QQ api, ConnectionValues values) {
        QQUserInfo qqUserInfo = api.getQQUserInfo();
        //QQ用户唯一标识
        values.setProviderUserId(qqUserInfo.getOpenId());
        //个人主页，微博或者博客等就有
        values.setImageUrl(null);
        //头像 选择40*40的，个别头像需要转码
        values.setImageUrl(qqUserInfo.getFigureurl_qq_1());
        //昵称
        values.setDisplayName(qqUserInfo.getNickname());
    }

    @Override
    //解绑需要
    public UserProfile fetchUserProfile(QQ api) {
        return null;
    }

    @Override
    public void updateStatus(QQ api, String message) {

    }
}
