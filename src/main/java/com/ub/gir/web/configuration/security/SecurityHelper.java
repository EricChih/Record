/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security;


import java.security.Principal;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Slf4j
public abstract class SecurityHelper {

    /**
     * Constructor
     */
    protected SecurityHelper() {
        super();
    }

    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean isAuthenticated() {
        if (!ObjectUtils.isEmpty(getCurrentAuthentication())) {
            return getCurrentAuthentication().isAuthenticated();
        }
        return false;
    }

    public static boolean isUserAccountAuthenticated() {
        if (isAuthenticated()) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(getCurrentAuthentication().getClass());
        }
        return false;
    }

    public static boolean isAnonymousAuthenticated() {
        if (isAuthenticated()) {
            return AnonymousAuthenticationToken.class.isAssignableFrom(getCurrentAuthentication().getClass());
        }
        return false;
    }

    public static boolean isRememberMeAuthenticated() {
        if (!isAuthenticated()) {
            return RememberMeAuthenticationToken.class.isAssignableFrom(getCurrentAuthentication().getClass());
        }
        return false;
    }

    public static Object getPrincipal() {

        if (isAuthenticated()) {

            final Object principal = getCurrentAuthentication().getPrincipal();

            if (!ObjectUtils.isEmpty(principal)) {
                return principal;
            }

        }

        return null;

    }

    public static String getCurrentUsername() {

        if (!ObjectUtils.isEmpty(getPrincipal())) {

            if (getPrincipal() instanceof UserDetails) {
                return ((UserDetails) getPrincipal()).getUsername();
            }

            if (getPrincipal() instanceof Principal) {
                return ((Principal) getPrincipal()).getName();
            }

            return getPrincipal().toString();

        }

        return null;

    }

    public static String getCurrentPassword() {

        if (!ObjectUtils.isEmpty(getPrincipal()) && (getPrincipal() instanceof UserDetails)) {
            return ((UserDetails) getPrincipal()).getPassword();
        }

        return null;

    }

    public static String getCurrentCredentials() {

        if (isAuthenticated()) {
            return getCurrentAuthentication().getCredentials().toString();
        }

        return null;

    }

    public static String[] getCurrentAuthorities() {

        if (!ObjectUtils.isEmpty(getPrincipal())) {

            Collection<? extends GrantedAuthority> userAuthorities;

            if (getPrincipal() instanceof UserDetails) {
                userAuthorities = ((UserDetails) getPrincipal()).getAuthorities();
            } else {
                userAuthorities = getCurrentAuthentication().getAuthorities();
            }

            return (String[]) userAuthorities
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray();

        }

        return null;

    }

}
