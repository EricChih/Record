package com.ub.gir.web.dto.girConfig;

import lombok.Data;

@Data
public class GirConfigResDto {
    //密碼是否需夾雜中英文
    private String pwdEngNumFlag;
    //密碼是否與跟帳號相同
    private String pwdSameAccountFlag;
    //首次登入是否重設密碼
    private String pwdFirstLoginFlag;
    //密碼是否有最小長度限制
    private String pwdLenLimitFlag;
    //密碼是否有最小長度限制-value
    private String pwdLenLimit;
    //密碼是否有錯誤次數鎖定
    private String pwdWrongTimesFlag;
    //密碼錯誤次數鎖定-value
    private String pwdWrongTimes;
    //密碼到期變更天數
    private String pwdExpirationModifyDaysFlag;
    //密碼到期變更天數-value
    private String pwdExpirationModifyDays;
    //密碼變更提醒天數
    private String pwdModifyReminderDaysFlag;
    //密碼變更提醒天數-value
    private String pwdModifyReminderDays;
    //密碼代數內不相同
    private String pwdUniqueInPreviousFlag;
    //密碼代數內不相同-value
    private String pwdUniqueInPrevious;
    private String overtime;

    int dbLogKeepDay;
    private String generalRecToHistoryRec;
    private String generalRecToHistoryRecFlag;
    private String historyRecToDeleteRec;
    private String historyRecToDeleteRecFlag;
    private String deleteRec;
    private String deleteRecFlag;
}