/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
public abstract class SecurityAttributes {

    public static final String SESSION_KEY = "JSESSIONID";

    public static final String ANONYMOUS_KEY = "ANONYMOUS";

    public static final String REMEMBER_ME_KEY = "REMEMBER-ME";

    public static final String CSRF_PARAMETER_KEY = "_csrf";

    public static final String CSRF_HEADER_KEY = "X-CSRF-TOKEN";

    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    public static final String COOKIE_CSRF_HEADER_KEY = "X-XSRF-TOKEN";

}
