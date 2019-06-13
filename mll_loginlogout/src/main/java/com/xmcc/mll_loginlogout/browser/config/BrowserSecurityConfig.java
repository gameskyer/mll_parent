package com.xmcc.mll_loginlogout.browser.config;

import com.xmcc.mll_loginlogout.browser.handler.BrowserAuthenticationFailurHandler;
import com.xmcc.mll_loginlogout.browser.handler.BrowserAuthenticationSuccessHandler;
import com.xmcc.mll_loginlogout.core.filter.ValidateCodeFilter;
import com.xmcc.mll_loginlogout.core.properties.SecurityProperties;
import com.xmcc.mll_loginlogout.core.vaidate.ValidateCode;
import com.xmcc.mll_loginlogout.core.vaidate.sms.authentication.SmsCodeAuthenticationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.social.security.SpringSocialConfigurer;

@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter  {
    @Autowired
    private SecurityProperties properties;
    @Autowired
    private BrowserAuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private BrowserAuthenticationFailurHandler authenticationFailureHandler;
    @Autowired
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;
    @Autowired
    private SpringSocialConfigurer springSocialConfigurer;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    protected void configure(HttpSecurity http) throws Exception{
        /**
         * 指定表单登陆，所有的请求都要经过身份认证后才能访问
         * */
        //http.forLogin()表单格式登陆
        ValidateCodeFilter filter = new ValidateCodeFilter();
        filter.setBrowserAuthenticationFailureHandler(authenticationFailureHandler);

        ValidateCodeFilter smsfilter = new ValidateCodeFilter();
        smsfilter.setBrowserAuthenticationFailureHandler(authenticationFailureHandler);
        //告诉security在执行用户名密码验证前，先执行验证码的校验
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(smsfilter,UsernamePasswordAuthenticationFilter.class )
                //自定义登陆页面
                .formLogin()
                .loginPage("/authentication/require")
                .loginProcessingUrl("/authentication/from")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .and()
                .authorizeRequests()
                //访问这个页面不需要认证
                .antMatchers("/authentication/require",properties.getBrowser().getLoginPage(),"/code/*").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                //关闭跨站防伪验证
                .csrf()
                .disable()
        //在这里添加我们自己短信验证的配置
                .apply(smsCodeAuthenticationSecurityConfig)
                .and()
                .apply(springSocialConfigurer);

        //弹窗登陆
//        http.httpBasic()
//                .and()
//                .authorizeRequests()
//                .anyRequest()
//                .authenticated();
    }
}
