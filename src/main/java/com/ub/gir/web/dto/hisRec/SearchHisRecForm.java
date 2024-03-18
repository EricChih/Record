package com.ub.gir.web.dto.hisRec;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SearchHisRecForm {

    @NotBlank(message = "年份不可為空")
    private String theYear;
    @NotBlank(message = "月份不可為空")
    private String theMonth;
    @NotBlank(message = "日期不可為空")
    private String theStartDay;
    @NotBlank(message = "日期不可為空")
    private String theEndDay;
    private String customerId;
    private String ani;
    private String agentId;
    private String callTypeName;
    private String callDir;
    private String agentDN;

}
