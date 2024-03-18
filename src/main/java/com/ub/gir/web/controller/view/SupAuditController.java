package com.ub.gir.web.controller.view;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.google.gson.Gson;
import com.ub.gir.web.dto.supAudit.SupAuditDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.exception.ErrorMessages;
import com.ub.gir.web.repository.db1.master.SupAuditListDB1MasterRepository;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.DepService;
import com.ub.gir.web.service.SupAuditService;
import com.ub.gir.web.service.UserService;
import com.ub.gir.web.util.HtmlEscapeUtils;

import javassist.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import static com.ub.gir.web.util.HtmlEscapeUtils.*;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/supaudit", "/supauditlist"})
public class SupAuditController {
    @Autowired
    SupAuditListDB1MasterRepository SupAuditListDB1MasterRepository;
    @Autowired
    private DepService depService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private UserService userService;
    @Autowired
    private SupAuditService supAuditService;

    //default 預設, 只回傳部門列表給View
    @GetMapping({"", "/all"})
    public ModelAndView getAllLog(@AuthenticationPrincipal User user, HttpServletRequest request) {
        TreeMap<String, String> depmap = getDepMap(user.getUsername());
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("supauditlist");

        return modelAndView;
    }

    //抓單部門ID 的 supervisor list
    @GetMapping("/getagent/{depid}")
    public ModelAndView getAgentList(@PathVariable("depid") String depid,
                                     @AuthenticationPrincipal User user) {
        List<UserDto> agentlist = userService.getSupByDepID(depid);
        TreeMap<String, String> depmap = getDepMap(user.getUsername());
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        String currDepName = depmap.get(depid);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("depmap", depmap);
        modelAndView.getModelMap().addAttribute("currDepName", currDepName);
        modelAndView.getModelMap().addAttribute("agentlist", agentlist);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("supauditlist");

        return modelAndView;
    }

    //抓given supervisor 的 audit AgentIDList + ExtList
    @GetMapping("/getaudit/{userid}")
    public ModelAndView getAuditList(@PathVariable("userid") String userid,
                                     @AuthenticationPrincipal User user) throws IllegalAccessException {
        String depid = userService.getDepIDByID(userid);

        return handleReturnAttr(user, depid, userService.getUserByID(userid));

    }//--------------------------------------------------------------------------------------------------

    //抓given supervisor 的 單一 AgentID狀態
    @GetMapping("/getaudit/{agentId}/{username}")
    public String getAuditList(@PathVariable("agentId") String agentId,
                               @PathVariable("username") String username) {
        SupAuditService updatedata = supAuditService.getAgentstatus(agentId, username);
        Gson gson = new Gson();

        return gson.toJson(updatedata);
    }

    //抓given supervisor 的 單一 AgentID狀態
    @GetMapping("/getauditext/{ext}/{username}")
    public String getextAuditList(@PathVariable("ext") String ext,
                                  @PathVariable("username") String username) {
        SupAuditService updatedata = supAuditService.getExtstatus(ext, username);
        Gson gson = new Gson();

        return gson.toJson(updatedata);
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

    private TreeMap<String, String> getSiteMap(String depId) {
        TreeMap<String, String> sitemap = configService.getAllSiteMap();
        if (depId.equalsIgnoreCase("MP")) { //如果是行企只有台北;此寫法暫解不算好,移到前端view處理會更好
            sitemap.remove("007");
        }
        return sitemap;
    }

    /**
     * 新增 可調聽AgentID
     */
    @PostMapping("/addagentid")
    public ModelAndView addAgentID(@Valid @RequestBody SupAuditDto addAuditData,
                                   @AuthenticationPrincipal User user) throws NotFoundException, IllegalAccessException {
        if (StringUtils.isEmpty(addAuditData.getAbleAgentID()))
            throw new NotFoundException("agentId 不可為空");

        String monitoringUserId = addAuditData.getMonitoringUserId();
        String depId = userService.getDepIDByID(monitoringUserId);

        boolean isExistsByDepIdAndAgentID = supAuditService.isExistsAgentIdByDepIdAndAgentID(depId, addAuditData.getAbleAgentID());
        if (!isExistsByDepIdAndAgentID)
            throw new NotFoundException("可調聽部門查無此 agentId");

        supAuditService.addAgentIDIn(user.getUsername(), monitoringUserId, addAuditData);

        return handleReturnAttr(user, depId, userService.getUserByID(monitoringUserId));
    }

    /**
     * 新增 可調聽AgentID
     */
    @PostMapping("/updateagentid")
    public ModelAndView updateAgentID(@Valid @RequestBody SupAuditDto addAuditData,
                                      @AuthenticationPrincipal User user) throws NotFoundException, IllegalAccessException {
        String monitoringUserId = addAuditData.getMonitoringUserId();
        String depId = userService.getDepIDByID(monitoringUserId);

        boolean isExistsByDepIdAndAgentID = supAuditService.isExistsAgentIdByDepIdAndAgentID(depId, addAuditData.getAbleAgentID());
        if (!isExistsByDepIdAndAgentID)
            throw new NotFoundException("可調聽部門查無此 agentId");

        supAuditService.addAgentIDIn(user.getUsername(), monitoringUserId, addAuditData);

        return handleReturnAttr(user, depId, userService.getUserByID(monitoringUserId));
    }

    //更新AgentID屬性
    @PostMapping("/updateAgentStatus")
    public void updateAgentId(@Valid @RequestBody SupAuditDto dto,
                              @AuthenticationPrincipal User user) throws ParseException {
        supAuditService.updateAgentstatus(dto, user.getUsername());
    }

    //更新Ext屬性
    @PostMapping("/updateExtStatus")
    public void updateExt(@Valid @RequestBody SupAuditDto dto,
                          @AuthenticationPrincipal User user) throws ParseException {
        supAuditService.updateExtstatus(dto, user.getUsername());
    }

    /**
     * 新增 可調聽Ext
     */
    @PostMapping("/addextnum")
    public ModelAndView addExtNum(@Valid @RequestBody SupAuditDto addAuditData,
                                  @AuthenticationPrincipal User user) throws NotFoundException, IllegalAccessException {
        if (StringUtils.isEmpty(addAuditData.getAbleExt()))
            throw new NotFoundException("分機號碼 不可為空");

        String monitoringUserId = addAuditData.getMonitoringUserId();
        String depId = userService.getDepIDByID(monitoringUserId);

        boolean isExistsExtByDepIdAndAgentID = supAuditService.isExistsExtByDepIdAndExt(depId, addAuditData.getAbleExt());
        if (!isExistsExtByDepIdAndAgentID)
            throw new NotFoundException("可調聽部門查無此 分機號碼");

        supAuditService.addExtNumIn(user.getUsername(), monitoringUserId, addAuditData);

        return handleReturnAttr(user, depId, userService.getUserByID(monitoringUserId));
    }

    /**
     * 檢查是否有同步 可調聽AgentID
     */
    @PostMapping("/checkSyncAgentId")
    public ResponseEntity<?> checkSyncAuditAgentID(@RequestParam(name = "syncDepID") String depId) {
        boolean isExistAgentIdData = supAuditService.checkSyncAgentId(depId);
        if (!isExistAgentIdData)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorMessages.RESOURCE_NOT_FOUND.toObject());

        return ResponseEntity.ok().build();
    }

    /**
     * 同步 可調聽AgentID
     */
    @PostMapping("/syncagentid")
    public ModelAndView syncAuditAgentID(@RequestParam(name = "syncDepID") String depId,
                                         @RequestParam(name = "syncUserName") String userName,
                                         @RequestParam(name = "hiddenDLSyncId") String setIsDownload,
                                         @RequestParam(name = "hiddenRecSyncId") String setForeverRec,
                                         @AuthenticationPrincipal User user) throws IllegalAccessException {
        String userid = userService.getIDByLoginName(userName);
        supAuditService.syncAgentId(depId, userName, setIsDownload, setForeverRec);
        return handleReturnAttr(user, depId, userService.getUserByID(userid));
    }

    /**
     * 檢查是否有同步 可調聽Ext
     */
    @PostMapping("/checkSyncExt")
    public ResponseEntity<?> checkSyncAuditExt(@RequestParam(name = "syncDepID") String depId) {
        boolean isExistExtData = supAuditService.checkSyncExt(depId);
        if (!isExistExtData)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorMessages.RESOURCE_NOT_FOUND.toObject());

        return ResponseEntity.ok().build();
    }

    /**
     * 同步 可調聽Ext
     */
    @PostMapping("/syncext")
    public ModelAndView syncAuditExt(@RequestParam(name = "syncDepID") String depID,
                                     @RequestParam(name = "syncUserName") String userName,
                                     @RequestParam(name = "hiddenDLSyncExt") String setIsDownload,
                                     @RequestParam(name = "hiddenRecSyncExt") String setForeverRec,
                                     @AuthenticationPrincipal User user) throws IllegalAccessException {
        String userid = userService.getIDByLoginName(userName);
        supAuditService.syncExt(depID, userName, setIsDownload, setForeverRec);

        return handleReturnAttr(user, depID, userService.getUserByID(userid));
    }

    /**
     * 透過 deleteType 刪除可調聽AgentID或可調聽Ext
     */
    @DeleteMapping
    public ModelAndView delAuditListByType(@RequestParam() String delList, @RequestParam() String deleteType, @AuthenticationPrincipal User user) throws IllegalAccessException {
        String userName;
        if (StringUtils.equals("delAgentIds", deleteType)) {
            userName = supAuditService.deleteAgentID(delList, user);
        } else {
            userName = supAuditService.deleteExt(delList, user);
        }
        String userId = userService.getIDByLoginName(userName);
        String depId = userService.getDepIDByID(userId);

        return handleReturnAttr(user, depId, userService.getUserByID(userId));
    }

    //設定下載權限
    @PostMapping("/setDLAgent")
    public ModelAndView setDLByAgentID(@RequestParam(name = "lokDLList") String lokDLList,
                                       @RequestParam(name = "setDLList") String setDLList,
                                       @RequestParam(name = "lokrecList") String lokrcDLList,
                                       @RequestParam(name = "setrecList") String setrcDLList,
                                       @AuthenticationPrincipal User user) throws IllegalAccessException {

        String encodeLokDLList = HtmlEscapeUtils.escapeString(lokDLList);
        String encodeSetDLList = HtmlEscapeUtils.escapeString(setDLList);
        String encodeLokrcDLList = HtmlEscapeUtils.escapeString(lokrcDLList);
        String encodeSetrcDLList = HtmlEscapeUtils.escapeString(setrcDLList);
        String setList = encodeLokDLList + encodeSetDLList;
        String setrcList = encodeLokrcDLList + encodeSetrcDLList;
        boolean dllist = Strings.isEmpty(setList);
        boolean reclist = Strings.isEmpty(setrcList);

        if (dllist!=true && reclist!=true) {
            String setdlList = setList.substring(0, setList.length() - 1);
            String setrecList = setrcList.substring(0, setrcList.length() - 1);
            String[] bksetdlList = setdlList.split(",");
            String[] bksetrecList = setrecList.split(",");
            List<String> name = new ArrayList<>();
            List<String> agent = new ArrayList<>();
            List<String> status = new ArrayList<>();
            for (int j = 1; j < bksetdlList.length; j = j + 3) {
                name.add(bksetdlList[1]);
            }
            for (int i = 0; i < bksetdlList.length; i = i + 3) {
                agent.add(bksetdlList[i]);
            }
            for (int k = 2; k < bksetdlList.length; k = k + 3) {
                status.add(bksetdlList[k]);
            }

            String sname = name.get(0);
            List<String> recagent = new ArrayList<>();
            List<String> recstatus = new ArrayList<>();
            for (int i = 0; i < bksetrecList.length; i = i + 3) {
                recagent.add(bksetrecList[i]);
            }
            for (int k = 2; k < bksetrecList.length; k = k + 3) {
                recstatus.add(bksetrecList[k]);
            }

            String userid = userService.getIDByLoginName(sname);
            String depid = userService.getDepIDByID(userid);
            supAuditService.setacByAgentID(sname, agent, status, recagent, recstatus, user.getUsername());

            return handleReturnAttr(user, depid, userService.getUserByID(userid));
        }

        if (dllist!=true) {
            String setdlList = setList.substring(0, setList.length() - 1);
            String[] bksetdlList = setdlList.split(",");
            List<String> name = new ArrayList<>();
            List<String> agent = new ArrayList<>();
            List<String> status = new ArrayList<>();
            for (int j = 1; j < bksetdlList.length; j = j + 3) {
                name.add(bksetdlList[1]);
            }
            for (int i = 0; i < bksetdlList.length; i = i + 3) {
                agent.add(bksetdlList[i]);
            }
            for (int k = 2; k < bksetdlList.length; k = k + 3) {
                status.add(bksetdlList[k]);
            }

            String sname = name.get(0);
            String userid = userService.getIDByLoginName(sname);
            String depid = userService.getDepIDByID(userid);
            supAuditService.setDLByAgentID(sname, agent, status, user.getUsername());

            return handleReturnAttr(user, depid, userService.getUserByID(userid));
        }

        if (reclist!=true) {
            String setdlList = setrcList.substring(0, setrcList.length() - 1);
            String[] bksetdlList = setdlList.split(",");
            List<String> name = new ArrayList<>();
            List<String> agent = new ArrayList<>();
            List<String> status = new ArrayList<>();
            for (int j = 1; j < bksetdlList.length; j = j + 3) {
                name.add(bksetdlList[1]);
            }
            for (int i = 0; i < bksetdlList.length; i = i + 3) {
                agent.add(bksetdlList[i]);
            }
            for (int k = 2; k < bksetdlList.length; k = k + 3) {
                status.add(bksetdlList[k]);
            }

            String sname = name.get(0);
            String userid = userService.getIDByLoginName(sname);
            String depid = userService.getDepIDByID(userid);
            supAuditService.setrecByAgentID(sname, agent, status, user.getUsername());

            return handleReturnAttr(user, depid, userService.getUserByID(userid));
        }
        return null;
    }

    //設定下載權限by ext
    @PostMapping("/setDLExt")
    public ModelAndView setDLByExt(@RequestParam(name = "lokDLListext") String lokDLList,
                                   @RequestParam(name = "setDLListext") String setDLList,
                                   @RequestParam(name = "lokrecListext") String lokrcDLList,
                                   @RequestParam(name = "setrecListext") String setrcDLList,
                                   @AuthenticationPrincipal User user) throws IllegalAccessException {
        String encodeLokDLList = HtmlEscapeUtils.escapeString(lokDLList);
        String encodeSetDLList = HtmlEscapeUtils.escapeString(setDLList);
        String encodeLokrcDLList = HtmlEscapeUtils.escapeString(lokrcDLList);
        String encodeSetrcDLList = HtmlEscapeUtils.escapeString(setrcDLList);
        String setList = encodeLokDLList + encodeSetDLList;
        String setrcList = encodeLokrcDLList + encodeSetrcDLList;
        boolean dllist = Strings.isEmpty(setList);
        boolean reclist = Strings.isEmpty(setrcList);

        if (dllist!=true && reclist!=true) {
            String setdlList = setList.substring(0, setList.length() - 1);
            String setrecList = setrcList.substring(0, setrcList.length() - 1);
            String[] bksetdlList = setdlList.split(",");
            String[] bksetrecList = setrecList.split(",");
            List<String> name = new ArrayList<>();
            List<String> ext = new ArrayList<>();
            List<String> status = new ArrayList<>();
            for (int j = 1; j < bksetdlList.length; j = j + 3) {
                name.add(bksetdlList[1]);
            }
            for (int i = 0; i < bksetdlList.length; i = i + 3) {
                ext.add(bksetdlList[i]);
            }
            for (int k = 2; k < bksetdlList.length; k = k + 3) {
                status.add(bksetdlList[k]);
            }

            String sname = name.get(0);
            List<String> recext = new ArrayList<>();
            List<String> recstatus = new ArrayList<>();
            for (int i = 0; i < bksetrecList.length; i = i + 3) {
                recext.add(bksetrecList[i]);
            }
            for (int k = 2; k < bksetrecList.length; k = k + 3) {
                recstatus.add(bksetrecList[k]);
            }

            String userid = userService.getIDByLoginName(sname);
            String depid = userService.getDepIDByID(userid);
            supAuditService.setacByExt(sname, ext, status, recext, recstatus, user.getUsername());

            return handleReturnAttr(user, depid, userService.getUserByID(userid));
        }

        if (dllist!=true) {
            String setdlList = setList.substring(0, setList.length() - 1);
            String[] bksetdlList = setdlList.split(",");
            List<String> name = new ArrayList<>();
            List<String> ext = new ArrayList<>();
            List<String> status = new ArrayList<>();
            for (int j = 1; j < bksetdlList.length; j = j + 3) {
                name.add(bksetdlList[1]);
            }
            for (int i = 0; i < bksetdlList.length; i = i + 3) {
                ext.add(bksetdlList[i]);
            }
            for (int k = 2; k < bksetdlList.length; k = k + 3) {
                status.add(bksetdlList[k]);
            }

            String sname = name.get(0);
            String userid = userService.getIDByLoginName(sname);
            String depid = userService.getDepIDByID(userid);
            supAuditService.setDLByExt(sname, ext, status, user.getUsername());

            return handleReturnAttr(user, depid, userService.getUserByID(userid));
        }

        if (reclist!=true) {
            String setdlList = setrcList.substring(0, setrcList.length() - 1);
            String[] bksetdlList = setdlList.split(",");
            List<String> name = new ArrayList<>();
            List<String> ext = new ArrayList<>();
            List<String> status = new ArrayList<>();
            for (int j = 1; j < bksetdlList.length; j = j + 3) {
                name.add(bksetdlList[1]);
            }
            for (int i = 0; i < bksetdlList.length; i = i + 3) {
                ext.add(bksetdlList[i]);
            }
            for (int k = 2; k < bksetdlList.length; k = k + 3) {
                status.add(bksetdlList[k]);
            }

            String sname = name.get(0);
            String userid = userService.getIDByLoginName(sname);
            String depid = userService.getDepIDByID(userid);
            supAuditService.setrecByExt(sname, ext, status, user.getUsername());

            return handleReturnAttr(user, depid, userService.getUserByID(userid));
        }
        return null;
    }

    //共用function: 設定回傳參數給 View page: supauditlist
    private ModelAndView handleReturnAttr(User user, String depid, UserDto userDto) throws IllegalAccessException {
        List<SupAuditService> agentAuditlist = supAuditService.getAgentAuditlist(userDto.getName());
        List<SupAuditService> extAuditlist = supAuditService.getExtAuditlist(userDto.getName());

        List<UserDto> agentlist = userService.getSupByDepID(depid);
        String mgrUsername = user.getUsername();
        UserDto loginUser = userService.getUserByUsername(mgrUsername);
        TreeMap<String, String> depMap = getDepMap(mgrUsername);
        String currDepName = depMap.get(depid);

        ModelAndView modelAndView = new ModelAndView("supauditlist");
        modelAndView.getModelMap().addAttribute("depmap", escapeTreeMap(depMap));   //回傳部門列表
        modelAndView.getModelMap().addAttribute("currdepid", escapeString(depid)); //回傳目前被選定的部門
        modelAndView.getModelMap().addAttribute("currDepName", escapeString(currDepName)); //回傳目前被選定的部門名稱
        modelAndView.getModelMap().addAttribute("currsup", escapeString(userDto.getLastName())
                + escapeString(userDto.getFirstName())); //回傳目前被選定的Sup名字
        modelAndView.getModelMap().addAttribute("currsupid", userDto.getID()); //回傳目前被選定的Sup的DB ID
        modelAndView.getModelMap().addAttribute("currsupname", escapeString(userDto.getName())); //回傳目前被選定的Sup的DB 登入帳號Name
        modelAndView.getModelMap().addAttribute("agentAuditList", escapeList(agentAuditlist)); //回傳目前被選定的Sup的可調聽AgentIDList
        modelAndView.getModelMap().addAttribute("extAuditList", escapeList(extAuditlist)); //回傳目前被選定的Sup的可調聽Ext List
        modelAndView.getModelMap().addAttribute("loginuser", escapeObject(loginUser));
        modelAndView.getModelMap().addAttribute("agentlist", escapeList(agentlist)); // 回傳目前選定部門的supervisor清單

        return modelAndView;
    }

    private TreeMap<String, String> getDepMap(String username) {
        String userRole = userService.getUserRoleByUsername(username);
        String depId = userService.getDepIDByUsername(username);
        //處理mgr role 看自己代管部門
        if (userRole.equalsIgnoreCase("manager")) {
            String groupTeam = depService.getDepGroupTeam(depId);
            return depService.getAllMgrDepMap(depId, groupTeam);
        } else {
            return depService.getAllOnOffDepMap();
        }
    }
}
