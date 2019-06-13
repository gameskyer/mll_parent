package com.xmcc.mll_loginlogout.core.social.qq;

import org.springframework.social.connect.Connection;
import org.springframework.stereotype.Component;

@Component
public class ConnectionSignUp implements org.springframework.social.connect.ConnectionSignUp {
    @Override
    public String execute(Connection<?> connection) {
        return connection.getDisplayName() ;
    }
}
