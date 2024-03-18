/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security;


import java.util.Objects;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Getter
@RequiredArgsConstructor
public enum UrlPathResourceConfig {

    DEFAULT(1,"target", "/user/all", null),
    LOGIN(2,"login", "/login","登入成功"),
    LOGOUT(3,"logout", "/login?msg=logout","登出成功"),
    ERROR(4,"error", "/login?msg=error","操作失敗"),
    FAILED(5,"failed", "/login?msg=failed&wrongTimes=","登入失敗：帳號或密碼錯誤，錯誤超過 {0} 次將鎖定帳號！"),
    LOCKED(6,"locked", "/login?msg=locked&unlockRole=","帳號已被鎖定"),
    DISABLED(7,"disabled", "/login?msg=disabled","帳號已被停用"),
    EXPIRED(8,"expired", "/login?msg=expired","帳號已過期"),
    AUTHENTICATION(9,"authentication", "/login?msg=authentication","認證失敗"),
    AUTHORIZATION(10,"authorization", "/login?msg=authorization","授權失敗"),
    ACCESS_DENIED(11,"access_denied", "/login?msg=access_denied","拒絕訪問"),
    SESSION_INVALID(12,"session_invalid", "/login?msg=session_invalid","無效的連線，請重新登入"),
    SESSION_EXPIRED(13,"session_expired", "/login?msg=session_expired","連線已失效"),
    SESSION_TIMEOUT(14,"session_timeout", "/login?msg=session_timeout","帳號靜止已逾時，請重新登入"),
    PWD_DEFAULT(15,"target", "/pwd/user", null),
    PWD_MISMATCH(16,"mismatch", "/pwd/user?msg=mismatch","當前密碼不符合目前系統密碼設定，請更改密碼"),
    PWD_EXPIRING(17,"expiring", "/pwd/user?msg=expiring","當前密碼將於{0}天後到期，請提前更改密碼"),
    PWD_EXPIRED(18,"expired", "/pwd/user?msg=expired","當前密碼已到期，請更改密碼"),
    PWD_AS_DEFAULT_ERROR(19, "pwd_as_default", "/pwd/user?msg=pwd_as_default", "此為預設密碼，請先執行密碼更改"),
    LOGOUT_PWD_CHANGE(20,"logout", "/login?msg=pwdchange","變更密碼成功"),
    ;

    private final int pageCode;

    private final String pageName;

    private final String pagePath;

    private final String pageInformation;

    public static UrlPathResourceConfig findPageCode(String pageCode) {
        return Stream.of(values())
                .filter(e -> Objects.equals(e.pageCode, pageCode))
                .findFirst()
                .orElse(null);
    }

    public static UrlPathResourceConfig findEnum(String enumName) {
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
