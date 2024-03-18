package com.ub.gir.web.controller.view;


import javax.annotation.Resource;

import com.ub.gir.web.controller.AbstractController;
import com.ub.gir.web.dto.pwd.PwdResDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.PwdService;
import com.ub.gir.web.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@Slf4j
@RestController
@RequestMapping({"/pwd", "/pass"})
public class PwdController extends AbstractController {

    @Resource
    private UserService userService;

    @Resource
    private PwdService pwdService;


    //密碼變更預設display頁
    @GetMapping({"/user"})
    public ModelAndView chgPwdDefault(@AuthenticationPrincipal User user) throws Exception {
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        // 檢查密碼與帳號是否一樣，一樣代表是第一次登入或是重設完密碼第一次登入
        boolean isSidebarVisible = userService.checkSidebarVisibleByUserNameAndPwd(loginUser.getName());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("isSidebarVisible", isSidebarVisible);
        modelAndView.setViewName("pwdchange"); //密碼變更頁的View

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //執行密碼變更預設landing頁
    @PostMapping("/update")
    public ModelAndView updateUserPwd(@RequestParam String firstPwd, @RequestParam String secondPwd,
                                      @AuthenticationPrincipal User user) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        boolean isSidebarVisible = userService.checkSidebarVisibleByUserNameAndPwd(loginUser.getName());
        PwdResDto resDto = pwdService.validatePwdUpdate(user.getUsername(), firstPwd, secondPwd);
        if (!ObjectUtils.isEmpty(resDto.getErrMsg())) {
            modelAndView.getModelMap().addAttribute("loginuser", loginUser);
            modelAndView.getModelMap().addAttribute("isSidebarVisible", isSidebarVisible);
            modelAndView.getModelMap().addAttribute("errmsg", resDto.getErrMsg());
            modelAndView.setViewName(resDto.getViewName());

            return modelAndView;
        }
        userService.updateDb1PwdByUsername(user.getUsername(), secondPwd);
        modelAndView.setViewName("redirect:/perform_logout_pwdchange");

        return modelAndView;
    }
}