package com.ub.gir.web.controller.view;


import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.RecYearService;
import com.ub.gir.web.service.UserService;
import com.ub.gir.web.util.HtmlEscapeUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/recyear", "/recyearlist"})
public class RecYearController {

    @Autowired
    private RecYearService recyearService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @GetMapping({"", "/all"})
    public ModelAndView getAllDep(@AuthenticationPrincipal User user, HttpServletRequest request) {
        String userrole = userService.getUserRoleByUsername(user.getUsername());
        String depid = userService.getDepIDByUsername(user.getUsername());
        TreeMap sitemap = configService.getAllSiteMap();
        TreeMap depmap = recyearService.getAllOnOffDepMap();

        //處理mgr role 看自己代管部門
        if (userrole.equalsIgnoreCase("manager")) {
            String groupteam = recyearService.getGroupDepIDList(depid);
            depmap = recyearService.getAllMgrDepMap(depid, groupteam);
        }
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("recyearlist");

        return modelAndView;
    }

    //抓單一部門資料by depid
    @GetMapping("/get/{id}")
    public ModelAndView getByDepId(@PathVariable("id") String depid, @AuthenticationPrincipal User user) {
        RecYearService updatedata = recyearService.getDepById(depid);

        //撈site list
        TreeMap sitemap = configService.getAllSiteMap();
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("updatedep", updatedata);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);

        modelAndView.setViewName("recyearupdate");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //更新部門資料
    @PostMapping("/update")
    public ModelAndView updateDep(@ModelAttribute RecYearService updatedep, Model model,
                                  @AuthenticationPrincipal User user) {
        recyearService.updateDepByDepID(updatedep, user.getUsername());
        List<RecYearService> alldep = recyearService.getAllDep();

        //撈site list
        TreeMap sitemap = configService.getAllSiteMap();
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        TreeMap depmap = recyearService.getAllOnOffDepMap();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("deps", alldep);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("depmap", depmap);

        modelAndView.setViewName("recyearlist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //停用部門Request
    @PostMapping("/stop")
    public ModelAndView stopDep(@RequestParam(name = "delId") String delDepId, @AuthenticationPrincipal User user) {
        //執行DB Dep data stop
        recyearService.stopDepByDepId(delDepId, user.getUsername());

        //Stop後,重新拉 deplist 資料列表
        List<RecYearService> alldep = recyearService.getAllDep();
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("deps", alldep);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("recyearlist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //查詢部門Request
    @PostMapping("/search")
    public ModelAndView searchDep(@RequestParam(name = "sDepID") String sDepID,
                                  @RequestParam(name = "sDepName") String sDepName,
                                  @AuthenticationPrincipal User user) {
        //先抓urole
        String urole = userService.getUserRoleByUsername(user.getUsername());
        //撈取User depid
        String depid = userService.getDepIDByUsername(user.getUsername());
        //搜尋結果資料
        List<RecYearService> searchlist = new ArrayList<RecYearService>();
        sDepID = HtmlEscapeUtils.escapeString(sDepID);
        sDepName = HtmlEscapeUtils.escapeString(sDepName);
        if (urole.equalsIgnoreCase("manager")) {
            //抓代管部門的資料
            String GroupTeam = recyearService.getGroupDepIDList(depid);
            searchlist = recyearService.searchMgrDep(sDepID.toUpperCase(), sDepName, user.getUsername(), GroupTeam);
        } else {
            searchlist = recyearService.searchDep(sDepID.toUpperCase(), sDepName, user.getUsername());
        }
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        //撈site map
        TreeMap sitemap = configService.getAllSiteMap();
        //撈dep map
        TreeMap depmap = recyearService.getAllOnOffDepMap();

        //處理mgr role 看自己代管部門
        if (urole.equalsIgnoreCase("manager")) {
            String groupteam = recyearService.getGroupDepIDList(depid);
            depmap = recyearService.getAllMgrDepMap(depid, groupteam);
        }
        ModelAndView modelAndView = new ModelAndView("recyearlist");
        modelAndView.getModelMap().addAttribute("deps", searchlist);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("sDepID", sDepID);
        modelAndView.getModelMap().addAttribute("sDepName", sDepName);

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

}
