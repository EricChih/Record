/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.constant;


import java.util.Objects;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * <p>定義地區的屬性</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/13
 */
@Getter
@RequiredArgsConstructor
public enum SiteAttributes {

    TAIPEI("002","taipei"),
    KAOHSIUNG("007","kaohsiung"),
    ;

    private final String siteCode;

    private final String siteName;

    public static SiteAttributes findSiteCode(String siteCode) {
        return Stream.of(values())
                .filter(e -> Objects.equals(e.siteCode, siteCode))
                .findFirst()
                .orElse(null);
    }

    public static SiteAttributes findEnum(String enumName) {
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
