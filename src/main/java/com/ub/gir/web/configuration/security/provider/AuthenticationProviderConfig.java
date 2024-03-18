/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.provider;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import com.ub.gir.web.configuration.security.SecurityAttributes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class AuthenticationProviderConfig {

    @Resource(name = "authInMemoryUserDetailsServiceImpl")
    private UserDetailsService authInMemoryUserDetailsService;

    @Resource(name = "authDaoUserDetailsServiceImpl")
    private UserDetailsService authDaoUserDetailsService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthenticationEventPublisher authenticationEventPublisher;

    /**
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider anonymousAuthenticationProvider() {
        AnonymousAuthenticationProvider anonymousAuthenticationProvider = new AnonymousAuthenticationProvider(SecurityAttributes.ANONYMOUS_KEY);
        return anonymousAuthenticationProvider;
    }

    /**
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider(SecurityAttributes.REMEMBER_ME_KEY);
    }

//    @Bean
//    public AuthenticationProvider inMemoryAuthenticationProvider() {
//        InMemoryAuthenticationProvider inMemoryAuthenticationProvider = new InMemoryAuthenticationProvider();
//        inMemoryAuthenticationProvider.setUserDetailsService(authInMemoryUserDetailsService);
////		inMemoryAuthenticationProvider.setAuthoritiesMapper ( roleHierarchyAuthoritiesMapper ) ;
//        inMemoryAuthenticationProvider.setPasswordEncoder(passwordEncoder);
//
//        return inMemoryAuthenticationProvider;
//    }

    /**
     * @return DaoAuthenticationProvider
     */
    @Bean
    //@Primary
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setUserDetailsService(authDaoUserDetailsService);
//		daoAuthenticationProvider.setAuthoritiesMapper ( roleHierarchyAuthoritiesMapper ) ;
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        return daoAuthenticationProvider;
    }

    /**
     * @return ProviderManager
     */
    @Bean
    public ProviderManager providerManager() {
        ProviderManager providerManager = new ProviderManager(providers());
        providerManager.setAuthenticationEventPublisher(authenticationEventPublisher);
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }

    /**
     * @return List < AuthenticationProvider >
     */
    private List<AuthenticationProvider> providers() {

        return Arrays.asList(
//                inMemoryAuthenticationProvider(),
                daoAuthenticationProvider()
        );
    }
}