package com.ub.gir.web.controller.view;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.dto.hisRec.HisRecDto;
import com.ub.gir.web.dto.hisRec.SearchHisRecForm;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.exception.ErrorMessages;
import com.ub.gir.web.exception.ServiceException;
import com.ub.gir.web.service.HisRecService;
import com.ub.gir.web.service.KeepRecService;
import com.ub.gir.web.service.LogService;
import com.ub.gir.web.service.UserService;
import com.ub.gir.web.util.DateTimeUtil;
import com.ub.gir.web.util.HtmlEscapeUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/hisrec", "/hisreclist"})
@Slf4j
public class HisRecController {

    @Autowired
    private UserService userService;

    @Autowired
    private HisRecService hisrecService;

    @Autowired
    private KeepRecService keepRecService;
    @Autowired
    private LogService logService;

    //無預設的default
    @GetMapping({"", "/all"})
    public ModelAndView getHisRec(@AuthenticationPrincipal User user) {
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView("hisreclist");
        List<Integer> yearList = hisrecService.getTableYearByDataTable();
        if (hisrecService.isValidADYearsCheck(yearList)) {
            int dayOfMonth = getDefaultDayOfMonthByYearTable(yearList);
            modelAndView.getModelMap().addAttribute("yearList", yearList);
            modelAndView.getModelMap().addAttribute("dayOfMonth", dayOfMonth);
        } else {
            modelAndView.getModelMap().addAttribute("yearList", new ArrayList<>());
            modelAndView.getModelMap().addAttribute("dayOfMonth", 1);
        }
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("searchHisRecForm", new SearchHisRecForm());

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    /**
     * 設定永久錄音檔
     */
    @PostMapping("/keep")
    public void keepRec(@RequestParam(name = "keepRecConnID") String keepConnID,
                        @RequestParam(name = "keepRecDate") String keepRecDate,
                        @AuthenticationPrincipal User user) throws ParseException, IOException, NoSuchFieldException {
        if (StringUtils.isEmpty(keepConnID)) {
            throw new IllegalArgumentException("keepConnID 不可為空");
        }
        if (StringUtils.isEmpty(keepRecDate)) {
            throw new IllegalArgumentException("keepRecDate 不可為空");
        }
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        String filterDateStr = DateTimeUtil.filterBasicDateStr(keepRecDate);
        int year = DateTimeUtil.getYearByDateStr(filterDateStr);
        boolean isYearValid = checkYearValid(year);
        if (!isYearValid) {
            throw new IllegalArgumentException("年份不在有效範圍內");
        }
        hisrecService.makeKeepRec(keepConnID, String.valueOf(year), user.getUsername());
    }

    //解除永久錄音檔
    @PostMapping("/delkeep")
    public void delKeepRec(@RequestParam(name = "delKeepRecConnID") String delKeepConnID,
                           @RequestParam(name = "keepRecDate") String keepRecDate,
                           @AuthenticationPrincipal User user) throws ParseException, IOException {
        if (StringUtils.isEmpty(delKeepConnID)) {
            throw new IllegalArgumentException("keepConnID 不可為空");
        }
        if (StringUtils.isEmpty(keepRecDate)) {
            throw new IllegalArgumentException("keepRecDate 不可為空");
        }
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        String filterDateStr = DateTimeUtil.filterBasicDateStr(keepRecDate);
        int year = DateTimeUtil.getYearByDateStr(filterDateStr);
        boolean isYearValid = checkYearValid(year);
        if (!isYearValid) {
            throw new IllegalArgumentException("年份不在有效範圍內");
        }
        keepRecService.delKeepRec(delKeepConnID, filterDateStr, user.getUsername());
    }

    @PostMapping("/fetchRecData")
    public List<HisRecDto> fetchRecData(@Valid @RequestBody SearchHisRecForm searchHisRecForm,
                                        @AuthenticationPrincipal User user) {
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        String userrole = userService.getUserRoleByUsername(user.getUsername());
        Integer year = DateTimeUtil.filterYear(searchHisRecForm.getTheYear());
        boolean isYearValid = checkYearValid(year);
        Integer month = DateTimeUtil.filterMonth(searchHisRecForm.getTheMonth());
        if (!isYearValid) {
            throw new ServiceException("年份格式有誤");
        }
        int dayOfMonth = DateTimeUtil.getDayOfMonth(year, month);
        boolean isDayValid = checkDayValid(dayOfMonth, searchHisRecForm.getTheStartDay(), searchHisRecForm.getTheEndDay());
        if (!isDayValid) {
            throw new ServiceException("日期格式有誤");
        }
        List<HisRecDto> resList = new ArrayList<>();

        // supervisor 只能搜尋自己部門
        if (userrole.equalsIgnoreCase("supervisor")) {
            resList = hisrecService.searchSupRec(searchHisRecForm, user.getUsername(), loginUser.getLocation());
        } else {
            resList = hisrecService.searchRec(searchHisRecForm, user.getUsername(), loginUser.getLocation());
        }
        return resList;
    }

    //音檔播放與下載
    @GetMapping("/recplay/{recdate}/{recConnId}")
    public ModelAndView goRecordplay(@PathVariable("recdate") String recdate,
                                     @PathVariable("recConnId") String recConnId,
                                     @AuthenticationPrincipal User user) {
        if (StringUtils.isEmpty(recdate))
            throw new IllegalArgumentException("音檔日期 不可為空");
        if (StringUtils.isEmpty(recConnId))
            throw new IllegalArgumentException("音檔Id 不可為空");
        String encodeRecDate = HtmlEscapeUtils.escapeString(recdate);
        String encodeRecConnId = HtmlEscapeUtils.escapeString(recConnId);

        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        String filterRecDateStr = DateTimeUtil.filterDateStrByPattern(encodeRecDate, "yyyyMMdd");
        HisRecDto theRec = hisrecService
                .getRecByFilename(encodeRecConnId, filterRecDateStr, user.getUsername(), loginUser.getLocation());

        String enddattime = DateTimeUtil.startDatetimeAddLen(theRec.getStartDate(), theRec.getLengthhhmmss());
        theRec.setEndDate(enddattime);
        String downloadurl = "/hisplay/" + filterRecDateStr + "/" + encodeRecConnId;
        String playurl = "/hisplay/" + filterRecDateStr + "/" + encodeRecConnId;

        String targetFile = FileSaveConfig.getMoreThanYearFolder() + filterRecDateStr + "/" + encodeRecConnId;

        Path filePath = FileSystems.getDefault().getPath(targetFile);
        if (!Files.exists(filePath) ) {
            throw new IllegalArgumentException("實體音檔不存在");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("recordurl", downloadurl);
        modelAndView.addObject("recordgvp", playurl);
        modelAndView.addObject("therec", theRec);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("recplay");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //紀錄下載log
    @PostMapping("/hisdlreclog")
    public void dlreclog(@RequestParam(name = "hisrecfilename") String filename,
                         @AuthenticationPrincipal User user) throws ParseException, IOException {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("hisreclist");
        logDto.setActionType("download");
        logDto.setInfo("下載歷史音檔: " + filename);
        logService.addLog(logDto, user.getUsername());
    }

    //紀錄匯出Excel log
    @GetMapping("/hisrecexcel")
    public void hisrecexcel(@AuthenticationPrincipal User user) throws ParseException, IOException {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("hisreclist");
        logDto.setActionType("excel");
        logDto.setInfo("匯出歷史音檔Excel資料表 ");
        logService.addLog(logDto, user.getUsername());
    }

    /**
     * 取得 歷史音檔名 DB TableName(ex.2021,2022)
     */
    @GetMapping("/tableYear")
    public ResponseEntity<?> getTableYearOfDataSource(@AuthenticationPrincipal User user) {
        List<Integer> yearTableNameList = hisrecService.getTableYearByDataTable();
        if (yearTableNameList.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorMessages.RESOURCE_NOT_FOUND.toObject());

        return ResponseEntity.ok(yearTableNameList);
    }

    // 檢查年份是否有效，年份涉及到table name
    private boolean checkYearValid(Integer year) {
        List<Integer> yearTableNameList = hisrecService.getTableYearByDataTable()
                .stream()
                .filter(this::isValidADYear)
                .collect(Collectors.toList());
        return year!=null && yearTableNameList.contains(year);
    }

    private boolean isValidADYear(Integer year) {
        int currentYear = Year.now().getValue();
        return year >= 1990 && year <= currentYear;
    }

    private boolean checkDayValid(int dayOfMonth, String reqStartDay, String reqEndDay) {
        try {
            int startDay = Integer.parseInt(reqStartDay);
            int endDay = Integer.parseInt(reqEndDay);
            return endDay <= dayOfMonth && startDay <= endDay;
        } catch (NumberFormatException e) {
            log.error("Error to check day valid : {}", e.getMessage(), e);
        }
        return false;
    }

    private int getDefaultDayOfMonthByYearTable(List<Integer> yearList) {
        int defaultYear;
        if (!yearList.isEmpty()) {
            defaultYear = yearList.get(0);
        } else {
            defaultYear = DateTimeUtil.getNowYear();
        }
        return DateTimeUtil.getDayOfMonth(defaultYear, 1);
    }

}