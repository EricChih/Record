package com.ub.gir.web.service;

import com.ub.gir.web.dto.girConfig.GirConfigResDto;
import com.ub.gir.web.dto.girConfig.UpdatePwdReqDto;
import com.ub.gir.web.dto.girConfig.UpdateScheduleReqDto;

import java.util.List;
import java.util.TreeMap;

public interface ConfigService {
    TreeMap<String, String> getAllSiteMap();

    TreeMap<String, String> getAllRoleMap();

    TreeMap<String, String> getAllModuleMap();

    TreeMap<String, String> getModuleMapByRole(String role);

    TreeMap<String, String> getAllActMap();

    TreeMap<String, String> getActMapByRole(String role);

    GirConfigResDto getSys();

    int getLogKeepDays();

    void updateLogKeepDays(String keepDays, String username);

    void updateSysPwd(UpdatePwdReqDto dto,String username);

    void updateSchedule(UpdateScheduleReqDto dto, String username);

    List<String> getUserSites(String siteIpCode);
}