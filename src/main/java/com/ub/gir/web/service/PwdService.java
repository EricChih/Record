package com.ub.gir.web.service;


import com.ub.gir.web.dto.pwd.PwdResDto;
import com.ub.gir.web.entity.BaseCfgPerson;
import com.ub.gir.web.entity.db1.master.GirConfigDB1Master;
import com.ub.gir.web.entity.parameter.PasswordParameter;
import com.ub.gir.web.repository.db1.master.GirConfigDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.PasswordHistoryDB1MasterRepository;
import com.ub.gir.web.util.DateTimeUtil;
import javassist.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Transactional
public class PwdService {
    @Resource
    private PasswordHistoryDB1MasterRepository pwdHisRepo;
    @Resource
    private GirConfigDB1MasterRepository girConfigRepository;
    @Resource
    private PasswordEncoder passwordEncoder;

    private static final String ERROR_MSG_PREFIX = "變更失敗: ";
    private static final String ERROR_MSG_PASSWORD_NOT_MATCH = "密碼與確認密碼不一致";

    /**
     * 透過 girConfig參數 取得歷史密碼紀錄(過濾目前密碼存在passwordHistory表進行 +1)
     */
    public List<char[]> getHisPwd(String userName) throws NotFoundException {
        Map<String, char[]> uniqueInPreviousMap = getPasswordParameterMemo(PasswordParameter.PWD_UNIQUE_IN_PREVIOUS);
        if(uniqueInPreviousMap.containsKey("Y")){
             List<String> result =pwdHisRepo.getPwdHistory(userName,
                    Integer.parseInt(new String(uniqueInPreviousMap.get("Y"))) + 1);

            return result.stream().map(String::toCharArray).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 密碼與舊密碼重複
     */
    public boolean isDuplicateHisPwd(String username, String currentPwd) throws NotFoundException {
        List<char[]> hisPwdList = getHisPwd(username);
        if (hisPwdList.isEmpty())
            return false;

        Map<String, char[]> uniqueInPreviousMap = getPasswordParameterMemo(PasswordParameter.PWD_UNIQUE_IN_PREVIOUS);
        if (uniqueInPreviousMap.containsKey("Y")) {
            for (char[] encryptedHisPassword : hisPwdList) {
                if (passwordEncoder.matches(currentPwd, new String(encryptedHisPassword)))
                    return true;
            }
        }
        return false;
    }

    /**
     * 密碼符合最小長度
     */
    public boolean isShortPwdLimit(String currentPwd) throws NotFoundException {
        Map<String, char[]> lenLimitMap = getPasswordParameterMemo(PasswordParameter.PWD_LEN_LIMIT);
        if (lenLimitMap.containsKey("Y")) {
            int lenLimitValue = Integer.parseInt(new String(lenLimitMap.get("Y")));

            return currentPwd.length() < lenLimitValue;
        }
        return false;
    }

    /**
     * pwdModifyReminderDaysMap
     */
    public boolean isSameAccountPwd(String username, String secondPwd) throws NotFoundException {
        Map<String, char[]> sameAccountMap = getPasswordParameterMemo(PasswordParameter.PWD_SAME_ACCOUNT_FLAG);
        if (sameAccountMap.containsKey("Y"))
            return StringUtils.equals(username, secondPwd);

        return false;
    }

    /**
     * 密碼符合正則表示法
     */
    public boolean isMatchRegular(String password) throws NotFoundException {
        Map<String, char[]> engNumMap = getPasswordParameterMemo(PasswordParameter.PWD_ENG_NUM_FLAG);
        Map<String, char[]> lenLimitMap = getPasswordParameterMemo(PasswordParameter.PWD_LEN_LIMIT);

        if(lenLimitMap.containsKey("Y")){
            if (engNumMap.containsKey("Y")) {
                final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{" + new String(lenLimitMap.get("Y")) + ",30}$");

                return PASSWORD_PATTERN.matcher(password).matches();
            }
        }
        return true;
    }

    /**
     * 登入時檢核明文密碼規則
     */
    public boolean isValidatePwdRuleFailed(String password) throws NotFoundException {
        return !isMatchRegular(password) || isShortPwdLimit(password);
    }

    public boolean isCurrentPasswordAsDefault(String username, String password) throws NotFoundException {
        return isSameAccountPwd(username, password);
    }

    /**
     * 首次登入重設密碼(判斷HisPwd無資料)
     */
    public boolean isFirstLogin(BaseCfgPerson cfgPerson) throws Exception {
        Map<String, char[]> firstLoginMap = getPasswordParameterMemo(PasswordParameter.PWD_FIRST_LOGIN_FLAG);
        if (firstLoginMap.containsKey("Y")) {
            List<char[]> hisPwdMap = getHisPwd(cfgPerson.getName());

            return hisPwdMap.isEmpty() & passwordEncoder.matches(cfgPerson.getName(), cfgPerson.getPasswordNew());
        }
        return false;
    }

    /**
     * 密碼到期前提醒訊息
     */
    public Map<Boolean, String> checkReminderBeforePasswordExpires(BaseCfgPerson cfgPerson) throws Exception {
        Map<String, char[]> modifyReminderDaysMap = getPasswordParameterMemo(PasswordParameter.PWD_MODIFY_REMINDER_DAYS);
        Map<String, char[]> expirationModifyDaysMap = getPasswordParameterMemo(PasswordParameter.PWD_EXPIRATION_MODIFY_DAYS);

        Map<Boolean, String> map = new HashMap<>();
        Date lastModifiedPwdDate = cfgPerson.getFinalChangeDate();
        if (ObjectUtils.isEmpty(lastModifiedPwdDate)) {
            map.put(true, "");

            return map;
        }

        if (modifyReminderDaysMap.containsKey("Y")) {
            int expirationDays = Integer.parseInt(new String(expirationModifyDaysMap.get("Y")));
            Date expiryDate = DateTimeUtil.addDays(lastModifiedPwdDate, expirationDays);
            int reminderDays = Integer.parseInt(new String(modifyReminderDaysMap.get("Y")));
            if (new Date().before(expiryDate)) {
                int calculatedValue = DateTimeUtil.diffDay(new Date(), expiryDate);
                boolean isReminderNeeded = calculatedValue <= reminderDays;
                map.put(isReminderNeeded, Integer.toString(calculatedValue));

                return map;
            }
        }
        map.put(false, "");

        return map;
    }

    /**
     * 密碼已達到過期天數
     */
    public boolean isRequiredChangePwd(BaseCfgPerson cfgPerson) throws Exception {
        Date lastModifiedPwdDate = cfgPerson.getFinalChangeDate();
        if (ObjectUtils.isEmpty(lastModifiedPwdDate))
            return true;

        Map<String, char[]> pwdExpirationModifyDaysMap = getPasswordParameterMemo(PasswordParameter.PWD_EXPIRATION_MODIFY_DAYS);
        if (pwdExpirationModifyDaysMap.containsKey("Y")) {
            int expirationDays = Integer.parseInt(new String(pwdExpirationModifyDaysMap.get("Y")));
            Date expiryDate = DateTimeUtil.addDays(lastModifiedPwdDate, expirationDays);

            return expiryDate.before(new Date()) || DateTimeUtil.isEqualsDate(expiryDate, new Date());
        }
        return false;
    }

    private Map<String, char[]> getPasswordParameterMemo(PasswordParameter parameter) throws NotFoundException {
        GirConfigDB1Master entity = girConfigRepository.findByIDCode(parameter.getIdCode())
                .orElseThrow(() -> new NotFoundException(ERROR_MSG_PREFIX + parameter.getErrorMsg()));

        String memo = entity.getMemo();
        Map<String, char[]> map = new HashMap<>();
        if (memo.contains("-")) {
            String[] oneSplit = memo.split("-");
            map.put(oneSplit[0], oneSplit[1].toCharArray());

            return map;
        }
        map.put(memo, "".toCharArray());

        return map;
    }

    public PwdResDto validatePwdUpdate(String username, String firstPwd, String secondPwd) throws NotFoundException {
        PwdResDto resDto = new PwdResDto();
        resDto.setViewName("pwdchange");

        if (!StringUtils.equals(firstPwd, secondPwd)) {
            resDto.setErrMsg(ERROR_MSG_PREFIX + ERROR_MSG_PASSWORD_NOT_MATCH);
            return resDto;
        }

        if (isShortPwdLimit(firstPwd)) {
            resDto.setErrMsg(ERROR_MSG_PREFIX + PasswordParameter.PWD_LEN_LIMIT.getErrorMsg());
            return resDto;
        }

        if (isSameAccountPwd(username, secondPwd)) {
            resDto.setErrMsg(ERROR_MSG_PREFIX + PasswordParameter.PWD_SAME_ACCOUNT_FLAG.getErrorMsg());
            return resDto;
        }

        if (!isMatchRegular(secondPwd)) {
            resDto.setErrMsg(ERROR_MSG_PREFIX + PasswordParameter.PWD_ENG_NUM_FLAG.getErrorMsg());
            return resDto;
        }

        if (isDuplicateHisPwd(username, secondPwd)) {
            resDto.setErrMsg(ERROR_MSG_PREFIX + PasswordParameter.PWD_UNIQUE_IN_PREVIOUS.getErrorMsg());
            return resDto;
        }
        return resDto;
    }

    /**
     * 取得密碼的可錯誤次數
     */
    public int getPasswordWrongTimes() throws NotFoundException {

        Map<String, char[]> wrongTimesMap = getPasswordParameterMemo(PasswordParameter.PWD_WRONG_TIMES);

        return wrongTimesMap.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getKey(), "Y"))
                .map(Map.Entry::getValue)
                .mapToInt(num -> Integer.parseInt(new String(num)))
                .findFirst()
                .orElseGet(() -> 0);

    }

}