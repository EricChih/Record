package com.ub.gir.web.service;


import com.ub.gir.web.dto.UserDTO;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.dto.user.UserSearchForm;
import com.ub.gir.web.dto.user.UserUpdateDto;
import com.ub.gir.web.entity.BaseCfgPerson;
import com.ub.gir.web.entity.db1.master.CfgPersonDB1Master;
import com.ub.gir.web.entity.db1.master.PasswordHistoryDB1Master;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.PasswordHistoryDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.SupAuditListDB1MasterRepository;
import com.ub.gir.web.util.HtmlEscapeUtils;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Service
public class UserService {
    @Autowired
    private CfgPersonDB1MasterRepository cfgPersonRepo;
    @Autowired
    private PasswordHistoryDB1MasterRepository pwdhistoryRepo;
    @Autowired
    private LogService logService;
    @Autowired
    private DepService depService;
    @Autowired
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private CfgPersonDB1MasterRepository CfgPersonRepository;
    @Resource
    private PwdService pwdService;
    @Resource
    private SupAuditListDB1MasterRepository supAuditListDB1MasterRepository;
    @PersistenceContext(unitName = "db1MasterPersistenceUnit")
    private EntityManager entityManager;
    //default listing maxsize
    @Value("${ub.view.page.max-size}")
    private int maxsize;

    //此Service Bean 的參數
    private int ID;
    private int PID;
    private String AgentID = "NA"; //default NA=沒有
    private String FirstName; //名字
    private String LastName; //姓氏
    private String Name; //登入的username
    private String Role = "admin"; //預設為admin
    private String Email = "NA";
    private String Ext = "NA"; //default NA=沒有
    private String Location;
    private int SupervisorID;
    private int ManagerID;
    private int is_agent;
    private int is_admin;
    private String DepID;
    private String Status = "1"; //1=使用中; 0=停用; 3=使用中+已轉移新密碼
    private String FinalChangeDate;
    private String LastLoginDate;
    private String ACanDL;
    private String ASetrec;
    private String ECanDL;
    private String ESetrec;
    private String errmsg; //非DB欄位,後端錯誤訊息
    private String sqlstr; //非DB欄位,暫存sql
    private String LocationCN; //非DB欄位,帳號所在地的中文 台北(002), 高雄(007)
    private String OtherLocationCN; //非DB欄位,其他點,若user本地在台北,other就是高雄
    private String StatusCN; //非DB欄位,0=停用,1=使用中, 3=密碼已加密
    private String OpenBothSite = "N"; //是否同步開啟北高帳號,相同帳號002 and 007同時開
    private int Attempts = 3;

    private String loginUserRole; // 非DB欄位,登入者的角色
    private String loginUserDepId; // 非DB欄位,登入者的部門ID

    // 統計目前cfg_person Table裡的資料總筆數
    public Long count() {
        return cfgPersonRepo.count();
    }

    // Get user role by given username
    public String getUserRoleByUsername(String username) {
        String userrole = "supadmin";
        List<String> roledata = cfgPersonRepo.getUserRoleByUsername(username.toLowerCase());
        if (roledata.size() >= 1) {
            userrole = roledata.get(0);
        }
        return userrole.trim();
    }//-------------------------------------------------------------------------------------------------

    public String getDepIDByUsername(String username) {
        try {
            Optional<String> depID = cfgPersonRepo.getDepIDByUsername(username.toLowerCase());

            return depID.orElse("IT");
        } catch (Exception e) {
            log.error("Error to getDepIDByUsername : {}", e.getMessage(), e);
        }
        return "IT";
    }

    public String getDepIdByUsernameRole(String username, String role) {
        try {
            Optional<String> depID = cfgPersonRepo.getDepIDByUsernameRole(username, role);
            return depID.orElse("NA");
        } catch (Exception e) {
            log.error("Error to getDepIdByUsernameRole" + e.getMessage());
            throw e;
        }
    }

    // Get user depid by given uid
    public String getDepIDByID(String uid) {
        String depid = "IT";

        List<String> depidlist = cfgPersonRepo.getDepIDByID(uid);
        if (!depidlist.isEmpty()) {
            depid = depidlist.get(0);
        }
        return depid;
    }//-------------------------------------------------------------------------------------------------

    public String getIDByLoginName(String name) {
        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                return cfgPersonRepo.findIDByName(name);
            }

        } catch (Exception e) {
            log.error("Error to getIDByLoginName" + e.getMessage());
            throw e;
        }

        return null;

    }

    // Get user login name by given uid
    public String getLoginNameByID(String uid) {
        String loginname = null;
        List<String> thelist = cfgPersonRepo.getLoginNameByID(uid);
        if (thelist.size() >= 1) {
            loginname = thelist.get(0);
        }
        return loginname;
    }//-------------------------------------------------------------------------------------------------

    // Get user siteid 002=台北, 007=高雄 by given username
    public String getUserSiteIDByUsername(String username) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                return cfgPersonRepo.getUserSiteIDByUsername(username.toLowerCase()).orElse("002");
            }

            return null;

        } catch (Exception e) {
            log.error("Error to getUserSiteIDByUsername" + e.getMessage());
            throw e;
        }

    }//-------------------------------------------------------------------------------------------------

    // Get one User data by given username
    public UserDto getUserByUsername(String username) {
        List<String> updatedata = cfgPersonRepo.getUserByUsername(username);
        List<UserDto> updatelist = convertStringToUServiceBean(updatedata);

        return updatelist.get(0);
    }//-------------------------------------------------------------------------------------

    // Get one User data by given username
    public UserDto getUserByID(String uid) {
        List<String> updateData = cfgPersonRepo.getUserByID(uid);
        List<UserDto> updatelist = convertStringToUServiceBean(updateData);

        return updatelist.get(0);
    }//-------------------------------------------------------------------------------------


    // Get Supervisor list by given DepID 撈同一個部門的人員
    public List<UserDto> getSupByDepID(String depid) {
        List<String> strlist = cfgPersonRepo.getSupByDepID(depid.toUpperCase().trim());
        return convertStringToUServiceBean(strlist);
    }//-------------------------------------------------------------------------------------

    //get all users List<GIR_UserService> with default maxsize
    public List<UserDto> getAllWithRole() {
        List<String> datalist = cfgPersonRepo.findAllWithRole(this.maxsize);
        List<UserDto> userlist = convertStringToUServiceBean(datalist);
        return userlist;
    }//-------------------------------------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    // 新增User data to DB
    public void addUser(UserDTO adduser, String username) {
        ZonedDateTime nowInTaipei = ZonedDateTime.now(ZoneId.of("Asia/Taipei"));
        adduser.setFinalChangeDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(nowInTaipei));
        adduser.setLastLoginDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(nowInTaipei));

        final char[] password = passwordEncoder.encode(adduser.getName()).toCharArray();
        adduser.setPasswordNew(new String(password));
        Arrays.fill(password, '0');

        String location = getUserSiteIDByUsername(username);
        userService.saveUserIntoDB(adduser, location);

        LogDto logDto = new LogDto();
        logDto.setFunctionName("usermgr");
        logDto.setActionType("add");
        logDto.setInfo("新增成功: 登入帳號=" + adduser.getName() + " 由 user=" + username + "新增完成");
        logService.addLog(logDto, username);
    }//-------------------------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public void saveUserIntoDB(UserDTO saveuser, String location) {
        cfgPersonRepo.save(convertServiceToEntityBean(saveuser, location));
    }

    // 目前這種作法不能同時rollback兩個dataSource
    @Transactional(rollbackFor = Exception.class)
    public void delUserByName(String delUsername, String username) {
        try {
            delDb1User(delUsername);
            if (ToolPlugins.checkAuthorization("authorization")) {
                supAuditListDB1MasterRepository.deleteByUserName(delUsername);
            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("usermgr");
            logDto.setActionType("delete");
            logDto.setInfo("刪除成功: 登入帳號= " + delUsername);
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to delUserByName");
            throw e;
        }
    }//-------------------------------------------------------------------------------------

    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public void delDb1User(String delUsername) {
        try {
            if (ToolPlugins.checkAuthorization("authorization")) {
                if (cfgPersonRepo.existsUserUsername(delUsername)) {
                    cfgPersonRepo.deleteByUsername(delUsername);
                }
            }
        } catch (Exception e) {
            log.error("Error to delDb1User" + e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    // delete user by given delUsername
    public void stopUserByName(String delUsername, String username) {
        try {
            if (ToolPlugins.checkAuthorization("authorization")) {
                cfgPersonRepo.stopByUsername(delUsername);
            }
        } catch (Exception e) {
            log.error("Error to stopByUsername" + e.getMessage());
            throw e;
        }
        LogDto logDto = new LogDto();
        logDto.setFunctionName("usermgr");
        logDto.setActionType("update");
        logDto.setInfo("停用: 登入帳號= " + delUsername);
        logService.addLog(logDto, username);
    }//-------------------------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    // reset user pwd by given delUsername
    public boolean resetUserPwdByName(String resetUsername, String username) {
        try {
            if (ToolPlugins.checkAuthorization("authorization")) {
                if (cfgPersonRepo.existsUserUsername(resetUsername)) {
                    final char[] password = passwordEncoder.encode(resetUsername).toCharArray();
                    cfgPersonRepo.resetUserPwdByName(resetUsername, new String(password));
                    Arrays.fill(password, '0');
                    userService.setAttempts(4);

                    LogDto logDto = new LogDto();
                    logDto.setFunctionName("usermgr");
                    logDto.setActionType("update");
                    logDto.setInfo("密碼重設: 登入帳號= " + resetUsername);
                    logService.addLog(logDto, username);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error to resetUserPwdByName" + e.getMessage());
            throw e;
        }
    }//-------------------------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public void updateDb1PwdByUsername(String username, String pwd) {
        try {
            if (ToolPlugins.checkAuthorization("authorization")) {
                if (cfgPersonRepo.existsUserUsername(username)) {
                    final char[] password = passwordEncoder.encode(pwd).toCharArray();
                    cfgPersonRepo.updatePasswordNewByUsername(new String(password), username);
                    PasswordHistoryDB1Master hisPwdEntity = new PasswordHistoryDB1Master();
                    hisPwdEntity.setUserName(username);
                    hisPwdEntity.setOLDPassword(new String(password));
                    ZonedDateTime nowInTaipei = ZonedDateTime.now(ZoneId.of("Asia/Taipei"));
                    hisPwdEntity.setChangeDate(Date.from(nowInTaipei.toInstant()));
                    Arrays.fill(password, '0');
                    pwdhistoryRepo.save(hisPwdEntity);

                    LogDto logDto = new LogDto();
                    logDto.setFunctionName("home");
                    logDto.setActionType("changepwd");
                    logDto.setInfo("密碼變更: 登入帳號= " + username + " 變更完成");
                    logService.addLog(logDto, username);
                }
            }
        } catch (Exception e) {
            log.error("Error to updateDb1PwdByUsername" + e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    //更新單一user資料
    public void updateUserByUsername(UserUpdateDto dto, String username) {
        updateDb1UserInformation(dto);

        LogDto logDto = new LogDto();
        logDto.setFunctionName("usermgr");
        logDto.setActionType("update");
        logDto.setInfo("修改: 登入帳號=" + dto.getName() + " 的資料");
        logService.addLog(logDto, username);
    }

    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public void updateDb1UserInformation(UserUpdateDto updateuser) {
        ZonedDateTime nowInTaipei = ZonedDateTime.now(ZoneId.of("Asia/Taipei"));
        try {
            if (ToolPlugins.checkAuthorization("authorization")) {
                cfgPersonRepo.updateUserByName(updateuser.getName(), updateuser.getFirstName(),
                        updateuser.getLastName(), updateuser.getRole(), updateuser.getDepID(),
                        "NA", "NA", "NA",
                        updateuser.getStatus(), Timestamp.from(nowInTaipei.toInstant()));
            }
        } catch (Exception e) {
            log.error("Error to updateDb1UserInformation" + e.getMessage());
            throw e;
        }
    }

    public List<UserDto> searchUsers(UserSearchForm searchuser, String username, String userRole, String userDepId) {
        List<Object[]> searchResult = queryUsers(searchuser, username, userRole, userDepId);
        List<String> strResult = new ArrayList();
        for (Object[] result : searchResult) {
            strResult.add(result[0] + "," + result[1] + "," + result[2] + "," + result[3] + "," +
                    result[4] + "," + result[5] + "," + result[6] + "," + result[7] + "," + result[8] + "," +
                    result[9] + "," + result[10] + "," + result[11] + "," + result[12] + "," + result[13] + "," + result[14]);
        }
        return convertStringToUServiceBean(strResult);
    }

    // 檢查使用者是否存在於台北或是高雄的部門
    public boolean checkUserNameExist(String userName) {
        try {
                if (ToolPlugins.checkAuthorization("authorization")) {
                    return cfgPersonRepo.existsUserUsername(userName);
                }
        } catch (Exception e) {
            log.error("Error to checkUserNameExist" + e.getMessage());
            throw e;
        }
        return true;
    }

    public boolean checkSidebarVisibleByUserNameAndPwd(String username) throws Exception {
        BaseCfgPerson cfgPerson = CfgPersonRepository.findByName(username)
                .orElseThrow(() -> new NotFoundException("查無使用者帳號"));
        // 帳號密碼不同且最後更改密碼日期還沒到，就代表可以顯示側邊欄
        return !passwordEncoder.matches(username, cfgPerson.getPasswordNew()) &&
                !pwdService.isRequiredChangePwd(cfgPerson);

    }

    //共用功能 用input GIR_UserService bean 動態組出 select sql string
    private List<Object[]> queryUsers(UserSearchForm searchuser, String username, String userRole, String userDepId) {
        try {
            StringBuilder sb = new StringBuilder("SELECT ID, Name, FirstName, LastName, Role, DepID, ")
                    .append("IF(char_length(AgentID)>0, AgentID, '0') as AgentID, ")
                    .append("IF(char_length(Ext)>0, Ext, '0') as Ext, Location, Status, ")
                    .append("IF(char_length(Password)>0, Password, 'NA') as Password, ")
                    .append("IF(char_length(Email)>0, Email, 'NA') as Email, FinalChangeDate, ")
                    .append("LastLoginDate, IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew ")
                    .append("From cfg_person WHERE Location= :location ");
            Map<String, Object> params = new HashMap<>();
            params.put("location", getUserSiteIDByUsername(username));

            if (StringUtils.isNotBlank(searchuser.getName())) {
                sb.append("and Name = :name ");
                params.put("name", searchuser.getName());
            }

            if (StringUtils.isNotBlank(searchuser.getFirstName())) {
                sb.append("and FirstName like :firstName ");
                params.put("firstName", "%" + searchuser.getFirstName() + "%");
            }

            if (StringUtils.isNotBlank(searchuser.getLastName())) {
                sb.append("and LastName like :lastName ");
                params.put("lastName", searchuser.getLastName() + "%");
            }

            if (StringUtils.isNotBlank(searchuser.getAgentId())) {
                sb.append("and AgentID = :agentID ");
                params.put("agentID", searchuser.getAgentId());
            }

            if (StringUtils.isNotBlank(searchuser.getDepId())) {
                sb.append("and DepID = :depID ");
                params.put("depID", searchuser.getDepId());
            } else {
                String depGroup = "";
                // 如果是manager，就撈代管部門的所有人員
                if (userRole.equalsIgnoreCase("manager")) { //卡處資訊科代理北高卡處處理其他部門
                    depGroup = depService.getDepGroupTeam(userDepId);
                    List<String> groupTeams = Arrays.asList(depGroup.split(";"));
                    if (!groupTeams.isEmpty()) {
                        sb.append("and DepID in :depIds ");
                        params.put("depIds", groupTeams);
                    }
                }
            }
            if (StringUtils.isNotBlank(searchuser.getRole())) {
                sb.append("and Role = :role ");
                params.put("role", searchuser.getRole());
            }
            if (StringUtils.isNotBlank(searchuser.getExt())) {
                sb.append("and Ext = :ext ");
                params.put("ext", searchuser.getExt());
            }

            if (StringUtils.isNotBlank(searchuser.getStatus())) {
                sb.append("and Status = :status ");
                params.put("status", searchuser.getStatus());
            }
            Query query = entityManager.createNativeQuery(sb.toString());
            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error queryUsers : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    //共用功能 把List<String> 轉成List<UBGIR_UserService>
    private List<UserDto> convertStringToUServiceBean(List<String> datalist) {
        List<UserDto> ulist = new ArrayList();
        for (String s : datalist) {
            String[] parts = s.split(",");
            UserDto resDto = new UserDto();
            resDto.setID(Integer.parseInt(parts[0]));
            resDto.setName(HtmlEscapeUtils.escapeString(parts[1]));
            resDto.setFirstName(HtmlEscapeUtils.escapeString(parts[2]));
            resDto.setLastName(HtmlEscapeUtils.escapeString(parts[3]));
            resDto.setRole(HtmlEscapeUtils.escapeString(parts[4]));
            resDto.setDepID(HtmlEscapeUtils.escapeString(parts[5]));
            resDto.setAgentID(HtmlEscapeUtils.escapeString(parts[6]));
            resDto.setExt(HtmlEscapeUtils.escapeString(parts[7]));
            resDto.setLocation(HtmlEscapeUtils.escapeString(parts[8]));
            resDto.setLocationCN(getLocationCNByCode(HtmlEscapeUtils.escapeString(parts[8]))); //設定非db欄位
            resDto.setStatus(HtmlEscapeUtils.escapeString(parts[9]));
            resDto.setStatusCN(getStatusCNByCode(HtmlEscapeUtils.escapeString(parts[9]))); //設定非db欄位
            resDto.setEmail(HtmlEscapeUtils.escapeString(parts[11]));
            resDto.setFinalChangeDate(HtmlEscapeUtils.escapeString(parts[12]));
            resDto.setLastLoginDate(HtmlEscapeUtils.escapeString(parts[13]));
            ulist.add(resDto);
        }
        return ulist;
    }

    //共用功能 把ServiceBean 轉成Entity Bean
    private CfgPersonDB1Master convertServiceToEntityBean(UserDTO userdata, String currentLocation) {
        CfgPersonDB1Master theuser = new CfgPersonDB1Master();
        theuser.setName(userdata.getName());
        theuser.setFirstName(userdata.getFirstName());
        theuser.setLastName(userdata.getLastName());
        theuser.setRole(userdata.getRole());
        theuser.setDepID(userdata.getDepID());
        theuser.setAgentID(userdata.getAgentID());
        if (userdata.getAgentID().equalsIgnoreCase("NA")) {
            theuser.setPID(0);
        } else {
            theuser.setPID(Integer.parseInt(userdata.getAgentID()));
        }
        theuser.setExt(userdata.getExt());
        theuser.setLocation(currentLocation);
        theuser.setStatus(userdata.getStatus());
        theuser.setEmail(userdata.getEmail());
        try {
            theuser.setFinalChangeDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(userdata.getFinalChangeDate()));
            theuser.setLastLoginDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(userdata.getLastLoginDate()));
        } catch (ParseException e) {
            log.error("convertServiceToEntityBean : {}", e.getMessage(), e);
        }
        theuser.setPasswordNew(userdata.getPasswordNew());

        return theuser;
    }

    private String getLocationCNByCode(String locationCode) {
        return locationCode.trim().equals("002") ? "台北":"高雄";
    }

    private String getStatusCNByCode(String statusCode) {
        return statusCode.trim().equals("0") ? "0:已鎖定":"1:使用中";
    }

    //參數設定的 Setter and Getter
    public int getID() {
        return ID;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int pID) {
        PID = pID;
    }

    public String getAgentID() {
        return AgentID;
    }

    public void setAgentID(String agentID) {
        agentID.replaceAll(",", ":");
        AgentID = agentID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        firstName.replaceAll(",", ":");
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        lastName.replaceAll(",", ":");
        LastName = lastName;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        name.replaceAll(",", ":");
        Name = name;
    }

    public String getRole() {
        return Role.toLowerCase();
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        email.replaceAll(",", ":");
        Email = email;
    }

    public String getExt() {
        return Ext;
    }

    public void setExt(String ext) {
        ext.replaceAll(",", ":");
        Ext = ext;
    }

    public String getLocation() {
        return Location;
    }

    public List<String> getLocationList() {
        return Collections.singletonList(Location);
    }

    public void setLocation(String location) {
        location.replaceAll(",", ":");
        Location = location;
    }

    public int getSupervisorID() {
        return SupervisorID;
    }

    public void setSupervisorID(int supervisorID) {
        SupervisorID = supervisorID;
    }

    public int getManagerID() {
        return ManagerID;
    }

    public void setManagerID(int managerID) {
        ManagerID = managerID;
    }

    public int getIs_agent() {
        return is_agent;
    }

    public void setIs_agent(int is_agent) {
        this.is_agent = is_agent;
    }

    public int getIs_admin() {
        return is_admin;
    }

    public void setIs_admin(int is_admin) {
        this.is_admin = is_admin;
    }

    public String getDepID() {
        return ObjectUtils.isEmpty(DepID) ? DepID:DepID.toUpperCase();
    }

    public void setDepID(String depID) {
        depID.replaceAll(",", ":");
        DepID = depID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        status.replaceAll(",", ":");
        Status = status;
    }

    public String getFinalChangeDate() {
        return FinalChangeDate;
    }

    public void setFinalChangeDate(String finalChangeDate) {
        finalChangeDate.replaceAll(",", ":");
        FinalChangeDate = finalChangeDate;
    }

    public String getLastLoginDate() {
        return LastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        lastLoginDate.replaceAll(",", ":");
        LastLoginDate = lastLoginDate;
    }

    public String getErrmsg() { //非db欄位
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getSqlstr() {
        return sqlstr;
    }

    public void setSqlstr(String sqlstr) {
        this.sqlstr = sqlstr;
    }

    public String getOpenBothSite() {
        return OpenBothSite;
    }

    public void setOpenBothSite(String bothopen) {
        this.OpenBothSite = bothopen;
    }

    public String getLocationCN() {
        return LocationCN;
    }

    //同步設定Location and OtherLocation
    public void setLocationCN(String locationcode) {
        this.LocationCN = "台北";
        this.OtherLocationCN = "高雄";

        if (locationcode.trim().equals("007")) {
            this.LocationCN = "高雄";
            this.OtherLocationCN = "台北";
        } else {
            this.LocationCN = "台北";
            this.OtherLocationCN = "高雄";
        }
    }

    public String getOtherLocationCN() {
        return OtherLocationCN;
    }

    public String getStatusCN() {
        return StatusCN;
    }

    public void setStatusCN(String statuscode) {
        if (statuscode.trim().equals("0")) {
            StatusCN = "0:已鎖定";
        } else {
            StatusCN = "1:使用中";
        }
    }

    public int getAttempts() {

        return Attempts;
    }

    public void setAttempts(int attempts) {
        Attempts = attempts;
    }

    public String getACanDL() {
        return ACanDL;
    }

    public void setACanDL(String ACanDL) {
        this.ACanDL = ACanDL;
    }

    public String getASetrec() {
        return ASetrec;
    }

    public void setASetrec(String ASetrec) {
        this.ASetrec = ASetrec;
    }

    public String getECanDL() {
        return ECanDL;
    }

    public void setECanDL(String ECanDL) {
        this.ECanDL = ECanDL;
    }

    public String getESetrec() {
        return ESetrec;
    }

    public void setESetrec(String ESetrec) {
        this.ESetrec = ESetrec;
    }

    public String getLoginUserRole() {
        return loginUserRole;
    }

    public void setLoginUserRole(String loginUserRole) {
        this.loginUserRole = loginUserRole;
    }

    public String getLoginUserDepId() {
        return loginUserDepId;
    }

    public void setLoginUserDepId(String loginUserDepId) {
        this.loginUserDepId = loginUserDepId;
    }
}
