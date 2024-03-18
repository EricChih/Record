package com.ub.gir.web.dto.rec;

import lombok.Data;

import java.util.Date;

@Data
public class FileDateInfo {
    private Date realFileDate;
    private String stringDateFolderName;

    public FileDateInfo( Date realFileDate,String stringDateFolderName) {
        this.realFileDate = realFileDate;
        this.stringDateFolderName = stringDateFolderName;
    }
}