package com.ub.gir.web.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.rec.FileDateInfo;
import com.ub.gir.web.dto.rec.RecDto;
import com.ub.gir.web.dto.rec.SearchRecForm;
import com.ub.gir.web.entity.db1.master.RecListDB1Master;
import com.ub.gir.web.exception.ServiceException;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.RecListDB1MasterRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ub.gir.web.util.RocDateUtil.convertDateType;
import static com.ub.gir.web.util.RocDateUtil.convertStringDateToDate;


@Slf4j
@Service
@Transactional
public class RecService {

    @Autowired
    private SupAuditListDB1MasterRepository supAuditlistRepo;

    @Autowired
    private RecListDB1MasterRepository reclistRepo;

    @Autowired
    private LogService logService;

    @Autowired
    private SupAuditService supAuditService;

    @Autowired
    private KeepRecService keepRecService;

    @Resource(name = "db1MasterNamedJdbcTemplate")
    private NamedParameterJdbcTemplate jdbc;

    // 統計目前reclist Table裡的資料總筆數
    public Long count() {
        return reclistRepo.count();
    }

    public RecDto getRecByFilename(String filename, User user) {
        try {
            String jpql = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, '') as Dnis, IF(char_length(AgentID)>0, AgentID, '') as AgentID, \r\n" +
                    "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, '') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, '') as CallTypeName, \r\n" +
                    "IF(char_length(CustomerID)>0, CustomerID, '') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n" +
                    "IF(char_length(Location)>0, Location, '') as Location, IF(char_length(CallDir)>0, CallDir, '') as CallDir, \r\n" +
                    "UUID, IF(char_length(WorkID)>0, WorkID, '') as WorkID, AgentDN, IF(char_length(AD)>0, AD, '') as AD \r\n" +
                    "FROM reclist WHERE FileName= :filenameParam Order by StartDate DESC";
            List<RecDto> recDtoList = jdbc.query(jpql, new MapSqlParameterSource("filenameParam", filename),
                    new BeanPropertyRowMapper<>(RecDto.class));
            List<RecDto> searchReclist = parseAndEncodeReplayDtoList(recDtoList);
            LogDto logDto = new LogDto();
            logDto.setFunctionName("reclist");
            logDto.setActionType("Play");
            logDto.setInfo("播放檔名: " + filename);
            logService.addLog(logDto, user.getUsername());

            return searchReclist.get(0);
        } catch (Exception e) {
            log.error("Error to getRecByFilename : {}", e.getMessage(), e);
        }
        return new RecDto();
    }

    //查詢跨部門音檔
    public List<RecDto> searchRec(SearchRecForm searchrec, String username) {
        boolean isSupervisor = false;
        return convertStringToRServiceBean(queryRecList(searchrec, username), isSupervisor);
    }

    //查詢Supervisor 同部門supervisor的音檔+可Audit 的音檔
    public List<RecDto> searchSupRec(SearchRecForm searchrec, String username) {
        List<String> supAbleAgentIDList = supAuditService.supgetAbleAgentIDList(username);
        List<String> supAbleExtList = supAuditService.supgetAbleExtList(username);
        if (supAbleAgentIDList.isEmpty() && supAbleExtList.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecDto> searchResult = querySupervisorRecList(searchrec, supAbleAgentIDList, supAbleExtList, username);
        boolean isSupervisor = true;
        return convertStringToRServiceBean(searchResult, isSupervisor);
    }//-------------------------------------------------------------------------------------------------

    public List<RecDto> parseAndEncodeReplayDtoList(List<RecDto> datalist) {
        return datalist.stream().peek(dto -> {
            dto.setLengthhhmmss(dto.getLength());
            dto.setAni(HtmlEscapeUtils.escapeString(dto.getAni()));
            dto.setDnis(HtmlEscapeUtils.escapeString(dto.getDnis()));
            dto.setStartDate(HtmlEscapeUtils.escapeString(dto.getStartDate()));
            dto.setEndDate(HtmlEscapeUtils.escapeString(dto.getEndDate()));
        }).collect(Collectors.toList());
    }

    //共用功能 把List<String> 轉成List<UBGIR_UserService>
    public List<RecDto> convertStringToRServiceBean(List<RecDto> dtoList, boolean isSupervisor) {
        try {
            String isNA = "NA";
            String validFlag = "0";
            new RecDto().convertDto(dtoList).forEach(dto -> {
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
                            DateTimeUtil.isDateTimeBetween(fileStartDate, startDateByAbleExt, endDateByAbleExt)) {
                        dto.setCanPlay(true);
                        dto.setCanDownload(false);
                        dto.setCanKeepRec(false);
                        boolean isExtCanDL = StringUtils.equals(dto.getECanDL(), validFlag);
                        boolean isExtCanKeepRec = StringUtils.equals(dto.getESetrec(), validFlag);
                        boolean isAgentIdCanDl = StringUtils.equals(dto.getACanDL(), validFlag);
                        boolean isAgentIdCanKeepRec = StringUtils.equals(dto.getASetrec(), validFlag);
                        boolean isAgentIdEmpty = StringUtils.equalsIgnoreCase(agentId, isNA);
                        // download
                        if (isExtCanDL && (isAgentIdEmpty || isAgentIdCanDl)) {
                            dto.setCanDownload(true);
                        }
                        // 設定永久權限
                        if (isExtCanKeepRec && (isAgentIdEmpty || isAgentIdCanKeepRec)) {
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
    }

    private boolean hasAccessToPlayRec(String fileStartDate, Date userAccessStartTime, Date userAccessEndTime) {
        LocalDateTime fileLocalDate = LocalDateTime.parse(fileStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (userAccessStartTime==null && userAccessEndTime==null) {
            return true;
        }

        LocalDateTime userAccessStartDate = DateTimeUtil.dateToLocalDateTime(userAccessStartTime);
        LocalDateTime userAccessEndDate = DateTimeUtil.dateToLocalDateTime(userAccessEndTime);
        if (userAccessStartDate!=null && userAccessEndDate!=null) {
            return userAccessStartDate.isBefore(fileLocalDate) && userAccessEndDate.isAfter(fileLocalDate);
        } else if (userAccessStartDate==null) {
            return fileLocalDate.isBefore(userAccessEndDate);
        } else {
            return fileLocalDate.isAfter(userAccessStartDate);
        }
    }//-------------------------------------------------------------------------------------------------

    /**
     * 設定永久音檔
     */
    public void makeKeepRec(String connid, String username) throws IOException, ParseException {

        if (ToolPlugins.checkAuthorization("authorization")) {
            reclistRepo.setKeepRec(connid);
        }

        Optional<RecListDB1Master> recOptional = reclistRepo.findAllByconnId(connid);

        if (recOptional.isPresent()) {
            RecListDB1Master recListDB1Master = recOptional.get();
            Path baseFolder = Paths.get(FileSaveConfig.getWithinYearFolder()).toAbsolutePath().normalize();
            Path targetFolder = Paths.get(FileSaveConfig.getForeverSaveFolder()).toAbsolutePath().normalize();
            FileDateInfo fileDateInfo = getFileDateDtoByFileName(recListDB1Master.getFileName());

            String stringDateFolderName = fileDateInfo.getStringDateFolderName();
            String sanitizeFileName = FileUtil.sanitizePathTraversal(recListDB1Master.getFileName());
            Path sourceSanitizedSuffixPath = baseFolder.resolve(Paths.get(stringDateFolderName, sanitizeFileName)).normalize();
            Path targetSanitizedSuffixPath = targetFolder.resolve(Paths.get(stringDateFolderName, sanitizeFileName)).normalize();
            if(!Files.exists(sourceSanitizedSuffixPath) ){
                throw new ServiceException("File Not Found");
            }
            FileUtil.copyFile(sourceSanitizedSuffixPath.toString(),
                    targetSanitizedSuffixPath.toString());
        }

        executeInsertKeepList(connid);
        LogDto logDto = new LogDto();
        logDto.setFunctionName("reclist");
        logDto.setActionType("keep");
        logDto.setInfo("設定永久音檔: 音檔ConnID= " + connid + " updated by user=" + username);
        logService.addLog(logDto, username);
    }

    //查出歷史音檔
    private List<RecDto> queryRecList(SearchRecForm searchRec, String username) {

        try {
            String jpql = "Select r.ID, r.ConnID, r.Ani, r.StartDate, r.EndDate, r.FileName, r.UUID, r.AgentDN, " +
                    "IF(char_length(r.Dnis) > 0, r.Dnis, '') as Dnis, " +
                    "IF(char_length(r.AgentID) > 0, r.AgentID,'') as AgentID, " +
                    "IF(char_length(r.CallType) > 0, r.CallType, '') as CallType, " +
                    "IF(char_length(r.CallTypeName) >0, r.CallTypeName, '') as CallTypeName, " +
                    "IF(char_length(r.CustomerID) > 0, r.CustomerID, '') as CustomerID, " +
                    "IF(char_length(r.Length) > 0, r.Length, 0) as Length, " +
                    "IF(char_length(r.Location) > 0, r.Location, '') as Location, " +
                    "IF(char_length(r.CallDir) > 0, r.CallDir, '') as CallDir, " +
                    "IF(char_length(r.WorkID) > 0, r.WorkID, '') as WorkID, " +
                    "IF(char_length(r.AD) > 0, r.AD, '') as AD, " +
                    "sa1.CanDL as ECanDL, sa1.Setrec as ESetrec, " +
                    "sa1.AuditStartDate as AuditStartDateByAbleExt, sa1.AuditEndDate as AuditEndDateByAbleExt, " +
                    "sa2.CanDL as ACanDL, sa2.Setrec as ASetrec, " +
                    "sa2.AuditStartDate as AuditStartDateByAbleAgentID, sa2.AuditEndDate as AuditEndDateByAbleAgentID, " +
                    "CASE WHEN kr.ConnID IS NOT NULL THEN 1 ELSE 0 END as KeepRec " +
                    "FROM reclist r " +
                    "LEFT JOIN sup_auditlist sa1 ON sa1.UserName = :username and sa1.ableExt = r.AgentDN  " +
                    "LEFT JOIN sup_auditlist sa2 ON sa2.UserName = :username and sa2.ableAgentID = r.AgentID  " +
                    "LEFT JOIN keepreclist kr ON kr.ConnID = r.ConnID " +
                    "WHERE r.StartDate >= :startDate AND r.StartDate <= :endDate ";
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("startDate", DateTimeUtil.dateStrToDate(searchRec.getStartDate()));
            parameters.put("endDate", DateTimeUtil.dateStrToDate(searchRec.getEndDate()));

            //做ALL的資料矯正
            if (searchRec.getCallDir().equalsIgnoreCase("ALL")) {
                searchRec.setCallDir("");
            }
            if (StringUtils.isNotBlank(searchRec.getCustomerId())) {
                jpql += "and r.CustomerID = :customerID ";
                parameters.put("customerID", searchRec.getCustomerId());
            }
            if (StringUtils.isNotBlank(searchRec.getAni())) {
                jpql += "and (r.Ani like :ani or r.Dnis like :ani) ";
                parameters.put("ani", "%" + searchRec.getAni() + "%");
            }
            if (StringUtils.isNotBlank(searchRec.getAgentId())) {
                jpql += "and r.AgentID = :agentID ";
                parameters.put("agentID", searchRec.getAgentId());
            }
            if (StringUtils.isNotBlank(searchRec.getAgentDN())) {
                jpql += "and r.AgentDN = :agentDN ";
                parameters.put("agentDN", searchRec.getAgentDN());
            }
            if (StringUtils.isNotBlank(searchRec.getCallDir())) {
                jpql += "and r.CallDir = :callDir ";
                parameters.put("callDir", searchRec.getCallDir());
            }
            if (StringUtils.isNotBlank(searchRec.getCallTypeName())) {
                jpql += "and r.CallTypeName like :callTypeName ";
                parameters.put("callTypeName", "%" + searchRec.getCallTypeName() + "%");
            }

            if (ToolPlugins.checkAuthorization("authorization")) {
                return escapeData(jdbc.query(jpql, parameters, new BeanPropertyRowMapper<>(RecDto.class)));
            }

            return null;

        } catch (Exception e) {
            log.error("Error queryRecList : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<RecDto> querySupervisorRecList(SearchRecForm searchRec, List<String> ableAgentIDList,
                                                List<String> ableExtList, String username) {
        try {
            String jpql = "SELECT * FROM (Select r.ID, r.ConnID, r.Ani, r.StartDate, r.EndDate, r.FileName, r.UUID, r.AgentDN, " +
                    "IF(char_length(r.Dnis) > 0, r.Dnis, '') as Dnis, " +
                    "IF(char_length(r.AgentID) > 0, r.AgentID,'') as AgentID, " +
                    "IF(char_length(r.CallType) > 0, r.CallType, '') as CallType, " +
                    "IF(char_length(r.CallTypeName) >0, r.CallTypeName, '') as CallTypeName, " +
                    "IF(char_length(r.CustomerID) > 0, r.CustomerID, '') as CustomerID, " +
                    "IF(char_length(r.Length) > 0, r.Length, 0) as Length, " +
                    "IF(char_length(r.Location) > 0, r.Location, '') as Location, " +
                    "IF(char_length(r.CallDir) > 0, r.CallDir, '') as CallDir, " +
                    "IF(char_length(r.WorkID) > 0, r.WorkID, '') as WorkID, " +
                    "IF(char_length(r.AD) > 0, r.AD, '') as AD, " +
                    "sa1.CanDL as ECanDL, sa1.Setrec as ESetrec, " +
                    "sa1.AuditStartDate as AuditStartDateByAbleExt, sa1.AuditEndDate as AuditEndDateByAbleExt, " +
                    "sa2.CanDL as ACanDL, sa2.Setrec as ASetrec, " +
                    "sa2.AuditStartDate as AuditStartDateByAbleAgentID, sa2.AuditEndDate as AuditEndDateByAbleAgentID, " +
                    "CASE WHEN kr.ConnID IS NOT NULL THEN 1 ELSE 0 END as KeepRec " +
                    "FROM reclist r " +
                    "LEFT JOIN sup_auditlist sa1 ON sa1.UserName = :username and sa1.ableExt = r.AgentDN  " +
                    "LEFT JOIN sup_auditlist sa2 ON sa2.UserName = :username and sa2.ableAgentID = r.AgentID   " +
                    "LEFT JOIN keepreclist kr ON kr.ConnID = r.ConnID " +
                    "WHERE r.StartDate >= :startDate AND r.StartDate <= :endDate ";

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("startDate", DateTimeUtil.dateStrToDate(searchRec.getStartDate()));
            parameters.put("endDate", DateTimeUtil.dateStrToDate(searchRec.getEndDate()));

            //做ALL的資料矯正
            if (searchRec.getCallDir().equalsIgnoreCase("ALL")) {
                searchRec.setCallDir("");
            }
            if (StringUtils.isNotBlank(searchRec.getCustomerId())) {
                jpql += "and r.CustomerID = :customerID ";
                parameters.put("customerID", searchRec.getCustomerId());
            }
            if (StringUtils.isNotBlank(searchRec.getAni())) {
                jpql += "and (r.Ani like :ani or r.Dnis like :ani) ";
                parameters.put("ani", "%" + searchRec.getAni() + "%");
            }
            if (StringUtils.isNotBlank(searchRec.getAgentId())) {
                jpql += "and r.AgentID = :agentID ";
                parameters.put("agentID", searchRec.getAgentId());
            }
            if (StringUtils.isNotBlank(searchRec.getAgentDN())) {
                jpql += "and r.AgentDN = :agentDN ";
                parameters.put("agentDN", searchRec.getAgentDN());
            }
            if (StringUtils.isNotBlank(searchRec.getCallDir())) {
                jpql += "and r.CallDir = :callDir ";
                parameters.put("callDir", searchRec.getCallDir());
            }
            if (StringUtils.isNotBlank(searchRec.getCallTypeName())) {
                jpql += "and r.CallTypeName like :callTypeName ";
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
                return escapeData(jdbc.query(jpql, parameters, new BeanPropertyRowMapper<>(RecDto.class)));
            }

            return null;

        } catch (Exception e) {
            log.error("Error querySupervisorRecList : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * 將音檔寫入DB
     */
    public void executeInsertKeepList(String connid) {
        try {
            StringBuilder selectSql = new StringBuilder();
            selectSql.append("SELECT connId, ani, dnis, agentId, startDate, fileName, ")
                    .append("callType, callTypeName, customerId, ")
                    .append("length, location, callDir, uuid, workId, agentDn, ad ")
                    .append("FROM ")
                    .append("reclist ")
                    .append(" WHERE ConnID = :connId");

            StringBuilder insertSql = new StringBuilder();
            insertSql.append("INSERT INTO keepreclist (ConnID, Ani, Dnis, AgentID, StartDate, ")
                    .append("FileName, CallType, CallTypeName, CustomerID, Length, Location, ")
                    .append("CallDir, UUID, WorkID, AgentDN, AD) ")
                    .append("VALUES (:connId, :ani, :dnis, :agentId, :startDate, :fileName, :callType, :callTypeName, ")
                    .append(":customerId, :length, :location, :callDir, :uuid, :workId, :agentDn, :ad)");

            MapSqlParameterSource queryParams = new MapSqlParameterSource("connId", connid.trim());
            if (ToolPlugins.checkAuthorization("authorization")) {
                Map<String, Object> queryResult = jdbc.queryForMap(selectSql.toString(), queryParams);
                jdbc.update(insertSql.toString(), new MapSqlParameterSource(queryResult));
            }
        } catch (Exception e) {
            log.error("Error insert into keepRec db : {}", e.getMessage(), e);
        }
    }

    public <T> List<T> escapeData(List<T> list) {
        return list;
    }

    public FileDateInfo getFileDateDtoByFileName(String fileName) throws ParseException {
        String[] fileNameArray = fileName.split("_");
        String fileDateYYYYMMDD = fileNameArray[1];
        String fileDate = fileDateYYYYMMDD + " " + fileNameArray[2].replaceAll("-", ":");
        Date realFileDate = convertStringDateToDate(fileDate, "yyyy-MM-dd HH:mm:ss", false);
        String stringDateFolderName = fileDateYYYYMMDD.replaceAll("-", "");

        return new FileDateInfo(realFileDate,stringDateFolderName);
    }
}