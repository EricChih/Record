/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.listener;


import javax.annotation.Resource;

import com.ub.gir.web.configuration.cache.CacheLocal;
import com.ub.gir.web.configuration.cache.caffeine.ContextStatusCacheSingleton;
import com.ub.gir.web.service.PwdService;
import com.ub.gir.web.service.UserService;

import com.ub.gir.web.util.DataUtil;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


/**
 * <p>監聽 Security 登入失敗事件</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/12
 */
@Component
@Slf4j
public class AuthenticationFailureEvenListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Resource
    private PwdService pwService;

    @Resource
    private UserService userService;

    private final CacheLocal<String, Integer> cacheLocal;

    public AuthenticationFailureEvenListener() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
        cacheLocal = ContextStatusCacheSingleton.getInstance().getCacheLocal();
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {

        log.info("authentication failed exception : {}", event.getException().getClass());
        int wrongTimes;

        try {
            wrongTimes = pwService.getPasswordWrongTimes();
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        if (wrongTimes <= 0) {
            return;
        }
        if (event.getException() instanceof BadCredentialsException) {

            final String username = event.getAuthentication().getName();

            Integer attempt = cacheLocal.getIfPresent(username);

            if (ObjectUtils.isEmpty(attempt)) {
                cacheLocal.put(username, 1);
            } else {
                cacheLocal.put(username, ++attempt);
            }

            attempt = cacheLocal.get(username);

            log.info("Number of incorrect entries : {}", attempt);

            if (attempt >= wrongTimes) {
                cacheLocal.invalidate(username);
                String logDetails = DataUtil.preventLogForging(username);
                log.info("This account is locked : {}", logDetails);
                userService.stopUserByName(username, username);
            }
            throw new BadCredentialsException(String.valueOf(wrongTimes));
        }else if(event.getException() instanceof UsernameNotFoundException){
            throw new UsernameNotFoundException(String.valueOf(wrongTimes));
        }
    }

}
