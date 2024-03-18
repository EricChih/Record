package com.ub.gir.web.entity.parameter;

public enum PasswordParameter {
    PWD("password","",""),
    //密碼是否需夾雜中英文
    PWD_ENG_NUM_FLAG("password","pwdEngNumFlag","密碼須符合英文及數字夾雜"),
    //密碼是否與跟帳號相同
    PWD_SAME_ACCOUNT_FLAG("password","pwdSameAccountFlag","密碼不得與登入帳號相同"),
    //首次登入是否重設密碼
    PWD_FIRST_LOGIN_FLAG("password","pwdFirstLoginFlag","首次登入需重設密碼"),
    //密碼是否有最小長度限制
    PWD_LEN_LIMIT("password","pwdLenLimit","密碼長度不足"),
    //密碼錯誤次數鎖定
    PWD_WRONG_TIMES("password","pwdWrongTimes","密碼已達到錯誤次數"),
    //密碼到期變更天數
    PWD_EXPIRATION_MODIFY_DAYS("password","pwdExpirationModifyDays","密碼到期變更天數提示"),
    //密碼變更提醒天數
    PWD_MODIFY_REMINDER_DAYS("password","pwdModifyReminderDays","密碼變更天數提醒"),
    //密碼代數內不相同
    PWD_UNIQUE_IN_PREVIOUS("password","pwdUniqueInPrevious","密碼不可與前代密碼相同");

    private String idType;
    private String idCode;
    private String errorMsg;

    PasswordParameter(String idType, String idCode,String errorMsg) {
        this.idType = idType;
        this.idCode = idCode;
        this.errorMsg = errorMsg;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}