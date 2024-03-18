package com.ub.gir.web.service;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.supAudit.SupAuditDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.entity.db1.master.DepAgentIDDB1Master;
import com.ub.gir.web.entity.db1.master.DepExtDB1Master;
import com.ub.gir.web.entity.db1.master.SupAuditListDB1Master;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.*;
import com.ub.gir.web.util.HtmlEscapeUtils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ub.gir.web.util.RocDateUtil.convertStringDateToDate;
import static java.util.Objects.requireNonNull;


@Slf4j
@Service
public class SupAuditService {

    @Autowired
    private CfgPersonDB1MasterRepository cfgPersonDB1MasterRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private SupAuditListDB1MasterRepository supAuditlistRepo;

    @Autowired
    private DepAgentIDDB1MasterRepository depAgentIDRepo;

    @Autowired
    private DepExtDB1MasterRepository depExtRepo;

    @Autowired
    private CfgPersonDB1MasterRepository cfgPersonRepo;

    @Autowired
    private LogService logService;

    @Resource
    private DepDB1MasterRepository depRepository;

    private int ID;
    private String UserName; //登入帳號
    private String ableAgentID; //可調聽AgentID
    private String ableExt; //可調聽Ext 分機號碼
    private String DepID; //登入的username
    private String Location = "002"; //default 002=台北
    private String AuditStartDate;
    private String AuditEndDate;
    private String Memo;
    private String CanDL;
    private String Setrec;
    private String errmsg; //非DB欄位,後端錯誤訊息


    /**
     * 取得supervisor 可調聽部門的AgentID
     */
    public List<SupAuditService> getAgentAuditlist(String username) {
        List<String> agentListString = supAuditlistRepo.getAgentAuditListByUsername(username);
        return convertStringToSupServiceBean(agentListString);
    }//-------------------------------------------------------------------------------------

    //找要更改的agentID 的list
    public SupAuditService getAgentstatus(String ableAgentID, String username) {
        List<String> agentList = supAuditlistRepo.getAgentstatus(ableAgentID, username);
        List<SupAuditService> upagent = convertStringToSupServiceBean(agentList);
        return upagent.get(0);
    }//-------------------------------------------------------------------------------------

    //找要更改的Ext 的list
    public SupAuditService getExtstatus(String Ext, String username) {
        List<String> extList = supAuditlistRepo.getExtstatus(Ext, username);
        List<SupAuditService> upagent = convertStringToSupServiceBean(extList);
        return upagent.get(0);
    }//-------------------------------------------------------------------------------------

    //回傳supervisor 可調聽的AgentID List
    public List<String> supgetAbleAgentIDList(String username) {
        try {
            List<String> stringList = supAuditlistRepo.getAbleAgentIDList(username);
            return stringList.stream().map(HtmlEscapeUtils::escapeString).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error to supgetAbleAgentIDList" + e.getMessage());
            throw e;
        }
    }//-------------------------------------------------------------------------------------

    //回傳supervisor 可調聽的Ext List
    public List<String> supgetAbleExtList(String username) {
        List<String> dataStrlist = supAuditlistRepo.getAbleExtList(username);
        return dataStrlist.stream().map(HtmlEscapeUtils::escapeString).collect(Collectors.toList());
    }//-------------------------------------------------------------------------------------

    /**
     * 取得supervisor 可調聽部門的Ext
     */
    public List<SupAuditService> getExtAuditlist(String username) {
        List<String> extListString = supAuditlistRepo.getExtAuditListByUsername(username);
        return convertStringToSupServiceBean(extListString);
    }//-------------------------------------------------------------------------------------

    /**
     * 新增 AgentId
     */
    public void addAgentIDIn(String loginUsername, String userId, SupAuditDto addData) {
        addPermission(loginUsername, userId, addData, "AgentID");
    }

    /**
     * 新增 ExtNum
     */
    public void addExtNumIn(String loginUsername, String userId, SupAuditDto addData) {
        addPermission(loginUsername, userId, addData, "Ext");
    }

    private void addPermission(String loginUsername, String userId, SupAuditDto addData, String type) {
        StringBuilder msg;
        String role = cfgPersonDB1MasterRepository.getUserRole(loginUsername);
        String depIdByAbleAgentID = getDepIdByType(type, addData);
        String monitoringUserName = userService.getLoginNameByID(userId);
        String addedValue = type.equals("AgentID") ? addData.getAbleAgentID():addData.getAbleExt();
        int chkExist = type.equals("AgentID") ? chkAgentIDExist(monitoringUserName, depIdByAbleAgentID, addData.getAbleAgentID()):
                chkExtExist(monitoringUserName, depIdByAbleAgentID, addData.getAbleExt());

        if (chkExist >= 1) {
            msg = new StringBuilder("新增調聽權限失敗: " + addedValue + " 此" + type + "已經存在!");
            addData.setErrmsg(msg.toString());
        } else {
            supAuditlistRepo.save(convertToDto(addData, depIdByAbleAgentID, monitoringUserName));
            msg = new StringBuilder("新增調聽權限成功: " + type + "=" + addedValue + " added by user=" + loginUsername);
        }
        LogDto logDto = new LogDto();
        logDto.setFunctionName("auditsetting");
        logDto.setActionType("add");
        logDto.setInfo(msg.toString());
        logService.addLog(logDto, loginUsername);
    }

    private String getDepIdByType(String type, SupAuditDto addData) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {

                if (StringUtils.equals("AgentID", type)) {
                    return depAgentIDRepo.findAllByAgentID(addData.getAbleAgentID())
                            .map(DepAgentIDDB1Master::getDepID)
                            .orElse("");
                } else {
                    return depExtRepo.findAllByExt(addData.getAbleExt())
                            .map(DepExtDB1Master::getDepID)
                            .orElse("");
                }

            }

            return "";

        } catch (Exception e) {
            log.error("Error to getDepIdByType" + e.getMessage());
            throw e;
        }

    }

    /**
     * 是否 可調聽部門重複AgentId By currentDepId、 agentId
     */
    public boolean isExistsAgentIdByDepIdAndAgentID(String currentDepId, String agentId) {
        try {
            String depIds = depRepository.geAuditGroupIDList(currentDepId).orElse("");
            List<String> depIdArray = Arrays.stream(depIds.split(";")).collect(Collectors.toList());
            depIdArray.add(currentDepId);

            for (String depId : depIdArray) {
                int existsByDepIdAndAgentID = depAgentIDRepo.existsByDepIDAndAgentID(depId, agentId);
                if (existsByDepIdAndAgentID==1)
                    return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error to isExistsAgentIdByDepIdAndAgentID : {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 是否 可調聽部門重複Ext By currentDepId、 agentId
     */
    public boolean isExistsExtByDepIdAndExt(String currentDepId, String ext) {
        try {
            String depIds = depRepository.geAuditGroupIDList(currentDepId).orElse("");
            List<String> depIdArray = Arrays.stream(depIds.split(";")).collect(Collectors.toList());
            depIdArray.add(currentDepId);

            for (String depId : depIdArray) {
                int existsByDepIdAndAgentID = depExtRepo.existsByDepIDAndExt(depId, ext);
                if (existsByDepIdAndAgentID==1)
                    return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error to isExistsExtByDepIdAndExt : {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * delete AgentID
     */
    @Transactional
    public String deleteAgentID(String delSupList, User user) {
        try {
            int i = 0;
            List<String> deleteAgentIdList = new ArrayList<>();
            String[] splitList = delSupList.split(",");
            while (i < splitList.length) {
                deleteAgentIdList.add(splitList[i]);
                i += 2;
            }

            String username = splitList[1];

            if (ToolPlugins.checkAuthorization("authorization")) {

                deleteAgentIdList.forEach(v -> {
                    supAuditlistRepo.deleteByAbleAgentID(username, v);
                });

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("delete");
            logDto.setInfo("刪除調聽by AgentID= " + deleteAgentIdList + " deleted by user=" + user.getUsername());
            logService.addLog(logDto, user.getUsername());

            return username;
        } catch (Exception e) {
            log.error("Error to deleteAgentID");
            throw e;
        }
    }

    /**
     * delete Ext
     */
    @Transactional(rollbackFor = Exception.class)
    public String deleteExt(String delSupList, User user) {
        try {
            int i = 0;
            List<String> deleteExtList = new ArrayList<>();
            String[] splitList = delSupList.split(",");
            while (i < splitList.length) {
                deleteExtList.add(splitList[i]);
                i += 2;
            }

            String username = splitList[1];

            if (ToolPlugins.checkAuthorization("authorization")) {

                deleteExtList.forEach(v -> {
                    supAuditlistRepo.deleteByExt(username, v);
                });

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("delete");
            logDto.setInfo("刪除調聽by ExtNum= " + deleteExtList + " deleted by user=" + user.getUsername());
            logService.addLog(logDto, user.getUsername());

            return username;
        } catch (Exception e) {
            log.error("Error to deleteExt");
            throw e;
        }
    }

    /**
     * 檢查是否有同步 可調聽AgentID
     */
    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public boolean checkSyncAgentId(String currentDepId) {
        try {
            List<String> depIdList = getSharedDepIdList(currentDepId);
            List<String> agentIdList = depIdList.stream()
                    .flatMap(depId -> depAgentIDRepo.getAgentIDByDepID(depId).stream())
                    .collect(Collectors.toList());
            return !agentIdList.isEmpty();
        } catch (Exception e) {
            log.error("Error to checkSyncAgentId" + e.getMessage());
            throw e;
        }
    }

    /**
     * 同步 AgentID
     */
    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public void syncAgentId(String currentDepId, String username, String setIsDownload, String setForeverRec) {
        try {
            List<String> depIdList = getSharedDepIdList(currentDepId);
            for (String depId : depIdList) {
                String location = userService.getUserSiteIDByUsername(username);
                List<String> agentIdList = depAgentIDRepo.getAgentIDByDepID(depId);
                for (String agentId : agentIdList) {
                    SupAuditListDB1Master dbAuditList = new SupAuditListDB1Master();
                    dbAuditList.setUserName(username);
                    dbAuditList.setAbleAgentID(agentId);
                    dbAuditList.setDepID(depId);
                    dbAuditList.setLocation(location);
                    dbAuditList.setCanDL(setIsDownload);
                    dbAuditList.setSetrec(setForeverRec);
                    if (chkAgentIDExist(username, depId, agentId)==0)
                        supAuditlistRepo.save(dbAuditList);
                }
            }
        } catch (Exception e) {
            log.error("同步AgentID時發生錯誤：" + e.getMessage());
            throw e;
        }
    }

    private List<String> getSharedDepIdList(String currentDepId) {
        Optional<String> depIdsOptional = depRepository.geAuditGroupIDList(currentDepId);
        List<String> depIdList = new ArrayList<>();
        depIdList.add(currentDepId);

        if (depIdsOptional.isPresent()) {
            String depIds = depIdsOptional.get();
            depIdList.addAll(Arrays.asList(depIds.split(";")));
        }
        return depIdList;
    }

    /**
     * 檢查是否有同步 可調聽Ext
     */
    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public boolean checkSyncExt(String currentDepId) {
        try {
            List<String> depIdList = getSharedDepIdList(currentDepId);
            List<String> extList = depIdList.stream()
                    .flatMap(depId -> depExtRepo.getExtByDepID(depId).stream())
                    .collect(Collectors.toList());

            return !extList.isEmpty();
        } catch (Exception e) {
            log.error("Error to checkSyncExt" + e.getMessage());
            throw e;
        }
    }

    /**
     * 同步 Ext
     */
    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public void syncExt(String currentDepId, String username, String setIsDownload, String setForeverRec) {
        try {
            List<String> depIdList = getSharedDepIdList(currentDepId);
            for (String depId : depIdList) {
                String location = userService.getUserSiteIDByUsername(username);
                List<String> extList = depExtRepo.getExtByDepID(depId);
                for (String ext : extList) {
                    SupAuditListDB1Master dbAuditList = new SupAuditListDB1Master();
                    dbAuditList.setUserName(username);
                    dbAuditList.setAbleExt(ext);
                    dbAuditList.setDepID(depId);
                    dbAuditList.setLocation(location);
                    dbAuditList.setCanDL(setIsDownload);
                    dbAuditList.setSetrec(setForeverRec);
                    if (chkExtExist(username, depId, ext)==0)
                        supAuditlistRepo.save(dbAuditList);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("同步Ext時發生錯誤：" + e.getMessage(), e);
        }
    }

    /**
     * 檢查 AgentID 在supAuditList資料表是否重複(loginName + AgentID + depID)
     */
    private int chkAgentIDExist(String loginName, String depId, String agentId) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                return supAuditlistRepo.chkAgentIDExist(loginName, agentId, depId);
            }

            return 0;

        } catch (Exception e) {
            log.error("Error to chkAgentIDExist" + e.getMessage());
            throw e;
        }

    }

    /**
     * 檢查 Ext 在supAuditList資料表是否重複 (loginName + AgentID + depID)
     */
    private int chkExtExist(String loginName, String depId, String ext) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                return supAuditlistRepo.chkExtNumExist(loginName, ext, depId);
            }

            return 0;

        } catch (Exception e) {
            log.error("Error to chkExtExist" + e.getMessage());
            throw e;
        }
    }

    //共用功能 把ServiceBean 轉成Entity Bean
    private SupAuditListDB1Master convertToDto(SupAuditDto dto, String depIdByAbleAgentID, String monitoringUserName) {
        UserDto monitoringUser = userService.getUserByID(dto.getMonitoringUserId());
        SupAuditListDB1Master entity = new SupAuditListDB1Master();
        entity.setUserName(monitoringUserName);
        entity.setAbleAgentID(dto.getAbleAgentID());
        entity.setAbleExt(dto.getAbleExt());
        entity.setDepID(depIdByAbleAgentID);
        entity.setLocation(monitoringUser.getLocation());
        entity.setCanDL(dto.getCanDL());
        entity.setSetrec(dto.getSetrec());
        entity.setAuditStartDate(null);
        entity.setAuditEndDate(null);
        try {
            if (StringUtils.isNotEmpty(dto.getAuditStartDate()))
                entity.setAuditStartDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dto.getAuditStartDate()));
            if (StringUtils.isNotEmpty(dto.getAuditEndDate()))
                entity.setAuditEndDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dto.getAuditEndDate()));
        } catch (ParseException e) {
            log.error("convertToDto : {}", e.getMessage(), e);
        }
        return entity;
    }//-------------------------------------------------------------------------------------

    //共用功能 把List<String> 轉成List<UBGIR_UserService>
    private List<SupAuditService> convertStringToSupServiceBean(List<String> datalist) {
        List<SupAuditService> auditlist = new ArrayList();
        SupAuditService theaudit;
        String[] parts;
        for (int i = 0; i < datalist.size(); i++) {
            parts = datalist.get(i).split(",");
            theaudit = new SupAuditService();
            theaudit.setID(Integer.parseInt(parts[0]));
            theaudit.setUserName(HtmlEscapeUtils.escapeString(parts[1]));
            theaudit.setAbleAgentID(HtmlEscapeUtils.escapeString(parts[2]));
            theaudit.setAbleExt(HtmlEscapeUtils.escapeString(parts[3]));
            theaudit.setDepID(HtmlEscapeUtils.escapeString(parts[4]));
            theaudit.setLocation(HtmlEscapeUtils.escapeString(parts[5]));
            String startDate = parts[6].substring(0, parts[6].length() - 2);
            String endtDate = parts[7].substring(0, parts[7].length() - 2);
            theaudit.setAuditStartDate(HtmlEscapeUtils.escapeString(startDate));
            theaudit.setAuditEndDate(HtmlEscapeUtils.escapeString(endtDate));
            if (startDate.equals("nu")) {
                theaudit.setAuditStartDate("");
            }
            if (endtDate.equals("nu")) {
                theaudit.setAuditEndDate("");
            }
            theaudit.setCanDL(HtmlEscapeUtils.escapeString(parts[8]));
            theaudit.setSetrec(HtmlEscapeUtils.escapeString(parts[9]));
            theaudit.setMemo(HtmlEscapeUtils.escapeString(parts[10]));

            auditlist.add(theaudit);
        }
        return auditlist;
    }//-------------------------------------------------------------------------------------

    // set DownLoad By AgentID
    @Transactional(rollbackFor = Exception.class)
    public void setDLByAgentID(String Username, List<String> agent, List<String> status, String username) {

        try {

            for (int i = 0; i < agent.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setDLIDStatus(Username, agent.get(i), status.get(i));
                }

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("update");
            logDto.setInfo("解鎖下載權限by AgentID= " + agent + " set by user=" + username);
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to setDLByAgentID");
            throw e;
        }
    }

    // set rec By AgentID
    @Transactional(rollbackFor = Exception.class)
    public void setrecByAgentID(String Username, List<String> agent, List<String> status, String loginUser) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {

                for (int i = 0; i < agent.size(); i++) {
                    supAuditlistRepo.setrecStatus(Username, agent.get(i), status.get(i));
                }

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("update");
            logDto.setInfo("解鎖錄音檔權限by AgentID= " + agent + " set by user=" + loginUser);
            logService.addLog(logDto, loginUser);

        } catch (Exception e) {
            log.error("Error to setrecByAgentID");
            throw e;
        }

    }

    // set rec and DL By AgentID
    @Transactional(rollbackFor = Exception.class)
    public void setacByAgentID(String Username, List<String> agent, List<String> status,
                               List<String> recagent, List<String> recstatus, String username) {
        try {

            for (int i = 0; i < agent.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setDLIDStatus(Username, agent.get(i), status.get(i));
                }

            }

            for (int i = 0; i < recagent.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setrecStatus(Username, recagent.get(i), recstatus.get(i));
                }

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("update");
            logDto.setInfo("解鎖錄音檔權限by AgentID= " + agent + " 解鎖下載權限by AgentID= "
                    + recagent + " set by user=" + username);
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to setacByAgentID");
            throw e;
        }
    }

    // set DownLoad By Ext
    @Transactional(rollbackFor = Exception.class)
    public void setDLByExt(String Username, List<String> ext, List<String> status, String username) {

        try {

            for (int i = 0; i < ext.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setDLExtStatus(Username, ext.get(i), status.get(i));
                }

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("update");
            logDto.setInfo("解鎖下載權限by Ext= " + ext + " set by user=" + username);
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to setDLByExt");
            throw e;
        }
    }

    // set rec By AgentID
    @Transactional(rollbackFor = Exception.class)
    public void setrecByExt(String Username, List<String> ext, List<String> status, String username) {

        try {

            for (int i = 0; i < ext.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setrecExtStatus(Username, ext.get(i), status.get(i));
                }

            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("update");
            logDto.setInfo("解鎖錄音黨權限by Ext= " + ext + " set by user=" + username);
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to setrecByExt");
            throw e;
        }
    }// -------------------------------------------------------------------------------------

    // set rec and DL By Ext
    @Transactional(rollbackFor = Exception.class)
    public void setacByExt(String Username, List<String> ext, List<String> status, List<String> recext,
                           List<String> recstatus, String username) {

        try {

            for (int i = 0; i < ext.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setDLExtStatus(Username, ext.get(i), status.get(i));
                }

            }

            for (int i = 0; i < recext.size(); i++) {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    supAuditlistRepo.setrecExtStatus(Username, recext.get(i), recstatus.get(i));
                }

            }
            LogDto logDto = new LogDto();
            logDto.setFunctionName("auditsetting");
            logDto.setActionType("update");
            logDto.setInfo("解鎖下載權限 by Ext= " + ext + "解鎖錄音檔權限 by Ext= " + recext + " set by user=" + username);
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to setacByExt");
            throw e;
        }
    }// -------------------------------------------------------------------------------------

    //更新單一user資料
    @Transactional(rollbackFor = Exception.class)
    public void updateAgentstatus(SupAuditDto dto, String username) throws ParseException {
        String actionDetail = " 修改 " + dto.getUserName() + " 的 AgentID: " + dto.getAbleAgentID() + "的權限設定。";
        ZonedDateTime nowInTaipei = ZonedDateTime.now(ZoneId.of("Asia/Taipei"));

        Timestamp starttamp = null;
        Timestamp endtamp = null;
        if (StringUtils.isNotEmpty(dto.getAuditStartDate())) {
            Date startdate = convertStringDateToDate(dto.getAuditStartDate(), "yyyy-MM-dd HH:mm:ss", false);
            starttamp = new Timestamp(requireNonNull(startdate).getTime());
        }

        if (StringUtils.isNotEmpty(dto.getAuditEndDate())) {
            Date enddate = convertStringDateToDate(dto.getAuditEndDate(), "yyyy-MM-dd HH:mm:ss", false);
            endtamp = new Timestamp(requireNonNull(enddate).getTime());
        }

        if (ToolPlugins.checkAuthorization("authorization")) {
            supAuditlistRepo.updateStatusByAgentID(starttamp, endtamp, dto.getCanDL(), dto.getSetrec(), dto.getUserName(), dto.getAbleAgentID());
        }

        LogDto logDto = new LogDto();
        logDto.setFunctionName("auditsetting");
        logDto.setActionType("update");
        logDto.setInfo(actionDetail);
        logService.addLog(logDto, username);
    }

    //更新單一user資料
    @Transactional(rollbackFor = Exception.class)
    public void updateExtstatus(SupAuditDto dto, String username) throws ParseException {
        String actionDetail = " 修改 " + dto.getUserName() + " 的 Ext: " + dto.getAbleAgentID() + "的權限設定。";

        Timestamp starttamp = null;
        Timestamp endtamp = null;
        if (!dto.getAuditStartDate().isEmpty()) {
            Date startdate = convertStringDateToDate(dto.getAuditStartDate(), "yyyy-MM-dd HH:mm:ss", false);
            starttamp = new Timestamp(requireNonNull(startdate).getTime());
        }

        if (!dto.getAuditEndDate().isEmpty()) {
            Date enddate = convertStringDateToDate(dto.getAuditEndDate(), "yyyy-MM-dd HH:mm:ss", false);
            endtamp = new Timestamp(requireNonNull(enddate).getTime());
        }

        if (ToolPlugins.checkAuthorization("authorization")) {
            supAuditlistRepo.updateStatusByExt(starttamp, endtamp, dto.getCanDL(), dto.getSetrec(), dto.getUserName(), dto.getAbleExt());
        }

        LogDto logDto = new LogDto();
        logDto.setFunctionName("auditsetting");
        logDto.setActionType("update");
        logDto.setInfo(actionDetail);
        logService.addLog(logDto, username);

    }

    //Getter and Setter
    public int getID() {
        return ID;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getAbleAgentID() {
        return ableAgentID;
    }

    public void setAbleAgentID(String ableAgentID) {
        this.ableAgentID = ableAgentID;
    }

    public String getAbleExt() {
        return ableExt;
    }

    public void setAbleExt(String albeExt) {
        this.ableExt = albeExt;
    }

    public String getDepID() {
        return DepID;
    }

    public void setDepID(String depID) {
        DepID = depID;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getAuditStartDate() {
        return AuditStartDate;
    }

    public void setAuditStartDate(String auditStartDate) {
        AuditStartDate = auditStartDate;
    }

    public String getAuditEndDate() {
        return AuditEndDate;
    }

    public void setAuditEndDate(String auditEndDate) {
        AuditEndDate = auditEndDate;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getCanDL() {
        return CanDL;
    }

    public void setCanDL(String canDL) {
        CanDL = canDL;
    }

    public String getSetrec() {
        return Setrec;
    }

    public void setSetrec(String setrec) {
        Setrec = setrec;
    }

    @Override
    public String toString() {
        return "SupAuditService{" +
                "userService=" + userService +
                ", supAuditlistRepo=" + supAuditlistRepo +
                ", depAgentIDRepo=" + depAgentIDRepo +
                ", depExtRepo=" + depExtRepo +
                ", cfgPersonRepo=" + cfgPersonRepo +
                ", logService=" + logService +
                ", ID=" + ID +
                ", UserName='" + UserName + '\'' +
                ", ableAgentID='" + ableAgentID + '\'' +
                ", ableExt='" + ableExt + '\'' +
                ", DepID='" + DepID + '\'' +
                ", Location='" + Location + '\'' +
                ", AuditStartDate='" + AuditStartDate + '\'' +
                ", AuditEndDate='" + AuditEndDate + '\'' +
                ", Memo='" + Memo + '\'' +
                ", CanDL='" + CanDL + '\'' +
                ", Setrec='" + Setrec + '\'' +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }
}
