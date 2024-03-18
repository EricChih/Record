/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.encoder;


import java.util.Objects;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/11
 */
@Getter
@RequiredArgsConstructor
public enum EncoderProperties {

    NOOP(1, "noop"),
    BCRYPT(2, "bcrypt"),
    SCRYPT(3, "scrypt"),
    PBKDF2(4, "pbkdf2"),
    ARGON2(5, "argon2"),
    SHA256(6, "sha256"),
    ;

    private final int encoderCode;

    private final String encoderName;

    public static EncoderProperties findEncoderCode(String encoderCode) {
        return Stream.of(values())
                .filter(e -> Objects.equals(e.encoderCode, encoderCode))
                .findFirst()
                .orElse(null);
    }

    public static EncoderProperties findEnum(String enumName) {
        return Stream.of(values())
                .filter(e -> e.name().equalsIgnoreCase(enumName))
                .findFirst()
                .orElse(null);
    }

    public static boolean exists(String enumName) {
        return Stream.of(values())
                .anyMatch(e -> e.name().equals(enumName));
    }

}
