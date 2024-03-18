/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.entrypoint;


import com.ub.gir.web.configuration.security.UrlPathResourceConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;


/**
 * <p>web 頁面的進入點</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class AuthenticationEntryPointConfig {

    private static final String ENTRY_POINT_PAGE = UrlPathResourceConfig.LOGIN.getPagePath();

    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
        MvcAuthenticationEntryPoint mvcAuthenticationEntryPoint = new MvcAuthenticationEntryPoint(ENTRY_POINT_PAGE);
        return mvcAuthenticationEntryPoint;
    }

}
