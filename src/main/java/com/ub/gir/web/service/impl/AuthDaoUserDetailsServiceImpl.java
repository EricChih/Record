package com.ub.gir.web.service.impl;


import com.ub.gir.web.entity.db1.master.CfgPersonDB1Master;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;


@Service
public class AuthDaoUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private CfgPersonDB1MasterRepository cfgPersonDB1MasterRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        final CfgPersonDB1Master cfgPerson = cfgPersonDB1MasterRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username does not exist !"));

        final String name = cfgPerson.getName();
        final char[] password = cfgPerson.getPasswordNew().toCharArray();
        final String role = cfgPerson.getRole();

        boolean isUserLocked = cfgPerson.getStatus().equals("0");

        UserDetails user = User.builder()
                .username(name)
                .password(new String(password))
                .disabled(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(isUserLocked)
                .roles(role)
                .build();
        Arrays.fill(password, '0');

        return user;
    }

}
