package com.ub.gir.web.controller.view;


import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.log.SearchLogReqDto;
import com.ub.gir.web.dto.log.SearchLogResDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.LogService;
import com.ub.gir.web.service.UserService;
import com.ub.gir.web.util.HtmlEscapeUtils;
import com.ub.gir.web.util.JsonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/log", "/loglist"})
public class LogController {

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @GetMapping({"", "/all"})
    public ModelAndView getAllLog(@AuthenticationPrincipal User user, HttpServletRequest request) {
        List<LogDto> alllog = new ArrayList();

        //撈取module list for each role
        TreeMap modulemap = configService.getAllModuleMap();
        TreeMap admModulemap = configService.getModuleMapByRole("admin");
        TreeMap mgrModulemap = configService.getModuleMapByRole("manager");
        TreeMap supModulemap = configService.getModuleMapByRole("supervisor");
        //撈取act list for each role
        TreeMap actmap = configService.getAllActMap();
        TreeMap admActmap = configService.getActMapByRole("admin");
        TreeMap mgrActmap = configService.getActMapByRole("manager");
        TreeMap supActmap = configService.getActMapByRole("supervisor");
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("logs", alllog);
        modelAndView.getModelMap().addAttribute("modulemap", modulemap);
        modelAndView.getModelMap().addAttribute("admModulemap", admModulemap);
        modelAndView.getModelMap().addAttribute("mgrModulemap", mgrModulemap);
        modelAndView.getModelMap().addAttribute("supModulemap", supModulemap);
        modelAndView.getModelMap().addAttribute("actmap", actmap);
        modelAndView.getModelMap().addAttribute("admActmap", admActmap);
        modelAndView.getModelMap().addAttribute("mgrActmap", mgrActmap);
        modelAndView.getModelMap().addAttribute("supActmap", supActmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("loglist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    @PostMapping("/search")
    public ModelAndView searchLog(@ModelAttribute SearchLogReqDto logDto,
                                  @AuthenticationPrincipal User user) throws IllegalAccessException {
        SearchLogReqDto responseDto = JsonUtil.convertObjectToClass(logDto, SearchLogReqDto.class);
        SearchLogReqDto encodeDto = new SearchLogReqDto();
        encodeDto.setActionUser(HtmlEscapeUtils.escapeString(responseDto.getActionUser()));
        encodeDto.setFunctionName(HtmlEscapeUtils.escapeString(responseDto.getFunctionName()));
        encodeDto.setActionType(HtmlEscapeUtils.escapeString(responseDto.getActionType()));
        encodeDto.setStarttime(HtmlEscapeUtils.escapeString(responseDto.getStarttime()));
        encodeDto.setEndtime(HtmlEscapeUtils.escapeString(responseDto.getEndtime()));

        List<SearchLogResDto> searchlist = logService.searchLog(encodeDto, user.getUsername());

        //撈取module list
        TreeMap modulemap = configService.getAllModuleMap();
        TreeMap admModulemap = configService.getModuleMapByRole("admin");
        TreeMap mgrModulemap = configService.getModuleMapByRole("manager");
        TreeMap supModulemap = configService.getModuleMapByRole("supervisor");
        //撈取act list
        TreeMap actmap = configService.getAllActMap();
        TreeMap admActmap = configService.getActMapByRole("admin");
        TreeMap mgrActmap = configService.getActMapByRole("manager");
        TreeMap supActmap = configService.getActMapByRole("supervisor");
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("logs", searchlist);
        modelAndView.getModelMap().addAttribute("modulemap", modulemap);
        modelAndView.getModelMap().addAttribute("admModulemap", admModulemap);
        modelAndView.getModelMap().addAttribute("mgrModulemap", mgrModulemap);
        modelAndView.getModelMap().addAttribute("supModulemap", supModulemap);
        modelAndView.getModelMap().addAttribute("actmap", actmap);
        modelAndView.getModelMap().addAttribute("admActmap", admActmap);
        modelAndView.getModelMap().addAttribute("mgrActmap", mgrActmap);
        modelAndView.getModelMap().addAttribute("supActmap", supActmap);
        modelAndView.getModelMap().addAttribute("searchrec", encodeDto);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("loglist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------
}
