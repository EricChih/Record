package com.ub.gir.web.configuration.file;

import com.ub.gir.web.entity.db1.master.GirConfigDB1Master;
import com.ub.gir.web.entity.parameter.ScheduleParameter;
import com.ub.gir.web.repository.db1.master.GirConfigDB1MasterRepository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;

/**
 * @author elliot
 * @version 1.0
 * @date 2023/3/28
 */
@Component
@Slf4j
public class FileSaveConfig {
    @Resource
    private GirConfigDB1MasterRepository girConfigRepo;
    private static String withinYearFolder;
    private static String moreThanYearFolder;
    private static String overdueDeletedFolder;
    private static String foreverSaveFolder;
    private static String scheduleLogFolder;

    @Value(value = "${ub.datasource.disk.withinYear-folder}")
    public void setWithinYearFolder(String withinYearFolder) {
        FileSaveConfig.withinYearFolder = withinYearFolder;
    }

    @Value(value = "${ub.datasource.disk.moreThanYear-folder}")
    public void setMoreThanYearFolder(String moreThanYearFolder) {
        FileSaveConfig.moreThanYearFolder = moreThanYearFolder;
    }

    @Value(value = "${ub.datasource.disk.overdueDeleted-folder}")
    public void setOverdueDeletedFolder(String overdueDeletedFolder) {
        FileSaveConfig.overdueDeletedFolder = overdueDeletedFolder;
    }

    @Value(value = "${ub.datasource.disk.foreverSave-folder}")
    public void setForeverSaveFolder(String foreverSaveFolder) {
        FileSaveConfig.foreverSaveFolder = foreverSaveFolder;
    }

    @Value(value = "${ub.datasource.disk.scheduleLog-folder}")
    public void setScheduleLogFolder(String scheduleLogFolder) {
        FileSaveConfig.scheduleLogFolder = scheduleLogFolder;
    }

    public static String getWithinYearFolder() {
        return withinYearFolder;
    }

    public static String getMoreThanYearFolder() {
        return moreThanYearFolder;
    }

    public static String getOverdueDeletedFolder() {
        return overdueDeletedFolder;
    }

    public static String getForeverSaveFolder() {
        return foreverSaveFolder;
    }

    public static String getScheduleLogFolder() {
        return scheduleLogFolder;
    }

    /**
     * 取得 搬移一般音檔至歷史音檔時間
     *
     * @throws IOException
     */
    public String getMoveToHistoryRecClock() {
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(ScheduleParameter.GENERAL_REC_TO_HISTORY_REC.getIdCode());
        if (girConfigOptional.isPresent()) {
            String timeInfo = girConfigOptional.get().getMemo();
            int splitIndex = timeInfo.indexOf("-");
            if(splitIndex != -1){
                String timeSplit = timeInfo.substring(splitIndex + 1);
                String cronExpression = "0 0 " + timeSplit + " * * ?";
                if(isValidCron(cronExpression))
                    return cronExpression;
            }
        }
        return "0 0 1 * * ?";
    }

    /**
     * 取得 搬移歷史音檔至垃圾桶音檔時間
     *
     * @throws IOException
     */
    public String getMoveToDeleteRecClock() {
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(ScheduleParameter.HISTORY_REC_TO_DELETE_REC.getIdCode());
        if (girConfigOptional.isPresent()) {
            String timeInfo = girConfigOptional.get().getMemo();
            int splitIndex = timeInfo.indexOf("-");
            if(splitIndex != -1){
                String timeSplit = timeInfo.substring(splitIndex + 1);
                String cronExpression = "0 0 " + timeSplit + " * * ?";
                if(isValidCron(cronExpression))
                    return cronExpression;
            }
        }
        return "0 0 1 * * ?";
    }

    /**
     * 取得 清空垃圾桶音檔時間
     *
     * @throws IOException
     */
    public String getDeleteRecClock() {
        Optional<GirConfigDB1Master> girConfigOptional = girConfigRepo.findByIDCode(ScheduleParameter.DELETE_REC.getIdCode());
        if (girConfigOptional.isPresent()) {
            String timeInfo = girConfigOptional.get().getMemo();
            int splitIndex = timeInfo.indexOf("-");
            if(splitIndex != -1){
                String timeSplit = timeInfo.substring(splitIndex + 1);
                String cronExpression = "0 0 " + timeSplit + " * * ?";
                if(isValidCron(cronExpression))
                    return cronExpression;
            }
        }
        return "0 0 4 * * ?";
    }

    /**
     * 驗證 Cron表達式是否正確
     */
    public static boolean isValidCron(String cronExpression) {
        try {
            CronExpression.isValidExpression(cronExpression);
            return true;
        } catch (Exception e) {
            log.error("Error to valid cron : {}", e.getMessage(), e);
        }
        return false;
    }
}