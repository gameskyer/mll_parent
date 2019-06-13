package com.xmcc.mll_loginlogout.core.exception;

import org.springframework.security.core.AuthenticationException;

public class ValidateCodeException extends AuthenticationException {
    public static final long serialVerssionUID=-7285211528095468156L;
    public ValidateCodeException(String msg){
        super(msg);
    }
}
