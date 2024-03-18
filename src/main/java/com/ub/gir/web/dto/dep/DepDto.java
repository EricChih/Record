package com.ub.gir.web.dto.dep;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class DepDto {
    @NotEmpty(message = "部門ID不可為空")
    private String DepID;
    @NotEmpty(message = "部門名稱不可為空")
    private String DepName;
    @NotEmpty(message = "所在地不可為空")
    private String SiteID;
    private String Status;
    private String DepIDinUnisys="";
    private String PlaceGroup="";
    private int RecKeepYear=0;
    private String GroupTeam="";

    private List<String> AuditGroup;
    private String errmsg; //非DB欄位,後端錯誤訊息
}