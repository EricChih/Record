package com.ub.gir.web.controller.view;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.rec.RecDto;
import com.ub.gir.web.dto.rec.SearchRecForm;
import com.ub.gir.web.dto.user.UserDto;
import com.ub.gir.web.service.*;
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
@RequestMapping({"/rec", "/reclist"})
public class RecController {
    @Autowired
    private UserService userService;

    @Autowired
    private RecService recService;

    @Autowired
    private KeepRecService keepRecService;

    @Autowired
    private LogService logService;

    @GetMapping("/all")
    public ModelAndView getAllSys(@AuthenticationPrincipal User user, HttpServletRequest request) {
        UserDto loginUser = userService.getUserByUsername(user.getUsername());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);
        modelAndView.getModelMap().addAttribute("searchRecForm", new SearchRecForm());
        modelAndView.setViewName("reclist");

        return modelAndView;
    }


    @PostMapping("/fetchRecData")
    public List<RecDto> fetchRecData(@Valid @RequestBody SearchRecForm searchRecForm,
                                     @AuthenticationPrincipal User user) {
        String username = HtmlEscapeUtils.escapeString(user.getUsername());
        String userrole = userService.getUserRoleByUsername(user.getUsername());

        // supervisor 只能搜尋自己部門
        List<RecDto> searchReclist;
        if (userrole.equalsIgnoreCase("supervisor")) {
            searchReclist = recService.searchSupRec(searchRecForm, username);
        } else {
            searchReclist = recService.searchRec(searchRecForm, username);
        }
        return searchReclist;
    }

    @PostMapping("/keep")
    public void keepRec(@RequestParam(name = "keepRecConnID") String keepConnID,
                        @AuthenticationPrincipal User user) throws IOException, ParseException {
        recService.makeKeepRec(keepConnID, user.getUsername());
    }

    //解除永久錄音檔
    @PostMapping("/delkeep")
    public void delKeepRec(@RequestParam(name = "delKeepRecConnID") String delKeepConnID,
                           @RequestParam(name = "keepRecDate") String recdate,
                           @AuthenticationPrincipal User user) throws ParseException, IOException {
        keepRecService.delKeepRec(delKeepConnID, recdate, user.getUsername());
    }

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
        String encodeRecConnId = HtmlEscapeUtils.escapeString(recConnid);
        RecDto theRec = recService.getRecByFilename(encodeRecConnId, user);
        String endDateTime = DateTimeUtil.startDatetimeAddLen(theRec.getStartDate(), theRec.getLengthhhmmss());
        theRec.setEndDate(endDateTime);

        String downloadUrl = "/play/" + encodeRecDate + "/" + encodeRecConnId;
        String playUrl = "/play/" + encodeRecDate + "/" + encodeRecConnId;
        UserDto loginUser = userService.getUserByUsername(user.getUsername());

        String targetFile = FileSaveConfig.getWithinYearFolder() + encodeRecDate + "/" + encodeRecConnId;

        Path filePath = FileSystems.getDefault().getPath(targetFile);
        if (!Files.exists(filePath) ) {
            throw new IllegalArgumentException("實體音檔不存在");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("recordurl", downloadUrl);
        modelAndView.addObject("recordgvp", playUrl);
        modelAndView.addObject("therec", theRec);
        modelAndView.getModelMap().addAttribute("loginuser", loginUser);

        modelAndView.setViewName("recplay");

        return modelAndView;
    }

    //紀錄下載log
    @PostMapping("/dlreclog")
    public void dlreclog(@RequestParam(name = "recfilename") String filename,
                         @AuthenticationPrincipal User user) throws ParseException, IOException {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("reclist");
        logDto.setActionType("download");
        logDto.setInfo("下載一般音檔: " + filename);
        logService.addLog(logDto, user.getUsername());
    }

    //紀錄匯出Excel log
    @GetMapping("/recexcel")
    public void recexcel(@AuthenticationPrincipal User user) throws ParseException, IOException {
        LogDto logDto = new LogDto();
        logDto.setFunctionName("reclist");
        logDto.setActionType("excel");
        logDto.setInfo("匯出一般音檔Excel資料表 ");
        logService.addLog(logDto, user.getUsername());
    }
}