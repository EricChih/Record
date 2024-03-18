package com.ub.gir.web.service;


import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.configuration.security.SecurityHelper;
import com.ub.gir.web.dto.keeprec.DateParamDto;
import com.ub.gir.web.dto.keeprec.KeepRecDto;
import com.ub.gir.web.dto.keeprec.SearchKeepRecForm;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.rec.FileDateInfo;
import com.ub.gir.web.entity.db1.master.DepDB1Master;
import com.ub.gir.web.entity.db1.master.KeepRecListDB1Master;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.DepDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.KeepRecListDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.SupAuditListDB1MasterRepository;
import com.ub.gir.web.util.DateTimeUtil;
import com.ub.gir.web.util.FileUtil;
import com.ub.gir.web.util.HtmlEscapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.ub.gir.web.util.DateTimeUtil.addYear;
import static com.ub.gir.web.util.DateTimeUtil.getYearListBetweenDates;
import static com.ub.gir.web.util.RocDateUtil.convertDateType;


@Slf4j
@Service
@Transactional
public class KeepRecService {
    @Autowired
    private KeepRecListDB1MasterRepository keepreclistRepo;
    @Autowired
    private LogService logService;
    @Autowired
    private UserService userService;
    @Autowired
    private SupAuditService supAuditService;
    @Resource
    private RecService recService;

    //default listing maxsize
    @Resource
    private DepDB1MasterRepository depRepository;

    @Resource(name="db1MasterNamedJdbcTemplate")
    private NamedParameterJdbcTemplate jdbc;

    // 統計目前reclist Table裡的資料總筆數
    public Long count() {
        return keepreclistRepo.count();
    }//-------------------------------------------------------------------------------------------------

    // 確認ConnID 是否已經被Keep為永久音檔, >=1 是永久音檔, 0=無設定
    public int chkKeepConnIDExist(String connid) {
        try {
            return keepreclistRepo.chkKeepConnIDExist(connid);
        } catch (Exception e) {
            log.error("Error to chkKeepConnIDExist" + e.getMessage());
            throw e;
        }
    }//-------------------------------------------------------------------------------------------------


    public KeepRecDto getRecByFilename(String filename, User user) {
        try {
            String jpql = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, 'NA') as Dnis, IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID, \r\n"
                    + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, 'NA') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, 'NA') as CallTypeName, \r\n"
                    + "IF(char_length(CustomerID)>0, CustomerID, 'NA') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
                    + "IF(char_length(Location)>0, Location, 'NA') as Location, IF(char_length(CallDir)>0, CallDir, 'NA') as CallDir, \r\n"
                    + "UUID, IF(char_length(WorkID)>0, WorkID, 'NA') as WorkID, AgentDN, IF(char_length(AD)>0, AD, 'NA') as AD \r\n"
                    + "FROM keepreclist WHERE FileName= :filenameParam Order by StartDate DESC";
            List<KeepRecDto> hisRecResDtoList = jdbc.query(jpql, new MapSqlParameterSource("filenameParam", filename), new BeanPropertyRowMapper<>(KeepRecDto.class));
            List<KeepRecDto> searchReclist = parseAndEncodeReplayDtoList(hisRecResDtoList);
            LogDto logDto =new LogDto();
            logDto.setFunctionName("keepreclist");
            logDto.setActionType("Play");
            logDto.setInfo("播放檔名: " + filename);
            logService.addLog(logDto, user.getUsername());
            return searchReclist.get(0);
        }catch (Exception e) {
            log.error("Error goRecByFilenameQuery : {}", e.getMessage(), e);
        }
        return new KeepRecDto();
    }//-------------------------------------------------------------------------------------------------

    public List<KeepRecDto> searchRec(SearchKeepRecForm searchrec, String username) {
        return parseAndEncodeDtoList(queryKeepRecList(searchrec, username), false);
    }

    private List<KeepRecDto> queryKeepRecList(SearchKeepRecForm searchRec, String username) {
        try {
            String jpql = "Select k.ID, k.ConnID, k.Ani, k.StartDate, k.EndDate, k.FileName, k.UUID, k.AgentDN, " +
                    "IF(char_length(k.Dnis)>0, k.Dnis, '') as Dnis, " +
                    "IF(char_length(k.AgentID)>0, k.AgentID, '') as AgentID, " +
                    "IF(char_length(k.CallType)>0, k.CallType, '') as CallType, " +
                    "IF(char_length(k.CallTypeName)>0, k.CallTypeName, '') as CallTypeName, " +
                    "IF(char_length(k.CustomerID)>0, k.CustomerID, '') as CustomerID, " +
                    "IF(char_length(k.Length)>0, k.Length, 0) as Length, " +
                    "IF(char_length(k.Location)>0, k.Location, '') as Location, " +
                    "IF(char_length(k.CallDir)>0, k.CallDir, '') as CallDir, " +
                    "IF(char_length(k.WorkID)>0, k.WorkID, '') as WorkID, " +
                    "IF(char_length(k.AD)>0, k.AD, '') as AD, " +
                    "sa1.CanDL as ECanDL, sa1.Setrec as ESetrec, " +
                    "sa1.AuditStartDate as AuditStartDateByAbleExt, sa1.AuditEndDate as AuditEndDateByAbleExt, " +
                    "sa2.CanDL as ACanDL, sa2.Setrec as ASetrec, " +
                    "sa2.AuditStartDate as AuditStartDateByAbleAgentID, sa2.AuditEndDate as AuditEndDateByAbleAgentID, " +
                    "CASE WHEN kr.ConnID IS NOT NULL THEN 1 ELSE 0 END as KeepRec " +
                    "FROM keepreclist k " +
                    "LEFT JOIN sup_auditlist sa1 ON sa1.UserName = :username and sa1.ableExt = k.AgentDN  " +
                    "LEFT JOIN sup_auditlist sa2 ON sa2.UserName = :username and sa2.ableAgentID = k.AgentID  " +
                    "LEFT JOIN keepreclist kr ON kr.ConnID = k.ConnID " +
                    "WHERE k.StartDate >= :startDate AND k.StartDate <= :endDate ";

            DateParamDto dateParamDto = obtainStartAndEndDate(searchRec.getTheYear(), searchRec.getTheMonth());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("startDate", dateParamDto.getStartDate());
            parameters.put("endDate", dateParamDto.getEndDate());

            //做ALL的資料矯正
            if(searchRec.getCallDir().equalsIgnoreCase("ALL")) {
                searchRec.setCallDir("");
            }
            if(StringUtils.isNotBlank(searchRec.getCustomerId()) ) {
                jpql += "and k.CustomerID = :customerID ";
                parameters.put("customerID", searchRec.getCustomerId());
            }
            if (StringUtils.isNotBlank(searchRec.getAni())) {
                jpql += "and (k.Ani like :ani or k.Dnis like :ani) ";
                parameters.put("ani", "%" + searchRec.getAni() + "%");
            }
            if(StringUtils.isNotBlank(searchRec.getAgentId())) {
                jpql += "and k.AgentID = :agentID ";
                parameters.put("agentID", searchRec.getAgentId());
            }
            if(StringUtils.isNotBlank(searchRec.getAgentDN())) {
                jpql += "and k.AgentDN = :agentDN ";
                parameters.put("agentDN", searchRec.getAgentDN());
            }
            if(StringUtils.isNotBlank(searchRec.getCallDir())) {
                jpql += "and k.CallDir = :callDir ";
                parameters.put("callDir", searchRec.getCallDir());
            }
            if(StringUtils.isNotBlank(searchRec.getCallTypeName())) {
                jpql += "and k.CallTypeName like :callTypeName ";
                parameters.put("callTypeName", "%" + searchRec.getCallTypeName() + "%");
            }

            if (ToolPlugins.checkAuthorization("authorization")) {
                return jdbc.query(jpql, parameters, new BeanPropertyRowMapper<>(KeepRecDto.class));
            }

            return null;

        } catch (Exception e) {
            log.error("Error queryKeepRecList : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<KeepRecDto> querySupervisorKeepRecList(SearchKeepRecForm searchRec, List<String> ableAgentIDList,
                                                      List<String> ableExtList, String username) {
        try {
            String jpql = "SELECT * FROM (Select k.ID, k.ConnID, k.Ani, k.StartDate, k.EndDate, k.FileName, k.UUID, k.AgentDN, " +
                    "IF(char_length(k.Dnis)>0, k.Dnis, '') as Dnis, " +
                    "IF(char_length(k.AgentID)>0, k.AgentID, '') as AgentID, " +
                    "IF(char_length(k.CallType)>0, k.CallType, '') as CallType, " +
                    "IF(char_length(k.CallTypeName)>0, k.CallTypeName, '') as CallTypeName, " +
                    "IF(char_length(k.CustomerID)>0, k.CustomerID, '') as CustomerID, " +
                    "IF(char_length(k.Length)>0, k.Length, 0) as Length, " +
                    "IF(char_length(k.Location)>0, k.Location, '') as Location, " +
                    "IF(char_length(k.CallDir)>0, k.CallDir, '') as CallDir, " +
                    "IF(char_length(k.WorkID)>0, k.WorkID, '') as WorkID, " +
                    "IF(char_length(k.AD)>0, k.AD, '') as AD, " +
                    "sa1.CanDL as ECanDL, sa1.Setrec as ESetrec, " +
                    "sa1.AuditStartDate as AuditStartDateByAbleExt, sa1.AuditEndDate as AuditEndDateByAbleExt, " +
                    "sa2.CanDL as ACanDL, sa2.Setrec as ASetrec, " +
                    "sa2.AuditStartDate as AuditStartDateByAbleAgentID, sa2.AuditEndDate as AuditEndDateByAbleAgentID, " +
                    "CASE WHEN kr.ConnID IS NOT NULL THEN 1 ELSE 0 END as KeepRec " +
                    "FROM keepreclist k " +
                    "LEFT JOIN sup_auditlist sa1 ON sa1.UserName = :username and sa1.ableExt = k.AgentDN  " +
                    "LEFT JOIN sup_auditlist sa2 ON sa2.UserName = :username and sa2.ableAgentID = k.AgentID  " +
                    "LEFT JOIN keepreclist kr ON kr.ConnID = k.ConnID " +
                    "WHERE k.StartDate >= :startDate AND k.StartDate <= :endDate ";

            DateParamDto dateParamDto = obtainStartAndEndDate(searchRec.getTheYear(), searchRec.getTheMonth());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("startDate", dateParamDto.getStartDate());
            parameters.put("endDate", dateParamDto.getEndDate());

            //做ALL的資料矯正
            if(searchRec.getCallDir().equalsIgnoreCase("ALL")) {
                searchRec.setCallDir("");
            }
            if(StringUtils.isNotBlank(searchRec.getCustomerId()) ) {
                jpql += "and k.CustomerID = :customerID ";
                parameters.put("customerID", searchRec.getCustomerId());
            }
            if (StringUtils.isNotBlank(searchRec.getAni())) {
                jpql += "and (k.Ani like :ani or k.Dnis like :ani) ";
                parameters.put("ani", "%" + searchRec.getAni() + "%");
            }
            if(StringUtils.isNotBlank(searchRec.getAgentId())) {
                jpql += "and k.AgentID = :agentID ";
                parameters.put("agentID", searchRec.getAgentId());
            }
            if(StringUtils.isNotBlank(searchRec.getAgentDN())) {
                jpql += "and k.AgentDN = :agentDN ";
                parameters.put("agentDN", searchRec.getAgentDN());
            }
            if(StringUtils.isNotBlank(searchRec.getCallDir())) {
                jpql += "and k.CallDir = :callDir ";
                parameters.put("callDir", searchRec.getCallDir());
            }
            if(StringUtils.isNotBlank(searchRec.getCallTypeName())) {
                jpql += "and k.CallTypeName like :callTypeName ";
                parameters.put("callTypeName", "%" + searchRec.getCallTypeName() + "%");
            }
            jpql += ") t WHERE ";

            if (!ableAgentIDList.isEmpty()) {
                jpql += " t.AgentID in (:ableAgentIDList) ";
                parameters.put("ableAgentIDList", ableAgentIDList);
            }
            if (!ableExtList.isEmpty()) {
                if (!ableAgentIDList.isEmpty()) {
                    jpql += " OR ";
                }
                jpql += " t.AgentDN in (:ableExtList) ";
                parameters.put("ableExtList", ableExtList);
            }

            if (ToolPlugins.checkAuthorization("authorization")) {
                return jdbc.query(jpql, parameters, new BeanPropertyRowMapper<>(KeepRecDto.class));
            }

            return null;

        } catch (Exception e) {
            log.error("Error querySupervisorKeepRecList : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    //查詢Supervisor 自己部門的永久音檔
    public List<KeepRecDto> searchSupRec(SearchKeepRecForm searchrec, String username) {
        //要先確定是否有調聽權限用 udepid check ani, dnis, agentdn
        //supervisor 讀部門的AgentIDList+ ExtList
        List<String> supAbleAgentIDList = supAuditService.supgetAbleAgentIDList(username); //讀部門的AgentIDList
        List<String> supAbleExtList = supAuditService.supgetAbleExtList(username); //讀部門的ExtList

        if (supAbleAgentIDList.isEmpty() && supAbleExtList.isEmpty()) {
            return Collections.emptyList();
        }

        return parseAndEncodeDtoList(querySupervisorKeepRecList(searchrec, supAbleAgentIDList,
                supAbleExtList, username), true);
    }//-------------------------------------------------------------------------------------------------

    private DateParamDto obtainStartAndEndDate(String yearStr, String month){
        int year = Integer.parseInt(yearStr);
        String startDay = "01";
        String endDay = "31";
        String startDate;
        String endDate;
        if(StringUtils.isNotBlank(month)){
            endDay = String.valueOf(DateTimeUtil.getDayOfMonth(year, Integer.parseInt(month)));
            startDate = year + "-" + month + "-" + startDay + " 00:00:00";
            endDate = year + "-" + month + "-" + endDay + " 23:59:59";
        }else {
            startDate = year + "-01-" + startDay + " 00:00:00";
            endDate = year + "-12-" + endDay + " 23:59:59";
        }

        return DateParamDto
                .builder()
                .startDate(DateTimeUtil.dateStrToDate(startDate))
                .endDate(DateTimeUtil.dateStrToDate(endDate))
                .build();
    }

    //共用功能 把List<String> 轉成List<UBGIR_UserService>
    private List<KeepRecDto> parseAndEncodeDtoList(List<KeepRecDto> dtoList, boolean isSupervisor) {
        try{
            String isNA = "NA";
            String validFlag = "0";
            new KeepRecDto().convertDto(dtoList).forEach(dto -> {
                String agentId = dto.getAgentID();

                if (!isSupervisor) {
                    dto.setCanPlay(true);
                    dto.setCanDownload(true);
                    dto.setCanKeepRec(true);
                } else {
                    String fileStartDate = dto.getStartDate();
                    Date startDateByAbleAgentID = dto.getAuditStartDateByAbleAgentID();
                    Date endDateByAbleAgentID = dto.getAuditEndDateByAbleAgentID();
                    Date startDateByAbleExt = dto.getAuditStartDateByAbleExt();
                    Date endDateByAbleExt = dto.getAuditEndDateByAbleExt();
                    if (DateTimeUtil.isDateTimeBetween(fileStartDate, startDateByAbleAgentID, endDateByAbleAgentID) &&
                            DateTimeUtil.isDateTimeBetween(fileStartDate, startDateByAbleExt, endDateByAbleExt)){
                        dto.setCanPlay(true);
                        dto.setCanDownload(false);
                        dto.setCanKeepRec(false);
                        boolean isExtCanDL = StringUtils.equals(dto.getECanDL(), validFlag);
                        boolean isExtCanKeepRec = StringUtils.equals(dto.getESetrec(), validFlag);
                        boolean isAgentIdCanDl = StringUtils.equals(dto.getACanDL(), validFlag);
                        boolean isAgentIdCanKeepRec = StringUtils.equals(dto.getASetrec(), validFlag);
                        boolean isAgentIdEmpty = StringUtils.equalsIgnoreCase(agentId, isNA);
                        // download
                        if(isExtCanDL && (isAgentIdEmpty || isAgentIdCanDl)) {
                            dto.setCanDownload(true);
                        }
                        // 設定永久權限
                        if(isExtCanKeepRec && (isAgentIdEmpty || isAgentIdCanKeepRec)) {
                            dto.setCanKeepRec(true);
                        }
                    } else {
                        // 如果可調聽的時間都沒有包含，則不能播放、下載、設定永久
                        dto.setCanPlay(false);
                        dto.setCanDownload(false);
                        dto.setCanKeepRec(false);
                    }
                }
            });

            return dtoList;
        } catch (Exception e) {
            log.error("Error to convertStringToRServiceBean");
            throw e;
        }
    }//-------------------------------------------------------------------------------------------------

    private boolean hasAccessToPlayRec(String fileStartDate, Date userAccessStartTime, Date userAccessEndTime){
        LocalDateTime fileLocalDate = LocalDateTime.parse(fileStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (userAccessStartTime == null && userAccessEndTime == null) {
            return true;
        }
        LocalDateTime userAccessStartDate = DateTimeUtil.dateToLocalDateTime(userAccessStartTime);
        LocalDateTime userAccessEndDate = DateTimeUtil.dateToLocalDateTime(userAccessEndTime);
        if (userAccessStartDate != null && userAccessEndDate != null) {
            return userAccessStartDate.isBefore(fileLocalDate) && userAccessEndDate.isAfter(fileLocalDate);
        }
        if (userAccessStartDate == null) {
            return fileLocalDate.isBefore(userAccessEndDate);
        }else {
            return fileLocalDate.isAfter(userAccessStartDate);
        }
    }

    private List<KeepRecDto> parseAndEncodeReplayDtoList(List<KeepRecDto> datalist) {
        return datalist.stream().peek(dto -> {
            dto.setLengthhhmmss(dto.getLength());
            dto.setAni(HtmlEscapeUtils.escapeString(dto.getAni()));
            dto.setDnis(HtmlEscapeUtils.escapeString(dto.getDnis()));
            dto.setStartDate(HtmlEscapeUtils.escapeString(dto.getStartDate()));
            dto.setEndDate(HtmlEscapeUtils.escapeString(dto.getEndDate()));
        }).collect(Collectors.toList());
    }

    // delete KeepRec by given connID
    public void delKeepRec(String connID, String keepRecDate, String username) throws ParseException, IOException {
        removeForeverRec(keepRecDate,connID);
        if (ToolPlugins.checkAuthorization("authorization")) {
            keepreclistRepo.delKeepRec(connID);
        }

        LogDto logDto =new LogDto();
        logDto.setFunctionName("keepreclist");
        logDto.setActionType("nokeep");
        logDto.setInfo("解除永久音檔: 音檔ConnID= " + connID + " deleted by user=" + username);
        logService.addLog(logDto, username);
    }

    /**
     * 永久音檔 設定解除永久(保內刪除 or 保外 搬移delRec)
     *
     * @throws ParseException, IOException
     */
    public void removeForeverRec(String recDate, String connId) throws ParseException, IOException {
        UserDetails userDetails = (UserDetails) SecurityHelper.getPrincipal();
        String depId = userService.getDepIDByUsername(userDetails.getUsername());

        Optional<DepDB1Master> depOptional = depRepository.findByDepId(depId);
        if (depOptional.isPresent()) {
            String recYear = recDate.split("-")[0];
            int recKeepYear = depOptional.get().getRecKeepYear();
            //預設最長刪除7年，最少刪除n-1年(ex.以2023年為例，0-1=2022)
            Date defaultDueDate = DateTimeUtil.addDays(addYear(new Date(), -7), -1);
            Date expirationBaseDate = DateTimeUtil.addDays(addYear(new Date(), -recKeepYear - 1), -1);
            List<Integer> yearListBetweenDates = getYearListBetweenDates(defaultDueDate, expirationBaseDate);
            Optional<KeepRecListDB1Master> recOptional = keepreclistRepo.findAllByConnId(connId);
            if (recOptional.isPresent()) {
                KeepRecListDB1Master keepRecListDB1Master = recOptional.get();
                Path baseFolder = Paths.get(FileSaveConfig.getForeverSaveFolder()).toAbsolutePath().normalize();
                Path targetFolder = Paths.get(FileSaveConfig.getOverdueDeletedFolder()).toAbsolutePath().normalize();

                FileDateInfo fileDateInfo = recService.getFileDateDtoByFileName(keepRecListDB1Master.getFileName());
                String stringDateFolderName = fileDateInfo.getStringDateFolderName();
                String sanitizeFileName = FileUtil.sanitizePathTraversal(keepRecListDB1Master.getFileName());
                Path sanitizedSuffixPath = baseFolder.resolve(Paths.get(stringDateFolderName, sanitizeFileName)).normalize();

                if (!sanitizedSuffixPath.startsWith(baseFolder)) {
                    throw new SecurityException("Invalid file path, possible path traversal attempt.");
                }

                if (yearListBetweenDates.contains(Integer.valueOf(recYear))) {
                    Path targetPath = targetFolder.resolve(Paths.get(stringDateFolderName, sanitizeFileName)).normalize();
                    FileUtil.moveFile(sanitizedSuffixPath.toString(), targetPath.toString());
                } else {
                    FileUtil.deleteFile(sanitizedSuffixPath.toString());
                }
            }
        }
    }
}