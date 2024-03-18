package com.ub.gir.web.service;


import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.entity.db1.master.DepDB1Master;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepDB1MasterRepository;
import com.ub.gir.web.util.HtmlEscapeUtils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
public class RecYearService {
    @Autowired
    private DepDB1MasterRepository depRepo;
    @Autowired
    private CfgPersonDB1MasterRepository cfgPersonDB1MasterRepository;
    @Autowired
    private LogService logService;
    @PersistenceContext(unitName = "db1MasterPersistenceUnit")
    private EntityManager entityManager;
    @Value("${ub.view.page.max-size}")
    private int maxsize;

    private String DepID;
    private String DepName;
    private String SiteID;
    private String Status;
    private String DepIDinUnisys = "";
    private String PlaceGroup = "";
    private int RecKeepYear = 7;
    private String GroupTeam = "";
    private String errmsg; //非DB欄位,後端錯誤訊息
    private String AuditGroup = "";

    // 統計目前dep Table裡的資料總筆數
    public Long count() {
        return depRepo.count();
    }

    // get all department return List<UBGIR_DepService> with default maxsize
    public List<RecYearService> getAllDep() {
        List<String> datalist = depRepo.getAllDep(this.maxsize);
        List<RecYearService> deplist = convertStringToDServiceBean(datalist);

        return deplist;
    }//-------------------------------------------------------------------------------------

    // Get dep list map Status=ON
    public TreeMap<String, String> getAllDepMap() {
        List<String> alldep = depRepo.getAllDeplist();

        return convertListStringToMap(alldep);
    }//-------------------------------------------------------------------------------------

    // Get dep list map Status=ALL
    public TreeMap<String, String> getAllOnOffDepMap() {
        List<String> alldep = depRepo.getAllOnOffDeplist();
        return convertListStringToMap(alldep);
    }//-------------------------------------------------------------------------------------

    // Get dep list map
    public TreeMap<String, String> getAllMgrDepMap(String depId, String groupTeam) {
        try {
            // 如果 depId 是 "TD"，處理代理部門
            List<String> allDep;
            if (depId.trim().equalsIgnoreCase("TD")) {
                String sql;
                List<Object[]> searchResult;
                Map<String, Object> parameters = new HashMap<>();

                if (StringUtils.isNotEmpty(groupTeam)) {
                    String[] groupTeamParts = groupTeam.split(";");
                    sql = "SELECT d.DepID, d.DepName FROM DepDB1Master d WHERE d.DepID IN (:groupTeam)";
                    parameters.put("groupTeam", Arrays.asList(groupTeamParts));
                } else {
                    sql = "SELECT d.DepID, d.DepName FROM DepDB1Master d WHERE d.DepID = :depId";
                    parameters.put("depId", depId);
                }
                Query query = entityManager.createQuery(sql);
                query = parameters.entrySet().stream()
                        .reduce(query,
                                (q, entry) -> q.setParameter(entry.getKey(), entry.getValue()),
                                (a, b) -> b);

                searchResult = query.getResultList();
                allDep = searchResult.stream()
                        .map(result -> result[0] + "," + result[1])
                        .collect(Collectors.toList());
            } else {
                // 查自己的部門
                allDep = depRepo.getMgrDeplist(depId);

            }
            return convertListStringToMap(allDep);
        } catch (Exception e) {
            log.error("Error getAllMgrDepMap : {}", e.getMessage(), e);
        }
        return new TreeMap<>();
    }

    // Get dep list by location 含抓代理部門
    public TreeMap<String, String> getDepListByUsername(String username, String depid) {
        TreeMap<String, String> deplist = getAllDepMap(); //全面部門的清單；depid對應中文名
        String GroupDepList = depRepo.getGroupTeamByDepid(depid); //抓此depid 有代管的GroupTeam
        String[] deplistparts = new String[]{""};
        if (GroupDepList!=null && GroupDepList.trim().length() > 0) {
            deplistparts = GroupDepList.split(";");
        }
        TreeMap<String, String> rtList = new TreeMap<String, String>();
        if (deplistparts.length > 1) {
            for (int i = 0; i < deplistparts.length; i++) {
                rtList.put(deplistparts[i], deplist.get(deplistparts[i]));
            }
        } else {
            rtList.put(depid, deplist.get(depid));
        }
        return rtList;
    }//-------------------------------------------------------------------------------------

    //input data List<String> convert to TreeMap Key, value
    private TreeMap<String, String> convertListStringToMap(List<String> datalist) {
        TreeMap<String, String> rtmap = new TreeMap<String, String>();
        String[] parts;

        for (int i = 0; i < datalist.size(); i++) {
            parts = datalist.get(i).split(",");
            rtmap.put(HtmlEscapeUtils.escapeString(parts[0]), HtmlEscapeUtils.escapeString(parts[1]));
        }
        return rtmap;
    }//----------------------------------------------------------------------------

    // Get one Dep data by given DepId
    public RecYearService getDepById(String empid) {
        List<String> updatedata = depRepo.getDepById(empid);
        List<RecYearService> updatelist = convertStringToDServiceBean(updatedata);

        return updatelist.get(0);
    }//-------------------------------------------------------------------------------------

    // Get DepName by given DepId
    public String getDepNameByDepid(String depid) {
        try {
            return depRepo.getDepNameByDepid(depid);
        } catch (Exception e) {
            log.error("Error to getDepNameByDepid");
            throw e;
        }
    }

    public String getGroupDepIDList(String depid) {
        try {
            return depRepo.getGroupDepIDList(depid);
        } catch (Exception e) {
            log.error("Error to getGroupDepIDList" + e.getMessage());
            throw e;
        }
    }

    // Search dep data by given parameters
    public List<RecYearService> searchDep(String sDepID, String sDepName,
                                          String username) {
        List<String> searchdata = new ArrayList<String>();
        if (sDepID.length() > 0) {
            searchdata = sDepName.length() > 0 ?
                    depRepo.searchbyIDName(sDepID, sDepName):depRepo.getDepById(sDepID);
        } else if (sDepName.length() > 0) { //沒選DepID
            searchdata = depRepo.getDepByName(sDepName);
        } else { //全沒選,回到default all
            searchdata = depRepo.getAllDep(this.maxsize);
        }

        return convertStringToDServiceBean(searchdata);
    }//-------------------------------------------------------------------------------------

    // Search Mgr dep data by given parameters,只搜自己和代管的部門
    public List<RecYearService> searchMgrDep(String sDepID, String sDepName, String username, String GroupTeam) {
        try {
            List<String> searchdata = new ArrayList<>();
            if (StringUtils.isNotEmpty(sDepID)) {
                searchdata = StringUtils.isNotEmpty(sDepName) ? depRepo.searchbyIDName(sDepID, sDepName):depRepo.getDepById(sDepID);
            } else if (StringUtils.isNotEmpty(sDepName)) { // 沒選DepID
                searchdata = depRepo.getDepByName(sDepName);
            } else {
                String[] groupTeamParts = GroupTeam.split(";");
                String jpql = "SELECT d.DepId, d.DepName, COALESCE(SITEID, 'NA') as SiteID, Status, " +
                        "REPLACE(COALESCE(DepIDinUnisys, 'NA'), ',', '|') as DepIDinUnisys, " +
                        "REPLACE(COALESCE(PlaceGroup, 'NA'), ',', '|') as PlaceGroup, RecKeepYear, " +
                        "COALESCE(GroupTeam, 'NA') as GroupTeam " +
                        "FROM dep d " +
                        "WHERE d.DepId IN :groupTeam";

                Query query = entityManager.createNativeQuery(jpql)
                        .setParameter("groupTeam", Arrays.asList(groupTeamParts));
                List<Object[]> searchResult = query.getResultList();
                for (Object[] result : searchResult) {
                    searchdata.add(result[0] + "," + result[1] + "," + result[2] + "," + result[3] + ","
                            + result[4] + "," + result[5] + "," + result[6] + "," + result[7]);
                }
            }
            return convertStringToDServiceBean(searchdata);
        } catch (Exception e) {
            log.error("Error searchMgrDep : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    //更新部門資料
    public void updateDepByDepID(RecYearService updatedep, String username) {
        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                depRepo.updateKeepYearByDepId(updatedep.getDepID(), updatedep.getRecKeepYear());
                depRepo.updategroupteamByDepId(updatedep.getDepID(), updatedep.getGroupTeam());
            }
            if(updatedep.getRecKeepYear()==999){
                String convert=updatedep.getRecKeepYear()== 999 ? "永久保留" : String.valueOf(updatedep.getRecKeepYear());
                LogDto logDto = new LogDto();
                logDto.setFunctionName("recyear");
                logDto.setActionType("UPDATE");
                logDto.setInfo("資料更新: DepID=" + updatedep.getDepID() + ", 保存年份=" + convert + ", by user= " + username);
                logService.addLog(logDto, username);
            }else {
            LogDto logDto = new LogDto();
            logDto.setFunctionName("recyear");
            logDto.setActionType("UPDATE");
            logDto.setInfo("資料更新: DepID=" + updatedep.getDepID() + ", 保存年份=" + updatedep.getRecKeepYear() + ", by user= " + username);
            logService.addLog(logDto, username);}

        } catch (Exception e) {
            log.error("Error to updateDepByDepID" + e.getMessage());
            throw e;
        }
    }//-------------------------------------------------------------------------------------

    // STOP Dep by given DepID
    public void stopDepByDepId(String stopDepId, String username) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                depRepo.stopByDepId(stopDepId);
            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("部門管理");
            logDto.setActionType("STOP");
            logDto.setInfo("停用: DepID= " + stopDepId + " by user= " + username);
            logService.addLog(logDto, username);

        } catch (Exception e) {
            log.error("Error to stopDepByDepId" + e.getMessage());
            throw e;
        }
    }//-------------------------------------------------------------------------------------

    // delete Dep by given DepID
    public void delDepByDepId(String delDepId, String username) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                depRepo.deleteByDepId(delDepId);
            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("depmgr");
            logDto.setActionType("DELETE");
            logDto.setInfo("刪除: DepID= " + delDepId + " by user= " + username);
            logService.addLog(logDto, username);

        } catch (Exception e) {
            log.error("Error to delDepByDepId" + e.getMessage());
            throw e;
        }

    }//-------------------------------------------------------------------------------------

    //共用功能 把List<String> 轉成List<UBGIR_DepService>
    private List<RecYearService> convertStringToDServiceBean(List<String> datalist) {
        List<RecYearService> dlist = new ArrayList<RecYearService>();
        RecYearService thedep;
        String[] parts;

        for (int i = 0; i < datalist.size(); i++) {
            parts = datalist.get(i).split(",");
            thedep = new RecYearService();
            thedep.setDepID(HtmlEscapeUtils.escapeString(parts[0]));
            thedep.setDepName(HtmlEscapeUtils.escapeString(parts[1]));
            thedep.setSiteID(HtmlEscapeUtils.escapeString(parts[2]));
            thedep.setStatus(HtmlEscapeUtils.escapeString(parts[3]));
            thedep.setDepIDinUnisys(HtmlEscapeUtils.escapeString(parts[4]));
            thedep.setPlaceGroup(HtmlEscapeUtils.escapeString(parts[5]));
            thedep.setRecKeepYear(Integer.parseInt(HtmlEscapeUtils.escapeString(parts[6])));
            thedep.setGroupTeam(HtmlEscapeUtils.escapeString(parts[7]));
            dlist.add(thedep);
        }
        return dlist;
    }//-------------------------------------------------------------------------------------

    //共用功能 把ServiceBean 轉成Entity Bean
    private DepDB1Master convertServiceToEntityBean(RecYearService adddep) {
        DepDB1Master thedep = new DepDB1Master();
        thedep.setDepID(adddep.getDepID());
        thedep.setDepName(adddep.getDepName());
        thedep.setSiteID(adddep.getSiteID());
        thedep.setStatus(adddep.getStatus());
        thedep.setDepIDinUnisys(adddep.getDepIDinUnisys());
        thedep.setPlaceGroup(adddep.getPlaceGroup());
        thedep.setRecKeepYear(adddep.getRecKeepYear());
        thedep.setGroupTeam(adddep.getGroupTeam());
        return thedep;
    }//-------------------------------------------------------------------------------------

    // Parameter setter and getter area
    public String getDepID() {
        return DepID.toUpperCase();
    }

    public void setDepID(String depID) {
        depID.replaceAll(",", ":");
        DepID = depID.toUpperCase();
    }

    public String getDepName() {
        return DepName;
    }

    public void setDepName(String depName) {
        //去除逗點
        depName.replaceAll(",", ":");
        //為空時放default = DepID
        if (depName.length() <= 0) {
            depName = getDepID();
        }
        DepName = depName;
    }

    public String getSiteID() {
        return SiteID;
    }

    public void setSiteID(String siteID) {
        SiteID = siteID;
    }

    public String getStatus() {
        return Status.toUpperCase();
    }

    public void setStatus(String status) {
        Status = status.toUpperCase();
    }

    public String getDepIDinUnisys() {
        return DepIDinUnisys;
    }

    public void setDepIDinUnisys(String depIDinUnisys) {
        if (depIDinUnisys==null) {
            depIDinUnisys = "";
        }
        DepIDinUnisys = depIDinUnisys;
    }

    public String getPlaceGroup() {
        return PlaceGroup;
    }

    public void setPlaceGroup(String placeGroup) {
        if (placeGroup==null) {
            placeGroup = "";
        }
        PlaceGroup = placeGroup;
    }

    public int getRecKeepYear() {
        return RecKeepYear;
    }

    public void setRecKeepYear(int recKeepYear) {
        RecKeepYear = recKeepYear;
    }

    public String getGroupTeam() {
        if (this.GroupTeam.equalsIgnoreCase("NA"))
            this.GroupTeam = "";
        return GroupTeam;
    }

    public void setGroupTeam(String groupTeam) {
        GroupTeam = groupTeam;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getAuditGroup() {
        if (this.AuditGroup.equalsIgnoreCase("NA"))
            this.AuditGroup = "";
        return AuditGroup;
    }

    public void setAuditGroup(String AuditGroup) {
        this.AuditGroup = AuditGroup;
    }

    @Override
    public String toString() {
        return "UBGIR_RecYearService data: [depRepo=" + depRepo + ", logService=" + logService + ", maxsize=" + maxsize
                + ", DepID=" + DepID + ", DepName=" + DepName + ", SiteID=" + SiteID + ", Status=" + Status
                + ", DepIDinUnisys=" + DepIDinUnisys + ", PlaceGroup=" + PlaceGroup + ", RecKeepYear=" + RecKeepYear
                + ", GroupTeam=" + GroupTeam + ", errmsg=" + errmsg + ", AuditGroup=" + AuditGroup + "]";
    }

}
