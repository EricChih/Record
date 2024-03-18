package com.ub.gir.web.controller.view;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.ub.gir.web.configuration.security.SecurityHelper;
import com.ub.gir.web.controller.AbstractController;
import com.ub.gir.web.dto.UserDTO;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.dto.user.UserSearchForm;
import com.ub.gir.web.dto.user.UserUpdateDto;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.DepService;
import com.ub.gir.web.service.LogService;
import com.ub.gir.web.service.UserService;
import com.ub.gir.web.util.HtmlEscapeUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@Slf4j
@RestController
@RequestMapping({"/user", "/userlist"})
public class UserController extends AbstractController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DepService depService;

    @Autowired
    private LogService logService;

    public UserController() {
    }

    @GetMapping("/extendSession")
    public ResponseEntity<?> extendSession(HttpServletRequest request){
        return ResponseEntity.ok("success");
    }

    //抓單一User 資料by username
    @GetMapping("/get/{username}")
    public ResponseEntity<?> getByDepId(@PathVariable("username") String username) {
        UserDetails userDetails = (UserDetails) SecurityHelper.getPrincipal();
        UserDto result = userService.getUserByUsername(username);
        if (userDetails.getAuthorities().toString().contains("manager")) {
            TreeMap<String, String> roleMap = new TreeMap<>();
            roleMap.put("supervisor", "Supervisor");
            result.setRolemap(roleMap);
        } else {
            result.setRolemap(configService.getAllRoleMap());
        }

        return ResponseEntity.ok(result);
    }//--------------------------------------------------------------------------------------------------

    //get all user data by doGet() urlpath= /user/all with application properties maxsize
    @GetMapping("/all")
    public ModelAndView getAllUser(@AuthenticationPrincipal User user, HttpServletRequest request, HttpServletResponse resp) throws IOException {

        // update csrf token data 席次確認
        /* logService.updateToken("add", request); //席次確認有做重複insert的防呆 */
        //撈取User role and siteid
        String userrole = userService.getUserRoleByUsername(user.getUsername());
        String usersiteid = userService.getUserSiteIDByUsername(user.getUsername());

        if (userrole.equalsIgnoreCase("supervisor")) {
            return new ModelAndView("redirect:" + "/rec/all");

        } else {
            return handleAllListViewSetting(usersiteid, userrole, user.getUsername());
        }

    }//---------------------------------------------------------------------------------------

    //搜尋User資料
    @PostMapping("/search")
    public ModelAndView searchUser(@ModelAttribute UserSearchForm userSearchForm,
                                   @AuthenticationPrincipal User user) throws IllegalAccessException {
        String encodeUsername = HtmlEscapeUtils.escapeString(user.getUsername());
        String loginUserRole = userService.getUserRoleByUsername(encodeUsername);
        String loginUserDepId = userService.getDepIDByUsername(encodeUsername);
        UserSearchForm encodeForm = new UserSearchForm();
        encodeForm.setName(HtmlEscapeUtils.escapeString(userSearchForm.getName()));
        encodeForm.setFirstName(HtmlEscapeUtils.escapeString(userSearchForm.getFirstName()));
        encodeForm.setLastName(HtmlEscapeUtils.escapeString(userSearchForm.getLastName()));
        encodeForm.setAgentId(HtmlEscapeUtils.escapeString(userSearchForm.getAgentId()));
        encodeForm.setDepId(HtmlEscapeUtils.escapeString(userSearchForm.getDepId()));
        encodeForm.setRole(HtmlEscapeUtils.escapeString(userSearchForm.getRole()));
        encodeForm.setExt(HtmlEscapeUtils.escapeString(userSearchForm.getExt()));
        encodeForm.setStatus(HtmlEscapeUtils.escapeString(userSearchForm.getStatus()));
        List<UserDto> searchlist = userService
                .searchUsers(encodeForm, encodeUsername, loginUserRole, loginUserDepId);

        TreeMap<String, String> sitemap = getSiteMap(loginUserDepId);

        //撈role list
        TreeMap rolemap = configService.getAllRoleMap();
        //撈dep list
        //撈 All dep list
        TreeMap depmap = getDepMap(loginUserRole, encodeUsername, loginUserDepId);
        UserDto loginUser = userService.getUserByUsername(encodeUsername);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("users", searchlist);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("rolemap", rolemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        // 用於判斷選單的值，讓option選單可以預設當初的選擇
        modelAndView.getModelMap().addAttribute("searchForm", encodeForm);

        modelAndView.setViewName("userlist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //新增 User Request
    @PostMapping("/add")
    public void addUser(@Valid @RequestBody UserDTO userdata, @AuthenticationPrincipal User user) {
        userService.addUser(userdata, user.getUsername());
    }//--------------------------------------------------------------------------------------------------

    //刪除 User Request, 資料庫其實是做停用
    @DeleteMapping()
    public void delUser(@RequestParam() String delUsername, @AuthenticationPrincipal User user) {
        userService.delUserByName(delUsername, user.getUsername());
    }

    //刪除 User Request, 資料庫其實是做停用
    @PostMapping("/reset")
    public boolean resetUser(@RequestParam(name = "resetUsername") String resetUsername,
                                  @AuthenticationPrincipal User user) {
        //執行DB User 密碼重設; 帳號與密碼一樣,status=3 密碼已加密但要馬上密碼變更
        boolean resetsucess = userService.resetUserPwdByName(resetUsername, user.getUsername());
        //撈取User siteid and userrole
        String usersiteid = userService.getUserSiteIDByUsername(user.getUsername());
        String userrole = userService.getUserRoleByUsername(user.getUsername());

        return resetsucess;
    }//--------------------------------------------------------------------------------------------------

    /**
     * 更新 User
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateDto dto, @AuthenticationPrincipal User user) {
        userService.updateUserByUsername(dto, user.getUsername());

        return ResponseEntity.ok().build();
    }

    // 檢查使用者在該地區是否存在，locations如果多個則以逗號分隔
    @GetMapping("/checkuserexist")
    public boolean isUserExist(@RequestParam String username) {
        return userService.checkUserNameExist(username);
    }

    // 匯出人員Excel操作軌跡
    @GetMapping("/userexcellog")
    @ResponseBody
    public void excelLog(@AuthenticationPrincipal User user) {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("usermgr");
        logDto.setActionType("excel");
        logDto.setInfo("匯出人員Excel資料表 ");
        logService.addLog(logDto, user.getUsername());
    }

    //共用function: 設定重新列表回 userlist page 的 obj setting
    private ModelAndView handleAllListViewSetting(String usersiteid, String userrole, String username) {
        List<UserDto> alluser = new ArrayList();
        String depid = userService.getDepIDByUsername(username);
        if (userrole.equalsIgnoreCase("superadmin") || userrole.equalsIgnoreCase("admin")) { //若為supadmin + admin  全撈
            alluser = userService.getAllWithRole(); //defalut user listing
        }

        TreeMap<String, String> sitemap = getSiteMap(depid);
        TreeMap rolemap = configService.getAllRoleMap();
        TreeMap depmap = getDepMap(userrole, username, depid);
        UserDto loginUser = userService.getUserByUsername(username);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("users", alluser);
        modelAndView.getModelMap().addAttribute("sitemap", sitemap);
        modelAndView.getModelMap().addAttribute("rolemap", rolemap);
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("userSearchForm", new UserSearchForm());
        modelAndView.setViewName("userlist");

        return modelAndView;
    }

    private TreeMap<String, String> getSiteMap(String depId) {
        TreeMap<String, String> sitemap = configService.getAllSiteMap();
        if (depId.equalsIgnoreCase("MP")) { //如果是行企只有台北;此寫法暫解不算好,移到前端view處理會更好
            sitemap.remove("007");
        }
        return sitemap;
    }

    private TreeMap<String, String> getDepMap(String userRole, String username, String depId) {
        TreeMap<String, String> depMap;
        if (userRole.equalsIgnoreCase("manager")) {
            depMap = depService.getDepListByUsername(username, depId);
        } else {
            depMap = depService.getAllDepMap();
        }
        return depMap;
    }
}