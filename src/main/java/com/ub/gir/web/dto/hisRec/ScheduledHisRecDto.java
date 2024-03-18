package com.ub.gir.web.dto.hisRec;

import lombok.Data;

import java.util.Date;

@Data
public class ScheduledHisRecDto {
    String ID;
    String ConnID;
    String Ani;
    String Dnis;
    String AgentID;
    Date StartDate;
    Date EndDate;
    String FileName;
    String CallType;
    String CallTypeName;
    String CustomerID;
    int Length;
    String Location;
    String CallDir;
    String UUID;
    String WorkID;
    String AgentDN;
    String AD;
}