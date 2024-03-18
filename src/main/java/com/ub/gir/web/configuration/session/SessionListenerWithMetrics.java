/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.session;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.*;


import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.ast.NullLiteral;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Slf4j
public class SessionListenerWithMetrics implements HttpSessionListener, HttpSessionAttributeListener {

    private final AtomicInteger activeSessions;

    private static final String SESSION_KEY = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    /**
     * <p>Title : Constructor</p>
     * <p>Description : </p>
     */
    public SessionListenerWithMetrics() {
        super();
        activeSessions = new AtomicInteger();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /* (non-Javadoc)
     * <p>Title : sessionCreated</p>
     * <p>Description : </p>
     * @param event
     * @see javax.servlet.http.UBHttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionCreated(final HttpSessionEvent event) {

        activeSessions.incrementAndGet();

        updateSessionCounter(event);

        HttpSession session = event.getSession();

        // in seconds
//        session.setMaxInactiveInterval ( 10 ) ;

    }

    /* (non-Javadoc)
     * <p>Title : sessionDestroyed</p>
     * <p>Description : </p>
     * @param event
     * @see javax.servlet.http.UBHttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {

        activeSessions.decrementAndGet();

        updateSessionCounter(event);

        log.info("Session has been destroyed ! Session ID : {}", event.getSession().getId());

        HttpServletResponse response = (HttpServletResponse) event.getSession().getAttribute("javax.servlet.http.HttpServletResponse");

        if (response != null) {
            try {
                // 跳转到 login.html 或者你想要的登录页面
                response.sendRedirect("login.html");
            } catch (Exception e) {
                log.error("Error redirecting to login.html", e);
            }
        } else {
            log.warn("HttpServletResponse is null. Unable to redirect.");
        }

//		throw new ResponseContentException ( HttpStatus.UNAUTHORIZED, ResultCode.RC_902, new ConnectionTimeoutException ( "Session expired !" ) ) ;

    }


    /* (non-Javadoc)
     * <p>Title : attributeAdded</p>
     * <p>Description : </p>
     * @param event
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeAdded(javax.servlet.http.HttpSessionBindingEvent)
     */
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {

        if (SESSION_KEY.equals(event.getName())) {

        }

        log.info("Session is added. Session ID : {}", event.getSession().getId());
    }

    /* (non-Javadoc)
     * <p>Title : attributeReplaced</p>
     * <p>Description : </p>
     * @param event
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeReplaced(javax.servlet.http.HttpSessionBindingEvent)
     */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {

        if (SESSION_KEY.equals(event.getName())) {

        }

        log.info("Session is replaced. Session ID : {}", event.getSession().getId());
        log.info("Session event name are : {} ", event.getName());
        log.info("Session event value are : {} ", event.getValue().getClass());

    }

    /**
     * @return int
     */
    private int getTotalActiveSession() {
        return activeSessions.get();
    }

    private void updateSessionCounter(HttpSessionEvent httpSessionEvent) {

        // set in the context
        httpSessionEvent.getSession().getServletContext().setAttribute("activeSession", activeSessions.get());

        log.info("Total active session are : {} ", activeSessions.get());

    }

}
