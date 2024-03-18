package com.ub.gir.web.configuration.interceptor;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
@WebListener
public class UBHttpSessionListener implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // 会话销毁时的操作，即会话超时时执行的动作
        System.out.println("Session Timeout - Do something here!");
        // 在这里执行后续动作
    }
}
