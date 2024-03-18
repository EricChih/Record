package com.ub.gir.web.entity;


import java.util.Date;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder
public abstract class BaseRecList {

    @Id
    private int ID;

    private String ConnID;
    private String Ani;
    private String Dnis;
    private String AgentID;
    private Date StartDate;
    private Date EndDate;
    private String FileName;
    private String CallType;
    private String CallTypeName;
    private String CustomerID;
    private int Length;
    private String Location;
    private String CallDir;
    private String UUID;
    private String WorkID;
    private String AgentDN;
    private String AD;

    @Override
    public String toString() {
        return "reclist [ID=" + ID + ", ConnID=" + ConnID + ", Ani=" + Ani + ", Dnis=" + Dnis + ", AgentID=" + AgentID
                + ", StartDate=" + StartDate + ", EndDate=" + EndDate + ", FileName=" + FileName + ", CallType="
                + CallType + ", CallTypeName=" + CallTypeName + ", CustomerID=" + CustomerID + ", Length=" + Length
                + ", Location=" + Location + ", CallDir=" + CallDir + ", UUID=" + UUID + ", WorkID=" + WorkID
                + ", AgentDN=" + AgentDN + ", AD=" + AD + "]";
    }

}
