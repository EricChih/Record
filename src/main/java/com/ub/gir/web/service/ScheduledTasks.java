package com.ub.gir.web.service;

import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.dto.hisRec.ScheduledHisRecDto;
import com.ub.gir.web.dto.rec.FileDateInfo;
import com.ub.gir.web.entity.db1.master.DepDB1Master;
import com.ub.gir.web.entity.db1.master.GirConfigDB1Master;
import com.ub.gir.web.entity.db1.master.RecListDB1Master;
import com.ub.gir.web.entity.parameter.ScheduleParameter;
import com.ub.gir.web.repository.db1.master.DepDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepExtDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.GirConfigDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.RecListDB1MasterRepository;
import com.ub.gir.web.util.DateTimeUtil;
import com.ub.gir.web.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ub.gir.web.util.DateTimeUtil.addYear;
import static com.ub.gir.web.util.DateTimeUtil.getYearListBetweenDates;
import static com.ub.gir.web.util.FileUtil.mkdir;
import static com.ub.gir.web.util.FileUtil.writeToFile;
import static com.ub.gir.web.util.RocDateUtil.*;


@Slf4j
@Component
@Transactional
public class ScheduledTasks {
    @Resource
    private ConfigService configservice;
    @Resource
    private LogService logservice;
    @Resource
    private FileSaveConfig fileSaveConfig;
    @Resource
    private GirConfigDB1MasterRepository girConfigRepo;
    @Resource
    private ExternalDbService externalDbService;
    @Resource
    private DepDB1MasterRepository depRepo;
    @Resource
    private DepExtDB1MasterRepository depExtRepo;
    @Resource(name = "db1MasterNamedJdbcTemplate")
    private NamedParameterJdbcTemplate jdbc;
    @Resource
    private RecListDB1MasterRepository recListRepo;
    private static final String ACTIVE_FLAG = "Y";
    private static final String LOG_FILE_EXTENSION = ".txt";


    /**
     * 執行 刪除操作軌跡
     */
    @Scheduled(cron = "${ub.schedule.cron.expression.delLogTrack}")
    public void deleteLogTrack() throws IOException {
        StringBuilder contentLog = new StringBuilder();
        int keepLogDays = configservice.getLogKeepDays();
        Date expiryDate = DateTimeUtil.addDays(new Date(), -keepLogDays);

        contentLog.append("Deleted to LogTrack is start\n");
        logservice.deleteByExpiryDate(expiryDate, contentLog);
        contentLog.append("\nDeleted to LogTrack is end");
        writeLogToFile(generateLogFileName("DeleteLogTrack_"), contentLog.toString());
    }

    /**
     * 執行 搬移一年內音檔至歷史音檔
     */
    @Scheduled(cron = "#{fileSaveConfig.getMoveToHistoryRecClock()}")
    public void transferToHisRec() throws ParseException, IOException {
        StringBuilder contentLog = new StringBuilder();
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(
                ScheduleParameter.GENERAL_REC_TO_HISTORY_REC.getIdCode());

        if (girConfigOptional.isPresent()) {
            String memo = girConfigOptional.get().getMemo();
            if (memo.contains(ACTIVE_FLAG)) {
                setCurrentTime(contentLog);
                contentLog.append("Moved withinYearFolder to moreThanYearFolder is start\n");
                toHistoryRec(contentLog);
                contentLog.append("\nMoved withinYearFolder to moreThanYearFolder is end");
                setCurrentTime(contentLog);
            }
        }
        writeLogToFile(generateLogFileName("ToHisRec_"), contentLog.toString());
    }

    /**
     * 執行 搬移歷史音檔至垃圾桶音檔排程(超過保存年限)
     */
    @Scheduled(cron = "#{fileSaveConfig.getMoveToDeleteRecClock()}")
    public void transferHistoryRecToDeleteRec() throws IOException, ParseException {
        StringBuilder contentLog = new StringBuilder();
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(
                ScheduleParameter.HISTORY_REC_TO_DELETE_REC.getIdCode());

        if (girConfigOptional.isPresent()) {
            String memo = girConfigOptional.get().getMemo();
            if (memo.contains(ACTIVE_FLAG)) {
                setCurrentTime(contentLog);
                contentLog.append("Moved moreThanYearFolder to overdueDeletedFolder is start\n");
                historyRecToDeleteRec(contentLog);
                contentLog.append("\nMoved moreThanYearFolder to overdueDeletedFolder is end");
                setCurrentTime(contentLog);
            }
        }
        writeLogToFile(generateLogFileName("HistoryRecToDeleteRec_"), contentLog.toString());
    }

    /**
     * 執行 清空垃圾桶音檔排程
     */
    @Scheduled(cron = "#{fileSaveConfig.getDeleteRecClock()}")
    public void cleanOfDelRec() throws IOException, ParseException {
        StringBuilder contentLog = new StringBuilder();
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(
                ScheduleParameter.DELETE_REC.getIdCode());

        if (girConfigOptional.isPresent()) {
            String memo = girConfigOptional.get().getMemo();
            if (memo.contains(ACTIVE_FLAG)) {
                setCurrentTime(contentLog);
                contentLog.append("Deleted to DelFolder is start\n");
                FileUtil.forceDeleteFolder(FileSaveConfig.getOverdueDeletedFolder(), contentLog);
                contentLog.append("\nDeleted to DelFolder is end");
                setCurrentTime(contentLog);

            }
        }
        writeLogToFile(generateLogFileName("CleanOfDelRec_"), contentLog.toString());
    }

    /**
     * 執行 取得GA資料至DB排程
     */
    @Scheduled(cron = "${ub.schedule.cron.expression.loadFromExternalDb}")
    public void loadFromExternalDb() {
        log.info("LoadFromExternalDb is start");
        externalDbService.loadFromExternalDb();
        log.info("LoadFromExternalDb is end");
    }

    private void setCurrentTime(StringBuilder contentLog) throws ParseException {
        contentLog.append(convertDateType(new Date(), "yyyy-MM-dd HH:mm:ss", "yyy-MM-dd HH:mm:ss"));
    }

    /**
     * 歷史 搬移 待刪除
     */
    public void historyRecToDeleteRec(StringBuilder contentLog) throws ParseException {
        int updateCount = 0;
        List<DepDB1Master> depList = depRepo.findAll();
        for (DepDB1Master depEntity : depList) {
            int recKeepYear = depEntity.getRecKeepYear();
            // 保留歷史音檔
            if (recKeepYear >= 999)
                continue;

            List<String> extList = depExtRepo.getExtByDepID(depEntity.getDepID());
            for (String ext : extList) {
                //預設最長刪除7年，最少刪除n-1年(ex.以2023年為例，0-1=2022)
                Date defaultDueDate = DateTimeUtil.addDays(addYear(new Date(), -7), -1);
                Date expirationBaseDate = DateTimeUtil.addDays(addYear(new Date(), -recKeepYear - 1), -1);
                List<Integer> yearListBetweenDates = getYearListBetweenDates(defaultDueDate, expirationBaseDate);
                for (Integer year : yearListBetweenDates) {
                    String hisTableName = year + "reclist";
                    List<ScheduledHisRecDto> hisRecList = getHisRecListForExt(hisTableName, ext, jdbc);
                    if(hisRecList.isEmpty()){
                        contentLog.append(String.format("\nNo matching data found by: (%s, %s)\n", hisTableName, ext));
                    }

                    for (ScheduledHisRecDto dto : hisRecList) {
                        FileDateInfo fileDateInfo = getFileDateDtoByFileName(dto.getFileName());

                        // fileDate 在 expireDate 之前 == -1
                        if (fileDateInfo.getRealFileDate().compareTo(expirationBaseDate) < 0) {
                            String stringDateFolderName = fileDateInfo.getStringDateFolderName();
                            String sanitizeFileName = FileUtil.sanitizePathTraversal(dto.getFileName());
                            String sourceFile = FileSaveConfig.getMoreThanYearFolder() + stringDateFolderName + "/" + sanitizeFileName;
                            String targetFile = FileSaveConfig.getOverdueDeletedFolder() + stringDateFolderName + "/" + sanitizeFileName;
                            moveFile(sourceFile, targetFile, contentLog);

                            //刪除 DB音檔
                            String deleteSql = "DELETE FROM " + hisTableName + " WHERE AgentDN = :AgentDN";
                            contentLog.append(String.format("\nDELETE SQL: %s (%s)\n", deleteSql, ext));
                            jdbc.update(deleteSql, new MapSqlParameterSource("AgentDN", ext));
                            updateCount++;
                        }
                    }
                }
            }
        }
        contentLog.append(String.format("\nTotal updated records: %d\n\n", updateCount));
    }

    /**
     * 根據部門ID和ext獲取該部門和該ext對應的歷史音檔列表
     */
    private List<ScheduledHisRecDto> getHisRecListForExt(String tableName, String ext, NamedParameterJdbcTemplate jdbc) {
        try {
            String sql = "SELECT * FROM " + tableName + " WHERE AgentDN = :AgentDN";

            return jdbc.query(sql, new MapSqlParameterSource("AgentDN", ext),
                    new BeanPropertyRowMapper<>(ScheduledHisRecDto.class));
        } catch (Exception e) {
            log.error("Error getHisRecListForExt : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * 一般音檔 搬移 歷史音檔 + 搬實體音檔
     */
    @Transactional(transactionManager = "db1MasterJpaTransactionManager", rollbackFor = Exception.class)
    public void toHistoryRec(StringBuilder contentLog) throws ParseException {
        int updateCount = 0;
        Date defaultDueDate = DateTimeUtil.addDays(addYear(new Date(), -1), -1);

        List<RecListDB1Master> recList = recListRepo.findRecListByStartDate(defaultDueDate);
        if(recList.isEmpty()){
            contentLog.append("\nNo matching data found\n");
        }

        for (RecListDB1Master recEntity : recList) {
            int id = recEntity.getID();
            FileDateInfo fileDateInfo = getFileDateDtoByFileName(recEntity.getFileName());

            // fileDate 在 defaultDueDate 之前 == -1
            if (fileDateInfo.getRealFileDate().compareTo(defaultDueDate) < 0) {
                String stringDateFolderName = fileDateInfo.getStringDateFolderName();
                String sanitizeFileName = FileUtil.sanitizePathTraversal(recEntity.getFileName());
                String sourceFile = FileSaveConfig.getWithinYearFolder() + stringDateFolderName + "/" + sanitizeFileName;
                String targetFile = FileSaveConfig.getMoreThanYearFolder() + stringDateFolderName + "/" + sanitizeFileName;
                String hisTableName = stringDateFolderName.substring(0, 4) + "reclist";
                contentLog.append(String.format("HisTableName: %s\n", hisTableName));
                moveFile(sourceFile, targetFile, contentLog);

                MapSqlParameterSource paramMap =setRecMapParameter(recEntity);
                String insertSql = "INSERT INTO " + hisTableName + " (ConnID,Ani,Dnis,AgentID,StartDate,EndDate,FileName,CallType,CallTypeName,CustomerID,`Length`,Location,CallDir,UUID,WorkID,AgentDN,AD)\n" +
                        "VALUES(:connId, :ani, :dnis, :agentId, :startDate, :endDate, :fileName, :callType, :callTypeName, :customerId, :length, :location, :callDir, :uuid, :workId, :agentDn, :ad)";
                try {
                    // 判斷 fileName db是否已存在並紀錄log，Y不複製 N複製
                    String existedRecSql = "SELECT COUNT(*) FROM " + hisTableName + " WHERE fileName = :fileName";
                    int count = jdbc.queryForObject(existedRecSql, new MapSqlParameterSource("fileName", recEntity.getFileName()), Integer.class);
                    if (count != 0) {
                        contentLog.append(String.format("\nDB Index already exists: %s\n", id));
                        continue;
                    }
                    jdbc.update(insertSql, paramMap);
                } catch (Exception e) {
                    contentLog.append(e.getMessage());
                    //可能因無hisTable，進行創建
                    createNewSqlTableByTableName(hisTableName, jdbc);
                    contentLog.append(String.format("\nCreate data table successfully: %s\n", hisTableName));
                    jdbc.update(insertSql, paramMap);
                }
                contentLog.append(String.format("\nSuccessfully transferred index: %s\n", id));
                recListRepo.deleteRecById(recEntity.getID());
                updateCount++;
            }
        }
        contentLog.append(String.format("\nTotal updated records: %d\n\n", updateCount));
    }

    private MapSqlParameterSource setRecMapParameter(RecListDB1Master recEntity){
        return new MapSqlParameterSource()
                .addValue("connId", recEntity.getConnID())
                .addValue("ani", recEntity.getAni())
                .addValue("dnis", recEntity.getDnis())
                .addValue("agentId", recEntity.getAgentID())
                .addValue("startDate", recEntity.getStartDate())
                .addValue("endDate", recEntity.getEndDate())
                .addValue("fileName", recEntity.getFileName())
                .addValue("callType", recEntity.getCallType())
                .addValue("callTypeName", recEntity.getCallTypeName())
                .addValue("customerId", recEntity.getCustomerID())
                .addValue("length", recEntity.getLength())
                .addValue("location", recEntity.getLocation())
                .addValue("callDir", recEntity.getCallDir())
                .addValue("uuid", recEntity.getUUID())
                .addValue("workId", recEntity.getWorkID())
                .addValue("agentDn", recEntity.getAgentDN())
                .addValue("ad", recEntity.getAD());
    }

    private FileDateInfo getFileDateDtoByFileName(String fileName) throws ParseException {
        String[] fileNameArray = fileName.split("_");
        String fileDateYYYYMMDD = fileNameArray[1];
        String fileDate = fileDateYYYYMMDD + " " + fileNameArray[2].replaceAll("-", ":");
        Date realFileDate = convertStringDateToDate(fileDate, "yyyy-MM-dd HH:mm:ss", false);
        String stringDateFolderName = fileDateYYYYMMDD.replaceAll("-", "");

        return new FileDateInfo(realFileDate,stringDateFolderName);
    }

    public void createNewSqlTableByTableName(String hisTableName, NamedParameterJdbcTemplate jdbc) {
        try {
            String sql = "CREATE TABLE " + hisTableName + " (\n" +
                    "ID bigint auto_increment PRIMARY KEY,\n" +
                    "ConnID varchar(100),\n" +
                    "Ani VARCHAR(50),\n" +
                    "Dnis VARCHAR(50),\n" +
                    "AgentID VARCHAR(50),\n" +
                    "StartDate timestamp,\n" +
                    "EndDate timestamp,\n" +
                    "FileName varchar(256) UNIQUE,\n" +
                    "CallType varchar(256),\n" +
                    "CallTypeName varchar(256),\n" +
                    "CustomerID VARCHAR(15),\n" +
                    "Length INT,\n" +
                    "Location VARCHAR(50),\n" +
                    "CallDir VARCHAR(50),\n" +
                    "UUID VARCHAR(100),\n" +
                    "WorkID VARCHAR(50),\n" +
                    "AgentDN VARCHAR(50),\n" +
                    "AD VARCHAR(50)\n" +
                    "INDEX idx_ConnID (ConnID)," +
                    "INDEX idx_StartDateAndEndDate (StartDate,EndDate)," +
                    "INDEX idx_FileName (FileName)," +
                    ")";
            jdbc.update(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            log.error("Error createNewSqlTableByTableName : {}", e.getMessage(), e);
        }
    }

    private void moveFile(String sourceFile, String targetFile, StringBuilder contentLog) {
        try {
            FileUtil.moveFile(sourceFile, targetFile);
            contentLog.append(String.format("File is found: %s", sourceFile));
        } catch (RuntimeException e) {
            contentLog.append(String.format("File is not found: %s", sourceFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateLogFileName(String prefix) {
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + formattedDateTime + LOG_FILE_EXTENSION;
    }

    private void writeLogToFile(String fileName, String content) throws IOException {
        String directory = FileSaveConfig.getScheduleLogFolder();
        mkdir(Paths.get(directory).normalize());
        writeToFile(Paths.get(directory, fileName).normalize(), content);
    }
}