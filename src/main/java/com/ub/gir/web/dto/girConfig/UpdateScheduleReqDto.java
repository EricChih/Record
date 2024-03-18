package com.ub.gir.web.dto.girConfig;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UpdateScheduleReqDto {
    @NotEmpty(message="一般音檔移轉歷史音檔時段不可為空")
    @Pattern(regexp = "^[1-5]$", message = "一般音檔移轉歷史音檔時段請輸入1到5的數字")
    String generalRecToHistoryRec;
    @NotEmpty(message="一般音檔移轉歷史音檔排程選項不可為空")
    String generalRecToHistoryRecFlag;
    @NotEmpty(message="歷史音檔移轉預刪音檔時段不可為空")
    @Pattern(regexp = "^[1-5]$", message = "歷史音檔移轉預刪音檔時段請輸入1到5的數字")
    String historyRecToDeleteRec;
    @NotEmpty(message="歷史音檔移轉預刪音檔排程選項不可為空")
    String historyRecToDeleteRecFlag;
    @NotEmpty(message="清除預刪音檔時段不可為空")
    @Pattern(regexp = "^[1-5]$", message = "清除預刪音檔請輸入1到5的數字")
    String deleteRec;
    @NotEmpty(message="清除預刪音檔排程選項不可為空")
    String deleteRecFlag;
}