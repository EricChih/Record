/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.encoder;


import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
public class PlainTextPasswordEncoder implements PasswordEncoder {

    private static final PasswordEncoder INSTANCE = new PlainTextPasswordEncoder();

    private PlainTextPasswordEncoder() {
        super();
    }

    public static PasswordEncoder getInstance() {
        return INSTANCE;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
    }

}
