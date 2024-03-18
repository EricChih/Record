package com.ub.gir.web.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Date;


@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder
public abstract class BaseSupAuditList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;

    private String UserName; //登入帳號
    private String ableAgentID; //可調聽AgentID
    private String ableExt; //可調聽Ext 分機號碼
    private String DepID; //登入的username
    private String Location; //default 002=台北
    private Date AuditStartDate;
    private Date AuditEndDate;
    private String Memo;
    private String CanDL;
    private String Setrec;

    @Override
    public String toString() {
        return "BaseSupAuditList{" +
                "ID=" + ID +
                ", UserName='" + UserName + '\'' +
                ", ableAgentID='" + ableAgentID + '\'' +
                ", ableExt='" + ableExt + '\'' +
                ", DepID='" + DepID + '\'' +
                ", Location='" + Location + '\'' +
                ", AuditStartDate=" + AuditStartDate +
                ", AuditEndDate=" + AuditEndDate +
                ", Memo='" + Memo + '\'' +
                ", CanDL='" + CanDL + '\'' +
                ", Setrec='" + Setrec + '\'' +
                '}';
    }
}
