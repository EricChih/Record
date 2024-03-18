package com.ub.gir.web.util;


import com.ub.gir.web.exception.BusinessException;
import com.ub.gir.web.exception.ServiceException;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author elliot
 * @version 1.0
 * @date 2023/3/28
 */
@Slf4j
public class DateTimeUtil {

    /**
     * 取得現在時間的民國年月日
     *
     * @return 日期字串 yyyMMdd
     */
    public static String getNowDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR) - 1911;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String yearString = year + "";
        String monthString = month < 10 ? "0" + month : String.valueOf(month);
        String dayString = day < 10 ? "0" + day : String.valueOf(day);

        return yearString + monthString + dayString;
    }

    /**
     * 取得現在時間的時分鐘
     *
     * @return hhmmss
     */
    public static String getNowTime() {
        Date now = new Date();
        SimpleDateFormat getTime = new SimpleDateFormat("hhmmss");

        return getTime.format(now);
    }

    /**
     * 取得編輯時間
     *
     * @param editDate 日期 1101201
     * @param editTime 時間 190000
     * @return yyyy/MM/dd HH:mm:ss 格式
     * @throws ParseException
     */
    public static String getResponseRegularEditTime(String editDate, String editTime)
        throws ParseException {
        SimpleDateFormat dateToString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        if (editDate == null || editTime == null) {
            return null;
        }
        int year = Integer.parseInt(editDate.substring(0, 3)) + 1911;
        String month = editDate.substring(3, 5);
        String day = editDate.substring(5, 7);
        String hour = editTime.substring(0, 2);
        String minute = editTime.substring(2, 4);
        String second = editTime.substring(4, 6);
        String editDateString = String
            .format("%s/%s/%s %s:%s:%s", year, month, day, hour, minute, second);
        Date result = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(editDateString);

        return dateToString.format(result);
    }

    /**
     * 轉換日期字串
     *
     * @param date 日期 YYYMMDD
     * @return 日期
     */
    public static Date getDateByYYYMMDD(String date) throws ParseException {
        String YYY = date.substring(0, 3);
        int YYYY = Integer.parseInt(YYY) + 1911;
        String MM = date.substring(3, 5);
        String DD = date.substring(5, 7);
        String editDateString = String.format("%s-%s-%s", YYYY, MM, DD);
        return new SimpleDateFormat("yyyy-MM-dd").parse(editDateString);
    }

    /**
     * <br>目的：取得系統日期
     * <br>參數：無
     * <br>傳回：傳回字串YYYYMMDD
     */
    public static String getYYYYMMDD() {
        StringBuilder sb = new StringBuilder();
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DATE);
        if (y <= 999) {
            sb.append("0");
        }
        sb.append(Integer.toString(y));
        if (m <= 9) {
            sb.append("0");
        }
        sb.append(Integer.toString(m));
        if (d <= 9) {
            sb.append("0");
        }
        sb.append(Integer.toString(d));
        return sb.toString();
    }

    /**
     * 轉換成民國日期字串
     *
     * @param date      日期字串
     * @param separator 分隔符號
     * @return 日期格式 YYYMMDD
     */
    public static String getYYYMMDD(String date, String separator) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date inputDate = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);
        int fullYear = calendar.get(Calendar.YEAR);
        String year = (fullYear - 1911) < 100 ? "0" + (fullYear - 1911) : String.valueOf(fullYear - 1911);
        String month = (calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1)
            : String.valueOf((calendar.get(Calendar.MONTH) + 1));
        String day = calendar.get(Calendar.DATE) < 10 ? "0" + calendar.get(Calendar.DATE)
            : String.valueOf(calendar.get(Calendar.DATE));
        return year + separator + month + separator + day;
    }

    /**
     * 取得幾年幾月的天數
     *
     * @param year  西元年
     * @param month 月
     * @return
     */
    public static int getDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        calendar.roll(Calendar.DATE, -1);

        return calendar.get(Calendar.DATE);
    }

    /**
     * 取得日期中的月份
     *
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 增加日期年份
     *
     * @param date   日期
     * @param amount 增加/減少多少年
     * @return 日期
     */
    public static Date addYear(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, amount);
        return calendar.getTime();
    }

    /**
     * 增加日期月份
     *
     * @param date   日期
     * @param amount 增加/減少多少月
     * @return 日期
     */
    public static Date addMonth(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, amount);
        return calendar.getTime();
    }

    /**
     * 原本的日期增加或減少天數
     *
     * @param date 日期
     * @param days 天數
     * @return 增加/減少的日期
     */
    public static Date addDays(Date date, Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * 增加/減少日期毫秒
     *
     * @param date   日期
     * @param amount 增加/減少多少毫秒
     * @return
     */
    public static Date addMillisecond(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MILLISECOND, amount);
        return calendar.getTime();
    }

    /**
     * 取得日期中的年份
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }

    public static int getYearByDateStr(String dateStr) {
        Date date = dateStrToDate(dateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }

    /**
     * 取得日期中的幾號
     *
     * @param date 日期
     * @return 幾號
     */
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 取得 該月份最後一天
     *
     * @return 最後一天日期
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, value);
        return cal.getTime();
    }

    /**
     * 設定幾年幾月幾日
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 幾年幾月幾日日期
     */
    public static Date setDate(Integer year, Integer month, Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }

    /**
     * 取得一天中最後的時間 2021/01/01/01 23:59:59.999
     *
     * @param date 日期
     * @return 一天最後的時間
     */
    public static Date getOverOfDate(Date date) {
        if (ObjectUtils.isEmpty(date)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 999);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        return calendar.getTime();
    }

    /**
     * 取得一天的開始時間 2021/01/01/01 00:00:00
     *
     * @param date 日期
     * @return 一天一開始的時間
     */
    public static Date getStartOfDate(Date date) {
        if (ObjectUtils.isEmpty(date)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }

    /**
     * 取得一年的第一天
     *
     * @param year 年
     * @return 一年的第一天
     */
    public static Date getStartOfYear(int year) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 取得一年最後一天
     *
     * @param year 年
     * @return 一年的最後一天
     */
    public static Date getEndOfYear(int year) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 計算兩個日期的年度清單
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 幾個月
     */
    public static List<Integer> getYearListBetweenDates(Date startDate, Date endDate) {
        List<Integer> yearList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int startYear = calendar.get(Calendar.YEAR);

        calendar.setTime(endDate);
        int endYear = calendar.get(Calendar.YEAR);

        for (int year = startYear; year <= endYear; year++) {
            yearList.add(year);
        }

        return yearList;
    }

    /**
     * 計算兩個日期的月份差
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 幾個月
     */
    public static int diffMonth(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        int startYear = startCalendar.get(Calendar.YEAR);
        int startMonth = startCalendar.get(Calendar.MONTH);
        int endYear = endCalendar.get(Calendar.YEAR);
        int endMonth = endCalendar.get(Calendar.MONTH);

        int result;
        if (startYear == endYear)
            result = endMonth - startMonth;
        else
            result = 12 * (endYear - startYear) + endMonth - startMonth;

        return result;
    }

    /**
     * 計算兩個日期的天數差
     *
     * @param startDate 開始四期
     * @param endDate   結束日期
     * @return 幾天
     */
    public static int diffDay(Date startDate, Date endDate) {
        long betweenDate = (endDate.getTime() - startDate.getTime()) / (1000 * 24 * 60 * 60);
        return (int) (betweenDate + 1);
    }

    /**
     * 取得當下西元年份
     *
     * @return 當下西元年份
     */
    public static int getNowYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 取得當天日期
     *
     * @return 當天日期
     */
    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * 取得 日期時間(HHMM)
     *
     * @param date 時間格式
     * @return HHMM
     */
    public static String convertDatetimeHHMM(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formatDate = df.format(date);
        String[] str = formatDate.split("\\s+|:");
        return str[1] + str[2];
    }

    /**
     * 判斷 日期是否相等(排除時間點)
     *
     * @param date1 時間
     * @param date2 時間
     * @return HHMM
     */
    public static boolean isEqualsDate(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return localDate1.isEqual(localDate2);
    }

    public static String startDatetimeAddLen(String startdatetime, String len) {
        LocalDateTime rtDateTime = null;
        String[] dateparts = startdatetime.split("\\."); //過濾掉日期最後面的0
        LocalTime ltime = LocalTime.parse(len);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.parse(dateparts[0], dtf);
        System.out.println("before add: "+ datetime.toString());
        datetime = datetime.plusHours(ltime.getHour());
        datetime = datetime.plusMinutes(ltime.getMinute());
        datetime = datetime.plusSeconds(ltime.getSecond());

        System.out.println("rec add on endtime: "+ datetime.toString() );
        String endtime = datetime.toString().replaceAll("T", " ");

        return endtime;
    }

    /**
     * 基本的 yyyy-MM-dd HH:mm:ss 格式轉字串
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 基本的日期字串 yyyy-MM-dd HH:mm:ss 轉 Date
     * @param dateStr
     * @return
     */
    public static Date dateStrToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        }catch (Exception e) {
            throw new IllegalArgumentException("日期格式錯誤");
        }
        return date;
    }

    public static String filterBasicDateStr(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            LocalDate date = LocalDate .parse(dateStr, formatter);
            if (date.isBefore(LocalDate.now())) {
                return dateStr;
            } else {
                throw new IllegalArgumentException("時間參數不得設定於未來");
            }
        }catch (DateTimeParseException e){
            throw new IllegalArgumentException("日期格式錯誤");
        }
    }

    public static String filterDateStrByPattern(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDate date = LocalDate .parse(dateStr, formatter);
            if (date.isBefore(LocalDate.now())) {
                return dateStr;
            } else {
                throw new IllegalArgumentException("時間參數不得設定於未來");
            }
        }catch (DateTimeParseException e){
            throw new IllegalArgumentException("日期格式錯誤");
        }
    }

    // 過濾年分
    public static Integer filterYear(String inputYear) {
        if (inputYear != null && inputYear.matches("\\d{4}")) {
            return Integer.parseInt(inputYear);
        } else {
           return null;
        }
    }

    // 過濾月份
    public static Integer filterMonth(String inputMonth) {
        try {
            Integer month = Integer.parseInt(inputMonth);
            return (month >= 1 && month <= 12) ? month : null;
        } catch (NumberFormatException e) {
            log.error("Error filterMonth : {}", e.getMessage(), e);
        }
        return null;
    }

    public static LocalDateTime dateStrToLocalDateTime(String dateStr){
        if(dateStr == null){
           throw new ServiceException("Date String is null");
        }
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static LocalDateTime dateToLocalDateTime(Date date){
        if(date == null){
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static boolean isDateTimeBetween(String dateStrToCompare, Date startDate, Date endDate) {
        boolean isAfter = true;
        boolean isBefore = true;
        LocalDateTime dateToCompare = dateStrToLocalDateTime(dateStrToCompare);
        LocalDateTime localStartDateTime = dateToLocalDateTime(startDate);
        LocalDateTime localEndDateTime = dateToLocalDateTime(endDate);
        // 如果使用 isAfter 會讓相同的時間的情況變成false，所以改用 "是否不在start time之前"，
        if(localStartDateTime != null){
            isAfter = !dateToCompare.isBefore(localStartDateTime);
        }

        // 如果使用 isBefore 會讓相同的時間的情況變成 false，所以改用 "是否不在end time之後"，
        if(localEndDateTime != null){
            isBefore = !dateToCompare.isAfter(localEndDateTime);
        }
        return isAfter && isBefore;
    }


}