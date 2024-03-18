package com.ub.gir.web.entity;


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
public abstract class BaseDep {

    @Id
    private String DepID;

    private String DepName;
    private String SiteID;
    private String Status;
    private String DepIDinUnisys;
    private String PlaceGroup;
    private int RecKeepYear = 7;
    private String GroupTeam;
    private String AuditGroup;

    @Override
    public String toString() {
        return "dep Entity Bean data: [DepID=" + DepID + ", DepName=" + DepName + ", SiteID=" + SiteID + ", Status=" + Status
                + ", DepIDinUnisys=" + DepIDinUnisys + ", PlaceGroup=" + PlaceGroup + ", RecKeepYear=" + RecKeepYear
                + ", GroupTeam=" + GroupTeam + ", AuditGroup=" + AuditGroup + "]";
    }

}
