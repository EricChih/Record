package com.ub.gir.web.dto.log;

import lombok.Data;

@Data
public class LogDto {
    private int ID;
    private String ActionTime;
    private String ActionUser;
    private String FunctionName;
    private String ActionType;
    private String Info;
    private String ActionDepID;
    private String ActionRole;
}