package com.xmcc.mll_loginlogout.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceimpl implements UserDetailsService, SocialUserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;


    public SocialUserDetails builderUser(String userId){
        /**
         * 参数1：用户名
         * 参数2：密码
         * 参数3：是否可用 参数4：用户是否过期 参数5：密码是否过期 参数6：用户是否冻结
         * 参数7：授权相关
         */
        String password = passwordEncoder.encode("123456");
        log.info("password:{}",password);
        return new SocialUser(userId, password,true,true,true,true
                , AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
    }


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.info("用户名密码登录----------username:{}",s);
        return builderUser(s);
    }

    @Override
    public SocialUserDetails loadUserByUserId(String s) throws UsernameNotFoundException {
        log.info("社交登录-------------username:{}",s);
        return builderUser(s);
    }
}
