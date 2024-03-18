package com.ub.gir.web.dto.rec;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SearchRecForm {
    @NotBlank(message = "請輸入開始日期")
    private String startDate;
    @NotBlank(message = "請輸入結束日期")
    private String endDate;
    private String customerId;
    private String ani;
    private String agentId;
    private String callTypeName;
    private String callDir;
    private String agentDN;

}
