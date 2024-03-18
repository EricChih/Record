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
public abstract class BaseUsageLog {

    @Id
    private int ID;

    private Date ActionTime;
    private String ActionUser;
    private String FunctionName;
    private String ActionType;
    private String Info;

    private String ActionRole;
    private String ActionDepID;

    @Override
    public String toString() {
        return "BaseUsageLog{" +
                "ID=" + ID +
                ", ActionTime=" + ActionTime +
                ", ActionUser='" + ActionUser + '\'' +
                ", FunctionName='" + FunctionName + '\'' +
                ", ActionType='" + ActionType + '\'' +
                ", Info='" + Info + '\'' +
                ", ActionRole='" + ActionRole + '\'' +
                ", ActionDepID='" + ActionDepID + '\'' +
                '}';
    }

}
