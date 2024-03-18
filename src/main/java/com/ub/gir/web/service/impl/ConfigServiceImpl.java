package com.ub.gir.web.service.impl;


import com.ub.gir.web.dto.girConfig.GirConfigResDto;
import com.ub.gir.web.dto.girConfig.UpdatePwdReqDto;
import com.ub.gir.web.dto.girConfig.UpdateScheduleReqDto;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.entity.db1.master.GirConfigDB1Master;
import com.ub.gir.web.entity.parameter.PasswordParameter;
import com.ub.gir.web.entity.parameter.ScheduleParameter;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.GirConfigDB1MasterRepository;
import com.ub.gir.web.service.ConfigService;
import com.ub.gir.web.service.LogService;
import com.ub.gir.web.util.HtmlEscapeUtils;
import com.ub.gir.web.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ConfigServiceImpl implements ConfigService {
    @Resource
    private GirConfigDB1MasterRepository girConfigRepo;
    @Resource
    private LogService logService;

    @Value("${server.servlet.session.timeout}")
    private String overtime;

    /**
     * 取得 site list
     */
    @Override
    public TreeMap<String, String> getAllSiteMap() {
        try {
            return convertListStringToMap(girConfigRepo.getAllSite());
        } catch (Exception e) {
            log.error("Error to getAllSiteMap" + e.getMessage());
            throw e;
        }
    }

    /**
     * 取得 role list
     */
    @Override
    public TreeMap<String, String> getAllRoleMap() {
        try {
            return convertListStringToMap(girConfigRepo.getAllRole());
        } catch (Exception e) {
            log.error("Error to getAllRoleMap" + e.getMessage());
            throw e;
        }
    }

    private List<String> getAllModule(){
        try {
            return girConfigRepo.getAllModule();
        } catch (Exception e) {
            log.error("Error to getAllModule" + e.getMessage());
            throw e;
        }
    }

    /**
     * 取得 module list
     */
    @Override
    public TreeMap<String, String> getAllModuleMap() {
        return convertListStringToMap(getAllModule());
    }

    /**
     * 取得 module list by role
     */
    @Override
    public TreeMap<String, String> getModuleMapByRole(String role) {
        TreeMap<String, String> rtMap = convertListStringToMap(getAllModule());

        if (role.equalsIgnoreCase("superadmin")) {
        } else if (role.equalsIgnoreCase("admin")){
            rtMap.remove("depmgr");//移除:非admin 使用模組
        } else if (role.equalsIgnoreCase("manager")) {
            rtMap.remove("reclist"); //移除:非mgr 使用模組
            rtMap.remove("hisreclist");
            rtMap.remove("keepreclist");
            rtMap.remove("systemsetting");
            rtMap.remove("recyear");
        } else if (role.equalsIgnoreCase("supervisor")) {
            rtMap.remove("systemsetting");
            rtMap.remove("recyear");
            rtMap.remove("depmgr");
            rtMap.remove("usermgr");
        }
        return rtMap;
    }

    private List<String> getAllAct(){
        try {
            return girConfigRepo.getAllAct();
        } catch (Exception e) {
            log.error("Error to getAllAct" + e.getMessage());
            throw e;
        }
    }

    /**
     * 取得 act list
     */
    @Override
    public TreeMap<String, String> getAllActMap() {
        return convertListStringToMap(getAllAct());
    }

    /**
     * 取得 act list by role
     */
    @Override
    public TreeMap<String, String> getActMapByRole(String role) {
        TreeMap<String, String> rtMap = convertListStringToMap(getAllAct());
        if (role.equalsIgnoreCase("admin")) {
            rtMap.remove("stop");
        } else if (role.equalsIgnoreCase("manager")) {
            rtMap.remove("download");
            rtMap.remove("play");
            rtMap.remove("keep");
            rtMap.remove("nokeep");
        } else if (role.equalsIgnoreCase("supervisor")) {
            rtMap.remove("add");
            rtMap.remove("update");
            rtMap.remove("delete");
            rtMap.remove("stop");
        }
        return rtMap;
    }

    /**
     * 取得 所有系統安全設定
     */
    @Override
    public GirConfigResDto getSys() {
        Map<String, char[]> mapData = this.getSysPwd();
        Map<String, char[]> scheduleMap = this.getSchedule();
        mapData.putAll(scheduleMap);

        GirConfigResDto resDto = JsonUtil.convertObjectToClass(mapData, GirConfigResDto.class);
        resDto.setDbLogKeepDay(this.getLogKeepDays());

        return resDto;
    }

    /**
     * 取得 操作軌跡保留天數
     */
    @Override
    public int getLogKeepDays() {
        int defaultValue = 60;
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(ScheduleParameter.KEEP_LOG_DAYS.getIdCode());

        return girConfigOptional.map(girConfig -> Integer.parseInt(girConfig.getMemo())).orElse(defaultValue);
    }

    /**
     * 更新 操作軌跡保留天數
     */
    @Override
    public void updateLogKeepDays(String keepDays,String username) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                girConfigRepo.updateMemoByIdCode(keepDays, ScheduleParameter.KEEP_LOG_DAYS.getIdCode());
            }

            LogDto logDto =new LogDto();
            logDto.setFunctionName("systemsetting");
            logDto.setActionType("update");
            logDto.setInfo("修改成功: 操作軌跡保留天數=" + keepDays + " 由 user=" + username + "修改完成");
            logService.addLog(logDto, username);

        } catch (Exception e) {
            log.error("Error to updateLogKeepDays" + e.getMessage());
            throw e;
        }

    }

    /**
     * 取得 系統安全設定-密碼
     */
    public Map<String, char[]> getSysPwd() {
        List<GirConfigDB1Master> dataList = girConfigRepo.findAllByIDType(PasswordParameter.PWD.getIdType());
        Map<String, char[]> map = setSysMapOfList(dataList);
        map.put("overtime", overtime.replaceAll("[a-zA-Z]", "").toCharArray());

        return map;
    }

    public Map<String, char[]> setSysMapOfList(List<GirConfigDB1Master> dataList) {
        Map<String, char[]> map = new HashMap<>();
        for (GirConfigDB1Master data : dataList) {
            Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(data.getIDCode());
            // 因DB結構關係，特別處理idType(ex. pwdLenLimit, Y-3)
            if (girConfigOptional.isPresent()) {
                String idCode = girConfigOptional.get().getIDCode();
                String memo = girConfigOptional.get().getMemo();
                if (!memo.isEmpty()) {
                    if (memo.contains("-")) {
                        String checkBoxSuffix = "Flag";
                        String[] oneSplit = memo.split("-");
                        map.put(idCode + checkBoxSuffix, replaceColumnOfYN(oneSplit[0]).toCharArray());
                        map.put(idCode, oneSplit[1].toCharArray());
                    } else {
                        map.put(idCode, replaceColumnOfYN(memo).toCharArray());
                    }
                }
            }
        }
        return map;
    }


    public String replaceColumnOfYN(String column) {
        return StringUtils.equals(column, "Y") ? "true" : "false";
    }

    /**
     * 更新 系統安全設定-密碼
     */
    @Override
    public void updateSysPwd(UpdatePwdReqDto dto,String username) {

        try {

            Map<String, String> map = convertDtoToMap(dto);

            if (ToolPlugins.checkAuthorization("authorization")) {

                for (Map.Entry<String, String> entry : map.entrySet()) {
                    girConfigRepo.updateMemoByIdCode(entry.getValue(), entry.getKey());
                }

            }

            LogDto logDto =new LogDto();
            logDto.setFunctionName("systemsetting");
            logDto.setActionType("update");
            logDto.setInfo("修改成功: 系統安全設定-密碼" + dto + " 由 user=" + username + "修改完成");
            logService.addLog(logDto, username);

        } catch (Exception e) {
            log.error("Error to updateSysPwd" + e.getMessage());
            throw e;
        }

    }

    /**
     * 取得 系統安全設定-排程
     */
    public Map<String, char[]> getSchedule() {
        List<GirConfigDB1Master> dataList = girConfigRepo.findAllByIDType(ScheduleParameter.SCHEDULE.getIdType());

        return setSysMapOfList(dataList);
    }

    /**
     * 更新 系統安全設定-排程
     */
    @Override
    public void updateSchedule(UpdateScheduleReqDto dto, String username) {
        try {
            String toHistoryRecFlag = StringUtils.equals(dto.getGeneralRecToHistoryRecFlag(), "true") ? "Y" : "N";
            String toDeleteRecFlag = StringUtils.equals(dto.getHistoryRecToDeleteRecFlag(), "true") ? "Y" : "N";
            String deleteRecFlag = StringUtils.equals(dto.getDeleteRecFlag(), "true") ? "Y" : "N";
            girConfigRepo.updateMemoByIdCode(toHistoryRecFlag + "-" + dto.getGeneralRecToHistoryRec(),
                    ScheduleParameter.GENERAL_REC_TO_HISTORY_REC.getIdCode());
            girConfigRepo.updateMemoByIdCode(toDeleteRecFlag + "-" + dto.getHistoryRecToDeleteRec(),
                    ScheduleParameter.HISTORY_REC_TO_DELETE_REC.getIdCode());
            girConfigRepo.updateMemoByIdCode(deleteRecFlag + "-" + dto.getDeleteRec(),
                    ScheduleParameter.DELETE_REC.getIdCode());

            LogDto logDto =new LogDto();
            logDto.setFunctionName("systemsetting");
            logDto.setActionType("update");
            logDto.setInfo("修改成功: 系統安全設定-排程=" + dto + " 由 user=" + username + "修改完成");
            logService.addLog(logDto, username);
        } catch (Exception e) {
            log.error("Error to updateSchedule" + e.getMessage());
            throw e;
        }
    }

    /**
     * 取得 user location site ip list by siteIpCode
     */
    @Override
    public List<String> getUserSites(String siteIpCode) {
        try {
            return girConfigRepo.getUserSites(siteIpCode);
        } catch (Exception e) {
            log.error("Error to getUserSites" + e.getMessage());
            throw e;
        }
    }

    /**
     * List<String> convert to TreeMap
     */
    private TreeMap<String, String> convertListStringToMap(List<String> datalist) {
        TreeMap<String, String> rtMap = new TreeMap<String, String>();
        for (String s : datalist) {
            String[] parts = s.split(",");
            rtMap.put(HtmlEscapeUtils.escapeString(parts[0]), HtmlEscapeUtils.escapeString(parts[1]));
        }

        return rtMap;
    }

    /**
     * Dto convert to LinkedHashMap & 判斷 Chk是否為空
     */
    private Map<String, String> convertDtoToMap(UpdatePwdReqDto dto) {
        Map<String, String> map = new HashMap<>();
        map.put(PasswordParameter.PWD_ENG_NUM_FLAG.getIdCode(), dto.getPwdEngNumFlag());
        map.put(PasswordParameter.PWD_SAME_ACCOUNT_FLAG.getIdCode(), dto.getPwdSameAccountFlag());
        map.put(PasswordParameter.PWD_FIRST_LOGIN_FLAG.getIdCode(), dto.getPwdFirstLoginFlag());
        map.put(PasswordParameter.PWD_LEN_LIMIT.getIdCode(), prefixPlusContent(dto.getPwdLenLimitFlag(), dto.getPwdLenLimit()));
        map.put(PasswordParameter.PWD_WRONG_TIMES.getIdCode(), prefixPlusContent(dto.getPwdWrongTimesFlag(), dto.getPwdWrongTimes()));
        map.put(PasswordParameter.PWD_EXPIRATION_MODIFY_DAYS.getIdCode(),
                prefixPlusContent(dto.getPwdExpirationModifyDaysFlag(), dto.getPwdExpirationModifyDays()));
        map.put(PasswordParameter.PWD_MODIFY_REMINDER_DAYS.getIdCode(),
                prefixPlusContent(dto.getPwdModifyReminderDaysFlag(), dto.getPwdModifyReminderDays()));
        map.put(PasswordParameter.PWD_UNIQUE_IN_PREVIOUS.getIdCode(),
                prefixPlusContent(dto.getPwdUniqueInPreviousFlag(), dto.getPwdUniqueInPrevious()));

        return map;
    }

    public String prefixPlusContent(String prefix, String content) {
        return prefix + "-" + content;
    }
}