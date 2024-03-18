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
public abstract class BasePasswordHistory {

    @Id
    private int ID;

    private String UserName;
    private String OLDPassword;
    private Date ChangeDate;

    @Override
    public String toString() {
        return "passwordhistory Entity Bean data: [ID=" + ID + ", UserName=" + UserName + ", OLDPassword=" + OLDPassword + ", ChangeDate="
                + ChangeDate + "]";
    }

}
