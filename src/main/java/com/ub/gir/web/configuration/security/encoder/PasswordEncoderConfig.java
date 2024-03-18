/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.encoder;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.util.ObjectUtils;


/**
 * <p>定義密碼的解密方式</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * @return PasswordEncoder
     */
    //@SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder noOpPasswordEncoder() {
        //return NoOpPasswordEncoder.getInstance();
        return PlainTextPasswordEncoder.getInstance();
    }

    /**
     * @return PasswordEncoder
     */
    @Bean
    @Value("${hualiteq.security.encoder.encode:bcrypt}")
    public PasswordEncoder passwordEncoder(String encoder) {

        final Map<String, PasswordEncoder> encoders = new HashMap<>(6);

        encoders.put(EncoderProperties.NOOP.getEncoderName(), PlainTextPasswordEncoder.getInstance());
        encoders.put(EncoderProperties.BCRYPT.getEncoderName(), new BCryptPasswordEncoder());
        encoders.put(EncoderProperties.SCRYPT.getEncoderName(), new SCryptPasswordEncoder());
        encoders.put(EncoderProperties.PBKDF2.getEncoderName(), new Pbkdf2PasswordEncoder());
        encoders.put(EncoderProperties.ARGON2.getEncoderName(), new Argon2PasswordEncoder());
        if (ObjectUtils.isEmpty(encoders)) {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        final DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(encoder, encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(noOpPasswordEncoder());

        return passwordEncoder;

    }

}
