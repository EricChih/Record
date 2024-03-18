package com.ub.gir.web.dto.keeprec;

import com.ub.gir.web.util.HtmlEscapeUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class KeepRecDto {
    private int ID;
    private String ConnID;
    private String Ani;    //分機號碼
    private String Dnis; //來電顯示號
    private String AgentID;
    private String StartDate;
    private String EndDate;
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
    private String UserName;
    private String ACanDL;
    private String ASetrec;
    private String ECanDL;
    private String ESetrec;
    private String crec = "false";
    private String PlayPathFile; //非DB欄位,音檔NAS播放folder+Filename
    private String sqlstr; //非DB欄位,暫存sql
    private String lengthhhmmss; //非DB欄位,通話時間的時分秒
    private String CallDirCN; //非DB欄位,通話方向的中文說明:1=(內線),3=(外撥),空白=(其他)
    private int KeepRec = 0; //非DB欄位,是否有設永久音檔: 0=無, 1=有設永久
    private String UDepID; //非DB欄位,紀錄目前使用者的部門.音檔沒有部門的欄位
    private String theYear; //非DB欄位年份,要依造年份去對應tablename 20xxreclist
    private String theMonth; //非DB欄位月份
    private Date AuditStartDateByAbleExt;
    private Date AuditEndDateByAbleExt;
    private Date AuditStartDateByAbleAgentID;
    private Date AuditEndDateByAbleAgentID;
    private boolean canPlay = false;
    private boolean canDownload = false;
    private boolean canKeepRec = false;

    public void setCallDirCN(String callDirCN) {
        if (callDirCN==null || callDirCN.isEmpty() || callDirCN.trim().isEmpty()) {
            this.CallDirCN = "請選擇";
        } else if (callDirCN.trim().equalsIgnoreCase("NA") || callDirCN.trim().equalsIgnoreCase("ALL")) {
            this.CallDirCN = "全部";
        } else {
            int nCallDir = 0;
            if (!StringUtils.isNumeric(callDirCN.trim())) {
                nCallDir = 0;
            } else {
                nCallDir = Integer.parseInt(callDirCN.trim());
            }
            if (nCallDir==1) {
                this.CallDirCN = "1:內線";
            } else if (nCallDir==2) {
                this.CallDirCN = "2:外線";
            } else if (nCallDir==3) {
                this.CallDirCN = "3:外撥";
            } else if (nCallDir==4) {
                this.CallDirCN = "4:諮詢";
            } else {
                this.CallDirCN = nCallDir + "其他";
            }
        }
    }

    public void setLengthhhmmss(int time) {
        int hh = time / 3600;
        int mm = (time % 3600) / 60;
        int ss = (time % 3600) % 60;
        this.lengthhhmmss = (hh < 10 ? ("0" + hh):hh) + ":" + (mm < 10 ? ("0" + mm):mm) + ":" + (ss < 10 ? ("0" + ss):ss);
    }

    public void setPlayPathFile(String filename) {
        String[] filenameparts = filename.split("_");
        if (filenameparts.length > 1) {
            this.PlayPathFile = filenameparts[1].replace("-", "") + "/" + filename;
        } else {
            this.PlayPathFile = filename;
        }
    }

    public List<KeepRecDto> convertDto(List<KeepRecDto> reqDtoList){
        return reqDtoList.stream().peek(dto -> {
            dto.setPlayPathFile(HtmlEscapeUtils.escapeString(dto.getFileName())); //設定非DB欄位, 播放檔案路徑
            dto.setLengthhhmmss(dto.getLength()); //設定非DB欄位, 音檔長度hhmmss
            dto.setCallDirCN(HtmlEscapeUtils.escapeString(dto.getCallDir())); //設定非DB欄位, 通話方向中文
            dto.setECanDL(HtmlEscapeUtils.escapeString(dto.getECanDL()));
            dto.setESetrec(HtmlEscapeUtils.escapeString(dto.getESetrec()));
            dto.setConnID(HtmlEscapeUtils.escapeString(dto.getConnID()));
            dto.setStartDate(HtmlEscapeUtils.escapeString(dto.getStartDate()));
            dto.setEndDate(HtmlEscapeUtils.escapeString(dto.getEndDate()));
            dto.setAni(HtmlEscapeUtils.escapeString(dto.getAni()));
            dto.setDnis(HtmlEscapeUtils.escapeString(dto.getDnis()));
            dto.setAgentID(HtmlEscapeUtils.escapeString(dto.getAgentID()));
            dto.setAgentDN(HtmlEscapeUtils.escapeString(dto.getAgentDN()));
            dto.setCustomerID(HtmlEscapeUtils.escapeString(dto.getCustomerID()));
            dto.setCallTypeName(HtmlEscapeUtils.escapeString(dto.getCallTypeName()));
            dto.setAD(HtmlEscapeUtils.escapeString(dto.getAD()));
            dto.setUUID(HtmlEscapeUtils.escapeString(dto.getUUID()));
        }).collect(Collectors.toList());
    }
}
