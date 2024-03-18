package com.ub.gir.web.entity.parameter;

public enum ScheduleParameter {
    SCHEDULE("schedule",""),
    //操作軌跡保留天數
    KEEP_LOG_DAYS("schedule","keepLogDays"),
    //一般音檔至歷史音檔
    GENERAL_REC_TO_HISTORY_REC("schedule","generalRecToHistoryRec"),
    //歷史音檔至刪除音檔
    HISTORY_REC_TO_DELETE_REC("schedule","historyRecToDeleteRec"),
    //刪除音檔
    DELETE_REC("schedule","deleteRec");

    private String idType;
    private String idCode;

    ScheduleParameter(String idType, String idCode) {
        this.idType = idType;
        this.idCode = idCode;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }
}