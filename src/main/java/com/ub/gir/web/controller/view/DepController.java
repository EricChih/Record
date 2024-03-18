package com.ub.gir.web.controller.view;


import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.ub.gir.web.dto.dep.DepDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.DepService;
import com.ub.gir.web.service.UserService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/dep", "/deplist"})
public class DepController {

    @Autowired
    private DepService depService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @GetMapping({"", "/all"})
    public ModelAndView getAllDep(@AuthenticationPrincipal User user) {
        //撈取User role
        String userrole = userService.getUserRoleByUsername(user.getUsername());
        //撈取User depid
        String depid = userService.getDepIDByUsername(user.getUsername());

        //撈site list
        TreeMap<String, String> sitemap = configService.getAllSiteMap();

        TreeMap<String, String> depmap;
        //處理mgr role 看自己代管部門
        if (userrole.equalsIgnoreCase("manager")) {
            String groupTeam = depService.getDepGroupTeam(depid);
            depmap = depService.getAllMgrDepMap(depid, groupTeam);
        } else {
            depmap = depService.getAllOnOffDepMap();
        }

        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("deplist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //新增部門Request
    @PostMapping("/add")
    public void addDep(@Valid @RequestBody DepDto dto, @AuthenticationPrincipal User user) {
        depService.addDep(dto, user.getUsername());
    }

    //抓單一部門資料by depid
    @GetMapping("/get/{id}")
    public String getByDepId(@PathVariable("id") String depid, @AuthenticationPrincipal User user) throws JsonProcessingException {
        //抓取需要被更新的Dep data
        DepService updatedata = depService.getDepById(depid);
        Gson gson = new Gson();
        return gson.toJson(updatedata);
    }//--------------------------------------------------------------------------------------------------

    //更新部門資料
    @PostMapping("/update")
    public ModelAndView updateDep(@ModelAttribute("upform") DepService updatedep, Model model,
                                  @AuthenticationPrincipal User user) {
        String urole = userService.getUserRoleByUsername(user.getUsername());
        String depId = userService.getDepIDByUsername(user.getUsername());
        String groupTeam = depService.getDepGroupTeam(depId);
        depService.updateDepByDepID(updatedep, user.getUsername());
        //更新後,重新拉 deplist 資料列表
        List<DepService> searchlist = depService.getDepListAfterUpdate(groupTeam, urole);

        //撈site list
        TreeMap<String, String> sitemap = configService.getAllSiteMap();
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        TreeMap<String, String> depmap;
        //處理mgr role 看自己代管部門
        if (urole.equalsIgnoreCase("manager")) {
            depmap = depService.getAllMgrDepMap(depId, groupTeam);
        } else {
            depmap = depService.getAllOnOffDepMap();
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("deps", searchlist);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);

        modelAndView.setViewName("deplist");
        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //停用部門Request
    @PostMapping("/stop")
    public ModelAndView stopDep(@RequestParam(name = "delId") String delDepId, @AuthenticationPrincipal User user) {
        depService.stopDepByDepId(delDepId, user.getUsername());

        //Stop後,重新拉 deplist 資料列表
        List<DepService> alldep = depService.getAllDep();
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("deps", alldep);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("deplist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //刪除部門Request
    @DeleteMapping()
    public void delDep(@RequestParam String delId, @AuthenticationPrincipal User user) {
        if (StringUtils.isEmpty(delId))
            throw new IllegalArgumentException("delDepId 不可為空");

        depService.delDepByDepId(delId, user.getUsername());
    }


    //查詢部門Request
    @PostMapping("/search")
    public ModelAndView searchDep(@RequestParam String sDepID, @RequestParam String sDepName,
                                  @AuthenticationPrincipal User user) {
        sDepID = HtmlUtils.htmlEscape(sDepID);
        sDepName = HtmlUtils.htmlEscape(sDepName);

        String urole = userService.getUserRoleByUsername(user.getUsername());
        String depid = userService.getDepIDByUsername(user.getUsername());

        List<DepService> searchlist;
        if (urole.equalsIgnoreCase("manager")) {// mgr 只撈自己部門
            //抓是否有代管其他部門
            String groupTeam = depService.getDepGroupTeam(depid);

            searchlist = depService.searchMgrDep(sDepID.toUpperCase(), sDepName, groupTeam);
        } else {
            searchlist = depService.searchDep(sDepID.toUpperCase(), sDepName);
        }
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        TreeMap<String, String> sitemap = configService.getAllSiteMap();
        TreeMap<String, String> depmap = depService.getAllOnOffDepMap();

        if (urole.equalsIgnoreCase("manager")) {
            String groupTeam = depService.getDepGroupTeam(depid);
            depmap = depService.getAllMgrDepMap(depid, groupTeam);
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("deps", searchlist);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("sDepID", sDepID);
        modelAndView.getModelMap().addAttribute("sDepName", sDepName);
        modelAndView.setViewName("deplist");

        return modelAndView;
    }

    /**
     * 檢查是否有人員屬於該部門，有的話不能刪除
     * 檢查是否有部門正調聽要刪除的部門，有的話給予警告但可以刪除
     *
     * @param depId
     * @return
     */
    @GetMapping("del-check/{depId}")
    @ResponseBody
    public Map<String, Object> delCheck(@PathVariable String depId) {
        return depService.checkDelDep(depId);
    }

    // 確認該部門是否存在
    @GetMapping("/check-dep-exist")
    public boolean checkDepExist(@RequestParam String depId) {
        return depService.checkDepExist(depId);
    }
}
