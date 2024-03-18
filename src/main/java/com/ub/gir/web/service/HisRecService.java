package com.ub.gir.web.service;


import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.dto.hisRec.HisRecDto;
import com.ub.gir.web.dto.hisRec.ScheduledHisRecDto;
import com.ub.gir.web.dto.hisRec.SearchHisRecForm;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.rec.FileDateInfo;
import com.ub.gir.web.exception.ServiceException;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.util.DateTimeUtil;
import com.ub.gir.web.util.FileUtil;
import com.ub.gir.web.util.HtmlEscapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Year;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ub.gir.web.util.RocDateUtil.convertDateType;


@Slf4j
@Service
@Transactional
public class HisRecService {
    @Autowired
    private SupAuditService supAuditService;
    @Autowired
    private LogService logService;
    @Resource
    private RecService recService;
    @Resource(name = "db1MasterNamedJdbcTemplate")
    private NamedParameterJdbcTemplate jdbc;

    @Value("${ub.datasource.db-1.master.database}")
    private String database;

    //依造檔名做歷史音檔資訊的撈取
    public HisRecDto getRecByFilename(String filename, String filterDate, String username, String location) {
        String hisTablePrefix = filterDate.substring(0, 4);
        String hisTableName = getTableNameByRequest(hisTablePrefix);
        String encodeTableName = HtmlEscapeUtils.escapeString(hisTableName);
        return goRecByFilenameQuery(filename, encodeTableName, username);
    }//-------------------------------------------------------------------------------------------------


    //共用功能 用input filename and recdate 動態組出撈歷史音檔的 select sql string
    private HisRecDto goRecByFilenameQuery(String filename, String encodeTableName, String username) {
        try {
            String jpql = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, '') as Dnis, IF(char_length(AgentID)>0, AgentID, '') as AgentID, \r\n"
                    + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, '') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, '') as CallTypeName,\r\n"
                    + "IF(char_length(CustomerID)>0, CustomerID, '') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
                    + "IF(char_length(Location)>0, Location, '') as Location, IF(char_length(CallDir)>0, CallDir, '') as CallDir, \r\n"
                    + "UUID, IF(char_length(WorkID)>0, WorkID, '') as WorkID, AgentDN, IF(char_length(AD)>0, AD, '') as AD \r\n"
                    + "FROM " + encodeTableName + " WHERE FileName =:filenameParam";
            String encodeFileName = HtmlEscapeUtils.escapeString(filename);
            List<HisRecDto> hisRecDtoList = jdbc.query(jpql, new MapSqlParameterSource("filenameParam", encodeFileName), new BeanPropertyRowMapper<>(HisRecDto.class));
            List<HisRecDto> searchReclist = parseAndEncodeHisRecDto(hisRecDtoList);

            LogDto logDto = new LogDto();
            logDto.setFunctionName("hisreclist");
            logDto.setActionType("Play");
            logDto.setInfo("播放檔名: " + encodeFileName);
            logService.addLog(logDto, username);

            return searchReclist.get(0);
        } catch (Exception e) {
            log.error("Error goRecByFilenameQuery : {}", e.getMessage(), e);
        }
        return new HisRecDto();
    }

    //查詢跨部門歷史音檔搜尋
    public List<HisRecDto> searchRec(SearchHisRecForm searchrec, String username, String location) {
        String hisTableName = getTableNameByRequest(searchrec.getTheYear());
        List<HisRecDto> searchResult = queryHisRecList(searchrec, hisTableName, username);
        boolean isSupervisor = false;
        return convertStringToRServiceBean(searchResult, isSupervisor);
    }//-------------------------------------------------------------------------------------------------

    public List<HisRecDto> searchSupRec(SearchHisRecForm searchrec, String username, String location) {
        //撈supervisor 可調聽的AgentIDList
        List<String> supAbleAgentIDList = supAuditService.supgetAbleAgentIDList(username);
        //撈supervisor 可調聽的Ext List
        List<String> supAbleExtList = supAuditService.supgetAbleExtList(username);

        if (supAbleAgentIDList.isEmpty() && supAbleExtList.isEmpty()) {
            return Collections.emptyList();
        }
        String hisTableName = getTableNameByRequest(searchrec.getTheYear());
        String encodeTableName = HtmlEscapeUtils.escapeString(hisTableName);

        //送去DB做查詢, 因為是SQL動態, 改用EntityManager 自行撈DB,不透過Repository
        List<HisRecDto> searchResult = querySupervisorHisRecList(searchrec, supAbleAgentIDList,
                supAbleExtList, encodeTableName, username);
        boolean isSupervisor = true;
        return convertStringToRServiceBean(searchResult, isSupervisor);
    }//-------------------------------------------------------------------------------------------------

    private List<HisRecDto> queryHisRecList(SearchHisRecForm searchrec, String encodeTableName, String username) throws DataAccessException {
        String startdate = searchrec.getTheYear() + "-" + searchrec.getTheMonth() + "-" + searchrec.getTheStartDay() + " 00:00:00";
        String enddate = searchrec.getTheYear() + "-" + searchrec.getTheMonth() + "-" + searchrec.getTheEndDay() + " 23:59:59";
        String jpql = "Select h.ID, h.ConnID, h.Ani, IF(char_length(h.Dnis)>0, h.Dnis, '') as Dnis, " +
                "IF(char_length(h.AgentID)>0, h.AgentID, '') as AgentID, " +
                "h.StartDate, h.EndDate, h.FileName, h.UUID, h.AgentDN," +
                "IF(char_length(h.CallType)>0, h.CallType, '') as CallType, " +
                "IF(char_length(h.CallTypeName)>0, h.CallTypeName, '') as CallTypeName, " +
                "IF(char_length(h.CustomerID)>0, h.CustomerID, '') as CustomerID, " +
                "IF(char_length(h.Length)>0, h.Length, 0) as Length, " +
                "IF(char_length(h.Location)>0, h.Location, '') as Location, " +
                "IF(char_length(h.CallDir)>0, h.CallDir, '') as CallDir, " +
                "IF(char_length(h.WorkID)>0, h.WorkID, '') as WorkID, " +
                "IF(char_length(h.AD)>0, h.AD, '') as AD, " +
                "sa1.CanDL as ECanDL, sa1.Setrec as ESetrec, " +
                "sa1.AuditStartDate as AuditStartDateByAbleExt, sa1.AuditEndDate as AuditEndDateByAbleExt, " +
                "sa2.CanDL as ACanDL, sa2.Setrec as ASetrec, " +
                "sa2.AuditStartDate as AuditStartDateByAbleAgentID, sa2.AuditEndDate as AuditEndDateByAbleAgentID, " +
                "CASE WHEN kr.ConnID IS NOT NULL THEN 1 ELSE 0 END as KeepRec " +
                " FROM " + encodeTableName + " h" +
                " LEFT JOIN sup_auditlist sa1 ON sa1.UserName = :username and sa1.ableExt = h.AgentDN " +
                " LEFT JOIN sup_auditlist sa2 ON sa2.UserName = :username and sa2.ableAgentID = h.AgentID " +
                " LEFT JOIN keepreclist kr ON kr.ConnID = h.ConnID " +
                " WHERE h.StartDate >= :startDate AND h.StartDate <= :endDate ";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("startDate", DateTimeUtil.dateStrToDate(startdate));
        parameters.put("endDate", DateTimeUtil.dateStrToDate(enddate));

        //做ALL的資料矯正
        if (searchrec.getCallDir().equalsIgnoreCase("ALL")) {
            searchrec.setCallDir("");
        }

        if (StringUtils.isNotBlank(searchrec.getCustomerId())) {
            jpql += "and h.CustomerID = :customerID ";
            parameters.put("customerID", searchrec.getCustomerId());

        }
        if (StringUtils.isNotBlank(searchrec.getAni())) {
            jpql += "and (h.Ani like :ani or h.Dnis like :ani) ";
            parameters.put("ani", "%" + searchrec.getAni() + "%");
        }
        if (StringUtils.isNotBlank(searchrec.getAgentId())) {
            jpql += "and h.AgentID = :agentID ";
            parameters.put("agentID", searchrec.getAgentId());
        }
        if (StringUtils.isNotBlank(searchrec.getAgentDN())) {
            jpql += "and h.AgentDN = :agentDN ";
            parameters.put("agentDN", searchrec.getAgentDN());
        }
        if (StringUtils.isNotBlank(searchrec.getCallDir())) {
            jpql += "and h.CallDir = :callDir ";
            parameters.put("callDir", searchrec.getCallDir());
        }
        if (StringUtils.isNotBlank(searchrec.getCallTypeName())) {
            jpql += "and h.CallTypeName like :callTypeName ";
            parameters.put("callTypeName", "%" + searchrec.getCallTypeName() + "%");
        }

        if (ToolPlugins.checkAuthorization("authorization")) {
            return jdbc.query(jpql, parameters, new BeanPropertyRowMapper<>(HisRecDto.class));
        }

        return null;
    }

    private List<HisRecDto> querySupervisorHisRecList(SearchHisRecForm searchrec, List<String> ableAgentIDList,
                                                      List<String> ableExtList, String encodeTableName,
                                                      String username) throws DataAccessException {
        String startdate = searchrec.getTheYear() + "-" + searchrec.getTheMonth() + "-" + searchrec.getTheStartDay() + " 00:00:00";
        String enddate = searchrec.getTheYear() + "-" + searchrec.getTheMonth() + "-" + searchrec.getTheEndDay() + " 23:59:59";
        String jpql = "SELECT * FROM (Select h.ID, h.ConnID, h.Ani, " +
                "IF(char_length(h.Dnis)>0, h.Dnis, '') as Dnis, " +
                "IF(char_length(h.AgentID)>0, h.AgentID, '') as AgentID, " +
                "h.StartDate, h.EndDate, h.FileName, h.UUID, h.AgentDN, " +
                "IF(char_length(h.CallType)>0, h.CallType, '') as CallType, " +
                "IF(char_length(h.CallTypeName)>0, h.CallTypeName, '') as CallTypeName, " +
                "IF(char_length(h.CustomerID)>0, h.CustomerID, '') as CustomerID, " +
                "IF(char_length(h.Length)>0, h.Length, 0) as Length, " +
                "IF(char_length(h.Location)>0, h.Location, '') as Location, " +
                "IF(char_length(h.CallDir)>0, h.CallDir, '') as CallDir, " +
                "IF(char_length(h.WorkID)>0, h.WorkID, '') as WorkID, " +
                "IF(char_length(h.AD)>0, h.AD, '') as AD, " +
                "sa1.CanDL as ECanDL, sa1.Setrec as ESetrec, " +
                "sa1.AuditStartDate as AuditStartDateByAbleExt, sa1.AuditEndDate as AuditEndDateByAbleExt, " +
                "sa2.CanDL as ACanDL, sa2.Setrec as ASetrec, " +
                "sa2.AuditStartDate as AuditStartDateByAbleAgentID, sa2.AuditEndDate as AuditEndDateByAbleAgentID, " +
                "CASE WHEN kr.ConnID IS NOT NULL THEN 1 ELSE 0 END as KeepRec " +
                " FROM " + encodeTableName + " h" +
                " LEFT JOIN sup_auditlist sa1 ON sa1.UserName = :username and sa1.ableExt = h.AgentDN  " +
                " LEFT JOIN sup_auditlist sa2 ON sa2.UserName = :username and sa2.ableAgentID = h.AgentID  " +
                " LEFT JOIN keepreclist kr ON kr.ConnID = h.ConnID  " +
                " WHERE h.StartDate >= :startDate AND h.StartDate <= :endDate ";


        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("startDate", DateTimeUtil.dateStrToDate(startdate));
        parameters.put("endDate", DateTimeUtil.dateStrToDate(enddate));

        //做ALL的資料矯正
        if (searchrec.getCallDir().equalsIgnoreCase("ALL")) {
            searchrec.setCallDir("");
        }

        if (StringUtils.isNotBlank(searchrec.getCustomerId())) {
            jpql += "and h.CustomerID = :customerID ";
            parameters.put("customerID", searchrec.getCustomerId());

        }
        if (StringUtils.isNotBlank(searchrec.getAni())) {
            jpql += "and (h.Ani like :ani or h.Dnis like :ani) ";
            parameters.put("ani", "%" + searchrec.getAni() + "%");
        }
        if (StringUtils.isNotBlank(searchrec.getAgentId())) {
            jpql += "and h.AgentID = :agentID ";
            parameters.put("agentID", searchrec.getAgentId());
        }
        if (StringUtils.isNotBlank(searchrec.getAgentDN())) {
            jpql += "and h.AgentDN = :agentDN ";
            parameters.put("agentDN", searchrec.getAgentDN());
        }
        if (StringUtils.isNotBlank(searchrec.getCallDir())) {
            jpql += "and h.CallDir = :callDir ";
            parameters.put("callDir", searchrec.getCallDir());
        }
        if (StringUtils.isNotBlank(searchrec.getCallTypeName())) {
            jpql += "and h.CallTypeName like :callTypeName ";
            parameters.put("callTypeName", "%" + searchrec.getCallTypeName() + "%");
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
            List<HisRecDto> hisRecResDtoList = jdbc.query(jpql, parameters, new BeanPropertyRowMapper<>(HisRecDto.class));
            try {
                return HtmlEscapeUtils.escapeList(hisRecResDtoList);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    private List<HisRecDto> convertStringToRServiceBean(List<HisRecDto> hisRecResDtoList, boolean isSupervisor) {
        try {
            String isNA = "NA";
            String validFlag = "0";
            hisRecResDtoList = new HisRecDto().convertDto(hisRecResDtoList);
            hisRecResDtoList.forEach(dto -> {
                String agentId = dto.getAgentID();
                dto.setFileKeepRec(dto.getKeepRec() == 1);
                // 如果不是supervisor 都不用檢查權限
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
            return hisRecResDtoList;
        } catch (Exception e) {
            log.error("Error to convertStringToRServiceBean");
            throw e;
        }
    }

    private List<HisRecDto> parseAndEncodeHisRecDto(List<HisRecDto> hisRecDtoList) {
        return hisRecDtoList.stream().peek(dto -> {
            dto.setLengthhhmmss(dto.getLength());
            dto.setAni(HtmlEscapeUtils.escapeString(dto.getAni()));
            dto.setDnis(HtmlEscapeUtils.escapeString(dto.getDnis()));
            dto.setStartDate(HtmlEscapeUtils.escapeString(dto.getStartDate()));
            dto.setEndDate(HtmlEscapeUtils.escapeString(dto.getEndDate()));
        }).collect(Collectors.toList());
    }//------------------------


    /**
     * 設定 歷史音檔為永久音檔
     */
    public void makeKeepRec(String connId, String year, String username) throws ParseException, IOException {
        String hisTableName = year + "reclist";
        historyRecToForeverRec(hisTableName, connId);
        executeInsertKeepList(hisTableName, connId);
        LogDto logDto = new LogDto();
        logDto.setFunctionName("hisreclist");
        logDto.setActionType("keep");
        logDto.setInfo("設定永久音檔: 音檔ConnID= " + connId + " updated by user=" + username);
        logService.addLog(logDto, username);
    }

    /**
     * 將音檔寫入DB
     */
    public void executeInsertKeepList(String hisTableName, String connid) {
        try {
            StringBuilder selectSql = new StringBuilder();
            selectSql.append("SELECT connId, ani, dnis, agentId, startDate, fileName, ")
                    .append("callType, callTypeName, customerId, ")
                    .append("length, location, callDir, uuid, workId, agentDn, ad ")
                    .append("FROM ")
                    .append(hisTableName)
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

    /**
     * 歷史音檔 按下設定永久(複製實體音檔 至待刪資料夾)
     *
     * @throws ParseException, IOException
     */
    public void historyRecToForeverRec(String hisTableName, String connId) throws ParseException, IOException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(hisTableName).append(" WHERE ConnID = :connId");
        if (ToolPlugins.checkAuthorization("authorization")) {
            List<ScheduledHisRecDto> hisRecList = jdbc.query(sql.toString(), new MapSqlParameterSource("connId", connId),
                    new BeanPropertyRowMapper<>(ScheduledHisRecDto.class));

            for (ScheduledHisRecDto dto : hisRecList) {
                Path baseFolder = Paths.get(FileSaveConfig.getMoreThanYearFolder()).toAbsolutePath().normalize();
                Path targetFolder = Paths.get(FileSaveConfig.getForeverSaveFolder()).toAbsolutePath().normalize();

                FileDateInfo fileDateInfo = recService.getFileDateDtoByFileName(dto.getFileName());
                String stringDateFolderName = fileDateInfo.getStringDateFolderName();
                String sanitizeFileName = FileUtil.sanitizePathTraversal(dto.getFileName());

                Path sourceSanitizedSuffixPath = baseFolder.resolve(Paths.get(stringDateFolderName, sanitizeFileName)).normalize();
                Path targetSanitizedSuffixPath = targetFolder.resolve(Paths.get(stringDateFolderName, sanitizeFileName)).normalize();
                if(!Files.exists(sourceSanitizedSuffixPath)) {
                    throw new ServiceException("File not found");
                }
                FileUtil.copyFile(sourceSanitizedSuffixPath.toString(),
                        targetSanitizedSuffixPath.toString());
            }
        }
    }

    /**
     * 取得 歷史音檔名 DB TableName(ex.2021,2022)
     *
     * @return
     */
    public List<Integer> getTableYearByDataTable() {
        try {
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = :tableName " +
                    "AND TABLE_NAME LIKE '%reclist' " +
                    "AND TABLE_NAME NOT IN ('keepreclist', 'reclist')";
            return jdbc.queryForList(sql,
                            new MapSqlParameterSource("tableName", database), String.class)
                    .stream()
                    .map(str -> Pattern.compile("\\d+").matcher(str))
                    .filter(Matcher::find)
                    .map(s -> Integer.parseInt(s.group()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error get hisTableName list : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private String getTableNameByRequest(String theYear) {
        List<Integer> allRecYearTableName = getTableYearByDataTable();
        Map<String, String> yearMap = allRecYearTableName
                .stream()
                .collect(Collectors.toMap(Object::toString, e -> e + "reclist"));
        if (!yearMap.containsKey(theYear)) {
            throw new ServiceException("年分格式有誤或該年分的資料表不存在");
        }
        return yearMap.get(theYear);
    }

    /**
     * 檢查是否為有效西元年份
     */
    public boolean isValidADYearsCheck(List<Integer> years) {
        int currentYear = Year.now().getValue();
        Pattern pattern = Pattern.compile("^(19|20)\\d{2}$");

        return years.stream()
                .allMatch(year -> {
                    Matcher matcher = pattern.matcher(year.toString());
                    if (matcher.matches()) {
                        int parsedYear = Integer.parseInt(year.toString());
                        return parsedYear <= currentYear;
                    }
                    return false;
                });
    }
}