package com.ub.gir.web.dto.girConfig;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UpdatePwdReqDto {
    @NotEmpty(message="密碼是否需夾雜中英文不可為空")
    private String pwdEngNumFlag;
    @NotEmpty(message="密碼是否與跟帳號相同不可為空")
    private String pwdSameAccountFlag;
    @NotEmpty(message="首次登入是否重設密碼不可為空")
    private String pwdFirstLoginFlag;
    @NotEmpty(message="    \n不可為空")
    private String pwdLenLimitFlag;
    @NotEmpty(message="密碼是否有最小長度限制的值不可為空")
    private String pwdLenLimit;
    @NotEmpty(message="密碼是否有錯誤次數鎖定不可為空")
    private String pwdWrongTimesFlag;
    @NotEmpty(message="密碼錯誤次數鎖定的值不可為空")
    private String pwdWrongTimes;
    @NotEmpty(message="密碼到期變更天數不可為空")
    private String pwdExpirationModifyDaysFlag;
    @NotEmpty(message="密碼到期變更天數的值不可為空")
    private String pwdExpirationModifyDays;
    @NotEmpty(message="密碼變更提醒天數不可為空")
    private String pwdModifyReminderDaysFlag;
    @NotEmpty(message="密碼變更提醒天數的值不可為空")
    private String pwdModifyReminderDays;
    @NotEmpty(message="密碼代數內不相同不可為空")
    private String pwdUniqueInPreviousFlag;
    @NotEmpty(message="密碼代數內不相同的值不可為空")
    private String pwdUniqueInPrevious;
}