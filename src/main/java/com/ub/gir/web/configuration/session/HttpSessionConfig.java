/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.session;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
@Slf4j
public class HttpSessionConfig {

    @Bean
    public ServletListenerRegistrationBean<SessionListenerWithMetrics> sessionListenerWithMetrics() {
        ServletListenerRegistrationBean<SessionListenerWithMetrics> listenerRegBean = new ServletListenerRegistrationBean<>();
        listenerRegBean.setListener(new SessionListenerWithMetrics());
        return listenerRegBean;
    }

}
