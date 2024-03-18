package com.ub.gir.web.controller.view;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.ub.gir.web.dto.keeprec.KeepRecDto;
import com.ub.gir.web.dto.keeprec.SearchKeepRecForm;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.KeepRecService;
import com.ub.gir.web.service.LogService;
import com.ub.gir.web.service.UserService;
import com.ub.gir.web.util.DateTimeUtil;
import com.ub.gir.web.util.HtmlEscapeUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/keeprec", "/keepreclist"})
public class KeepRecController {

    @Autowired
    private UserService userService;

    @Autowired
    private KeepRecService keeprecService;

    @Autowired
    private LogService logService;


    @GetMapping({"", "/all"})
    public ModelAndView getAllSys(@AuthenticationPrincipal User user) throws IllegalAccessException {
        User encodeUser = HtmlEscapeUtils.escapeObject(user);
        UserDto loginUser = userService.getUserByUsername(encodeUser.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("searchRecForm", new SearchKeepRecForm());
        modelAndView.setViewName("keepreclist");

        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //錄音檔依參數搜尋
    @PostMapping("/search")
    public ModelAndView searchRec(@ModelAttribute SearchKeepRecForm searchRecForm,
                                  @AuthenticationPrincipal User user) throws IllegalAccessException {
        String encodeUsername = HtmlEscapeUtils.escapeString(user.getUsername());
        String userrole = userService.getUserRoleByUsername(encodeUsername);
        List<KeepRecDto> searchReclist;
        SearchKeepRecForm encodeForm = new SearchKeepRecForm();
        encodeForm.setAni(HtmlEscapeUtils.escapeString(searchRecForm.getAni()));
        encodeForm.setAgentId(HtmlEscapeUtils.escapeString(searchRecForm.getAgentId()));
        encodeForm.setTheYear(HtmlEscapeUtils.escapeString(searchRecForm.getTheYear()));
        encodeForm.setTheMonth(HtmlEscapeUtils.escapeString(searchRecForm.getTheMonth()));
        encodeForm.setCustomerId(HtmlEscapeUtils.escapeString(searchRecForm.getCustomerId()));
        encodeForm.setCallDir(HtmlEscapeUtils.escapeString(searchRecForm.getCallDir()));
        encodeForm.setCallTypeName(HtmlEscapeUtils.escapeString(searchRecForm.getCallTypeName()));
        encodeForm.setAgentDN(HtmlEscapeUtils.escapeString(searchRecForm.getAgentDN()));

        //login user data
        UserDto loginUser = userService.getUserByUsername(encodeUsername);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("searchRecForm", encodeForm); //搜尋條件
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.setViewName("keepreclist");
        //撈取搜尋結果資料
        if (userrole.equalsIgnoreCase("superadmin") || userrole.equalsIgnoreCase("admin") ||
                userrole.equalsIgnoreCase("manager")) { //若為supadmin + admin + manager 跨部門搜尋
            searchReclist = keeprecService.searchRec(encodeForm, encodeUsername);
        } else { //supervisor 要依照自己+自己的audit list 搜尋
            searchReclist = keeprecService.searchSupRec(encodeForm, encodeUsername);
        }
        if (searchReclist.isEmpty()) {
            modelAndView.getModelMap().addAttribute("dataNotExistErr", true);
            return modelAndView;
        }
        modelAndView.getModelMap().addAttribute("keeprecs", searchReclist);
        return modelAndView;
    }//--------------------------------------------------------------------------------------------------

    //解除永久錄音檔
    @PostMapping("/delkeep")
    public ModelAndView delKeepRec(@RequestParam(name = "delKeepRecConnID") String delKeepConnID,
                                   @RequestParam(name = "keepRecDate") String delkeeprecDate,
                                   @AuthenticationPrincipal User user) throws ParseException, IOException {
        keeprecService.delKeepRec(delKeepConnID, delkeeprecDate, user.getUsername());

        return null;
    }//--------------------------------------------------------------------------------------------------

    //音檔播放與下載
    @GetMapping("/recplay/{recdate}/{recConnId}")
    public ModelAndView goRecordplay(@PathVariable("recdate") String recdate,
                                     @PathVariable("recConnId") String recConnid,
                                     @AuthenticationPrincipal User user) {
        if (StringUtils.isEmpty(recdate))
            throw new IllegalArgumentException("音檔日期 不可為空");
        if (StringUtils.isEmpty(recConnid))
            throw new IllegalArgumentException("音檔Id 不可為空");
        String encodeRecDate = HtmlEscapeUtils.escapeString(recdate);
        String encodeRecConnid = HtmlEscapeUtils.escapeString(recConnid);
        KeepRecDto theRec = keeprecService.getRecByFilename(encodeRecConnid, user);
        String endDateTime = DateTimeUtil.startDatetimeAddLen(theRec.getStartDate(), theRec.getLengthhhmmss());
        theRec.setEndDate(endDateTime);

        String downloadUrl = "/keepplay/" + encodeRecDate + "/" + encodeRecConnid;
        String playUrl = "/keepplay/" + encodeRecDate + "/" + encodeRecConnid;
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("recordurl", downloadUrl);
        modelAndView.addObject("recordgvp", playUrl);
        modelAndView.addObject("therec", theRec);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);

        modelAndView.setViewName("recplay");
        return modelAndView;
    }

    //紀錄下載log
    @PostMapping("/keepdlreclog")
    public void dlreclog(@RequestParam(name = "keeprecfilename") String filename,
                         @AuthenticationPrincipal User user) throws ParseException, IOException {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("keepreclist");
        logDto.setActionType("download");
        logDto.setInfo("下載永久音檔: " + filename);
        logService.addLog(logDto, user.getUsername());
    }

    //紀錄匯出Excel log
    @GetMapping("/keeprecexcel")
    public void keeprecexcel(@AuthenticationPrincipal User user) throws ParseException, IOException {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("keepreclist");
        logDto.setActionType("excel");
        logDto.setInfo("匯出永久音檔Excel資料表 ");
        logService.addLog(logDto, user.getUsername());
    }
}
