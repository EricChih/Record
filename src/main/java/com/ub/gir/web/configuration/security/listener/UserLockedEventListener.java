package com.ub.gir.web.configuration.security.listener;


import javax.annotation.Resource;

import com.ub.gir.web.entity.db1.master.CfgPersonDB1Master;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UserLockedEventListener implements ApplicationListener<AuthenticationFailureLockedEvent> {

    @Resource
    private CfgPersonDB1MasterRepository cfgPersonDB1MasterRepository;

    @Override
    public void onApplicationEvent(AuthenticationFailureLockedEvent event) throws UsernameNotFoundException {
        final String username = event.getAuthentication().getName();

        if (ToolPlugins.checkAuthorization("authorization")) {

            final CfgPersonDB1Master cfgPerson = cfgPersonDB1MasterRepository.findByName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid username does not exist !"));

            boolean isUserDisabled = cfgPerson.getStatus().equals("0");

            if (isUserDisabled) {
                String unlockRole = cfgPerson.getRole().equals("supervisor") ? "Manager":"Admin";
                throw new LockedException(unlockRole);
            }

        }

    }

}
