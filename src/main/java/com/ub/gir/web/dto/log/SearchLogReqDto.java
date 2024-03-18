package com.ub.gir.web.dto.log;

import lombok.Data;

@Data
public class SearchLogReqDto {
    private int ID;
    private String ActionTime;
    private String ActionUser;
    private String FunctionName;
    private String ActionType;
    private String Info;
    private String ActionDepID;
    private String ActionRole;
    private String Starttime;
    private String Endtime;

    private String FunctionNameCN; //非DB欄位
    private String ActionTypeCN; //非DB欄位
}