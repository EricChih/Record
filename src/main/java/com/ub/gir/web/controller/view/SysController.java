package com.ub.gir.web.controller.view;


import java.io.IOException;
import java.text.ParseException;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.ub.gir.web.dto.girConfig.GirConfigResDto;
import com.ub.gir.web.dto.girConfig.UpdatePwdReqDto;
import com.ub.gir.web.dto.girConfig.UpdateScheduleReqDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.ScheduledTasks;
import com.ub.gir.web.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@Slf4j
@RestController
@RequestMapping({"/sys", "/syslist"})
public class SysController {
    @Resource
    private ConfigService configService;
    @Resource
    private UserService userService;

    /**
     * 查詢 系統安全設定
     */
    @GetMapping({"", "/all"})
    public ModelAndView getAllSys(@AuthenticationPrincipal User user) throws ParseException, IOException {
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        GirConfigResDto resDto = configService.getSys();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.addObject("data", resDto);
        modelAndView.setViewName("sysconfig");

        return modelAndView;
    }

    /**
     * 更新 系統安全設定-密碼參數
     */
    @PostMapping("/updatePwd")
    public ResponseEntity<?> updatePwd(@Valid @RequestBody UpdatePwdReqDto dto, @AuthenticationPrincipal User user) {
        configService.updateSysPwd(dto, user.getUsername());

        return ResponseEntity.ok().build();
    }

    /**
     * 更新 系統安全設定-系統操作軌跡
     */
    @PostMapping("/updateLogDay")
    public ResponseEntity<?> updateLogKeepDays(@RequestParam String logKeepDay, @AuthenticationPrincipal User user) {
        configService.updateLogKeepDays(logKeepDay, user.getUsername());

        return ResponseEntity.ok().build();
    }

    /**
     * 更新 系統安全設定-排程參數
     */
    @PostMapping("/updateSchedule")
    public ResponseEntity<?> updateSchedule(@Valid @RequestBody UpdateScheduleReqDto dto, @AuthenticationPrincipal User user) {
        configService.updateSchedule(dto, user.getUsername());

        return ResponseEntity.ok().build();
    }
}