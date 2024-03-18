/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.controller.view;


import com.ub.gir.web.configuration.security.SecurityHelper;
import com.ub.gir.web.controller.AbstractController;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/13
 */
@CrossOrigin(origins = "http://localhost:8082")
@Controller
public class IndexController extends AbstractController {

    @GetMapping(value = {"/", "/index", "/login", "/error"}, produces = MediaType.TEXT_HTML_VALUE)
    public String doLogin() {
        if (isLogin()) {
            return "redirect:/user/all";
        }
        return "login";
    }

    private boolean isLogin() {
        return SecurityHelper.getCurrentAuthentication()!=null
                && SecurityHelper.isAuthenticated()
                && !(SecurityHelper.getCurrentAuthentication() instanceof AnonymousAuthenticationToken);
    }

}
