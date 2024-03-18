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
public abstract class BaseCfgPerson {

    @Id
    private int ID;

    private int PID;
    private String AgentID;
    private String FirstName;
    private String LastName;
    private String Name; //登入的username
    private String Password;
    private String Role;
    private String Email;
    private String Ext;
    private String Location;
    private int SupervisorID;
    private int ManagerID;
    private int is_agent;
    private int is_admin;
    private String DepID;
    private String Status;
    private Date FinalChangeDate;
    private Date LastLoginDate;
    private String PasswordNew;

    @Override
    public String toString() {
        return "cfg_person Enitity Bean data: [ID=" + ID + ", PID=" + PID + ", AgentID=" + AgentID + ", FirstName=" + FirstName
                + ", LastName=" + LastName + ", Name=" + Name + ", Password=" + Password + ", Role=" + Role + ", Email="
                + Email + ", Ext=" + Ext + ", Location=" + Location + ", SupervisorID=" + SupervisorID + ", ManagerID="
                + ManagerID + ", is_agent=" + is_agent + ", is_admin=" + is_admin + ", DepID=" + DepID + ", Status="
                + Status + ", FinalChangeDate=" + FinalChangeDate + ", LastLoginDate=" + LastLoginDate
                + ", PasswordNew=" + PasswordNew + "]";
    }

}
