package com.ub.gir.web.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author elliot
 * @version 1.0
 * @date 2023/3/28
 */
@Slf4j
public class RocDateUtil {
    public static void main(String[] args) throws ParseException {
        Date previousYear = DateTimeUtil.addYear(new Date(), -1);
        log.debug(convertDate(previousYear, "yyyy-MM-dd HH:mm:ss", "yyyyMMdd").toString());

        Date fileDate = convertStringDateToDate("20220330", "yyyyMMdd",false);
        log.debug(fileDate.toString());

    }

    public RocDateUtil() throws ParseException {
        log.debug(convertDateType(new Date(), "YYYY-MM-DD", "yyy 年 MM 月 dd 日"));
        log.debug(convertDateType(new Date(), "yyyy-MM-dd HH:mm:ss", "yyy-MM-dd HH:mm:ss"));
        log.debug(convertDate("20220214", "yyyyMMdd", "yyyMMdd"));
        log.debug(convertDate("20220214", "yyyyMMdd", "yyy/MM/dd"));
        log.debug(convertDate("2022/02/14", "yyyy/MM/dd", "yyyMM"));
        log.debug(convertDate("2022/02/14", "yyyy/MM/dd", "yyyMMdd"));
        log.debug(convertDate("2022/02/14", "yyyy/MM/dd", "yyy/MM/dd"));
        log.debug(convertDate("2022/02/14", "yyyy/MM/dd", "yyy-MM-dd"));
        log.debug(convertDate("2022/02/14", "yyyy/MM/dd", "yy-MM-dd"));
        log.debug(convertDate("111/02/14", "yyy/MM/dd", "yyyy/MM/dd"));
        log.debug(convertDate("111-02-14", "yyy-MM-dd", "yyyy/MM/dd"));
        log.debug(convertDate("民國 111 年 01 月 14 日", "民國 yyy 年 MM 月 dd 日", "西元 yyyy 年 MM 月 dd 日"));
        log.debug(convertDate("西元 2022 年 01 月 14 日", "西元 yyyy 年 MM 月 dd 日", "民國 yyy 年 MM 月 dd 日"));
    }

    /**
     * 透過 StringDate 轉換日期
     *
     * @return String
     */
    public static String convertDate(String adDate, String beforeFormat, String afterFormat) throws ParseException {
        if (StringUtils.isEmpty(adDate))
            return adDate;
        SimpleDateFormat df4 = new SimpleDateFormat(beforeFormat);
        SimpleDateFormat df2 = new SimpleDateFormat(afterFormat);
        Calendar cal = Calendar.getInstance();
        cal.setTime(df4.parse(adDate));

        int currentYear = cal.get(Calendar.YEAR);
        boolean isYearLessThan1911 = currentYear < 1911;
        boolean isYearGreaterThan1911 = currentYear > 1911;
        cal.add(Calendar.YEAR, afterFormat.contains("yyyy") ?
                (isYearLessThan1911 ? +1911 : 0) : (isYearGreaterThan1911 ? -1911 : 0));

        return df2.format(cal.getTime());
    }

    /**
     * 透過 Date 轉換日期
     *
     * @return Date
     */
    public static Date convertDate(Date adDate, String beforeFormat, String afterFormat) throws ParseException {
        if (ObjectUtils.isEmpty(adDate))
            return adDate;
        SimpleDateFormat df4 = new SimpleDateFormat(beforeFormat);
        SimpleDateFormat df2 = new SimpleDateFormat(afterFormat);
        Calendar cal = Calendar.getInstance();
        cal.setTime(df4.parse(df4.format(adDate)));

        int currentYear = cal.get(Calendar.YEAR);
        boolean isYearLessThan1911 = currentYear < 1911;
        boolean isYearGreaterThan1911 = currentYear > 1911;
        cal.add(Calendar.YEAR, afterFormat.contains("yyyy") ?
                (isYearLessThan1911 ? +1911 : 0) : (isYearGreaterThan1911 ? -1911 : 0));

        return df2.parse(df2.format(cal.getTime()));
    }

    /**
     * 透過 Date 轉換日期
     *
     * @return String
     */
    public static String convertDateType(Date adDate, String beforeFormat, String afterFormat) throws ParseException {
        if (ObjectUtils.isEmpty(adDate))
            return null;
        SimpleDateFormat df4 = new SimpleDateFormat(beforeFormat);
        SimpleDateFormat df2 = new SimpleDateFormat(afterFormat);
        Calendar cal = Calendar.getInstance();
        cal.setTime(df4.parse(df4.format(adDate)));

        int currentYear = cal.get(Calendar.YEAR);
        boolean isYearLessThan1911 = currentYear < 1911;
        boolean isYearGreaterThan1911 = currentYear > 1911;
        cal.add(Calendar.YEAR, afterFormat.contains("yyyy") ?
                (isYearLessThan1911 ? +1911 : 0) : (isYearGreaterThan1911 ? -1911 : 0));

        return df2.format(cal.getTime());
    }

    /**
     * 透過 StringDate 轉換 Date
     *
     * @return Date
     */
    public static Date convertStringDateToDate(String stringDate, String beforeFormat, boolean isConvertYear) throws ParseException {
        if (StringUtils.isEmpty(stringDate))
            return null;
        SimpleDateFormat df4 = new SimpleDateFormat(beforeFormat);
        Calendar cal = Calendar.getInstance();
        cal.setTime(df4.parse(stringDate));

        if (isConvertYear) {
            int currentYear = cal.get(Calendar.YEAR);
            boolean isYearLessThan1911 = currentYear < 1911;
            boolean isYearGreaterThan1911 = currentYear > 1911;
            cal.add(Calendar.YEAR, beforeFormat.contains("yyyy") ?
                    (isYearGreaterThan1911 ? -1911 : 0) : (isYearLessThan1911 ? +1911 : 0));
        }

        return cal.getTime();
    }
}