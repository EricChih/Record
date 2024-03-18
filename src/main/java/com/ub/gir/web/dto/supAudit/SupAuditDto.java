package com.ub.gir.web.dto.supAudit;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SupAuditDto {
    @NotEmpty(message = "可調聽者ID 不可為空")
    private String monitoringUserId;

    @NotEmpty(message = "可調聽者部門 不可為空")
    private String monitoringDepId;

    //登入帳號
    private String UserName;

    private String ableAgentID;

    private String ableExt;

    private String Location ;

    private String AuditStartDate;

    private String AuditEndDate;

    private String Memo;

    @NotEmpty(message = "下載音檔權限 不可為空")
    private String CanDL;

    @NotEmpty(message = "設定永久音檔權限 不可為空")
    private String Setrec;

    private String errmsg;
}