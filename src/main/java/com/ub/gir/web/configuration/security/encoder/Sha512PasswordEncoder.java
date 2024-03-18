/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.encoder;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * <p>SHA-512 encoder</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/11
 */
public class Sha512PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return hashWithSHA512(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String hashedPassword = encode(rawPassword);
        return encodedPassword.equals(hashedPassword);
    }

    private String hashWithSHA512(String input) {

        StringBuilder result = new StringBuilder();

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update("salt".getBytes());

            byte[] digested = md.digest(input.getBytes());

            for (int i = 0; i < digested.length; i++) {
                result.append(Integer.toHexString(0xFF & digested[i]));
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Bad algorithm");
        }

        return result.toString();

    }

}
