/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.provider;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ub.gir.web.configuration.security.provider.user.EmbeddedUserProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/3/16
 */
@Component
@Slf4j
public class AuthInMemoryUserDetailsServiceImpl implements UserDetailsService {

    public static final String ROLE_PREFIX = "ROLE_";

    private final EmbeddedUserProperties embeddedUserProperties;

    public AuthInMemoryUserDetailsServiceImpl(EmbeddedUserProperties embeddedUserProperties) {
        super();
        this.embeddedUserProperties = embeddedUserProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("User : {}", users());

        return users().stream()
                .filter(e -> e.getUsername().equals(username))
                .findAny()
                .orElse(null);

    }

    public UserDetailsService authInMemoryUserDetailsService() {
        return new InMemoryUserDetailsManager(users());
    }

    public Collection<UserDetails> getUsers() {
        return users();
    }

    protected Collection<UserDetails> users() {

        return embeddedUserProperties.getUsers()
                .stream()
                .map(e ->
                        User.builder()
                                .username(e.getUsername())
                                .password(e.getPassword())
                                .disabled(!e.isEnabled())
                                .accountExpired(e.isAccountExpired())
                                .credentialsExpired(e.isCredentialsExpired())
                                .accountLocked(e.isAccountLocked())
                                .roles(convertRoleContent(e.getRoles()))
                                //.authorities(authorities(e.getRoles()))
                                .build()
                )
                .collect(Collectors.toList());

    }

    private String[] convertRoleContent(String role) {

        return Stream.of(role.split(","))
                .map(String::trim)
                .toArray(String[]::new);

    }

    private String[] convertRoleContent(List<String> roles) {

        return roles.stream()
                .map(String::trim)
                .toArray(String[]::new);

    }

    private Collection<? extends GrantedAuthority> convertAuthorityContent(String authority) {

        return Stream.of(authority.split(","))
                .map(String::trim)
                .map(e -> !e.startsWith(ROLE_PREFIX) ? ROLE_PREFIX.concat(e):e)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    }

    private Collection<? extends GrantedAuthority> convertAuthorityContent(List<String> authorities) {

        return authorities.stream()
                .map(String::trim)
                .map(e -> !e.startsWith(ROLE_PREFIX) ? ROLE_PREFIX.concat(e):e)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    }

}
