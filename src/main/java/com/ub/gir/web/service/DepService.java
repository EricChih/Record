package com.ub.gir.web.service;


import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.ub.gir.web.dto.dep.DepDto;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.entity.db1.master.DepDB1Master;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepAgentIDDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepExtDB1MasterRepository;
import com.ub.gir.web.util.HtmlEscapeUtils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;


@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DepService {
    @Autowired
    private CfgPersonDB1MasterRepository cfgPersonDB1MasterRepository;

    @Autowired
    private DepDB1MasterRepository depRepo;

    @Autowired
    private LogService logService;

    @Autowired
    private DepExtDB1MasterRepository depExtRepo;

    @Autowired
    private DepAgentIDDB1MasterRepository depAgentIDRepo;

    @Resource
    private UserService userService;

    @PersistenceContext(unitName = "db1MasterPersistenceUnit")
    private EntityManager entityManager;

    //default listing maxsize
    @Value("${ub.view.page.max-size}")
    private int maxsize;

    private String DepID;
    private String DepName;
    private String SiteID;
    private String Status;
    private String DepIDinUnisys = "";
    private String PlaceGroup = "";
    private int RecKeepYear = 0;
    private String GroupTeam = "";

    private String AuditGroup = "";
    private String errmsg; //非DB欄位,後端錯誤訊息

	// 統計目前dep Table裡的資料總筆數
	public Long count() {
		return depRepo.count();
	}

	// get all department return List<UBGIR_DepService> with default maxsize
	public List<DepService> getAllDep(){
		List<String> datalist = depRepo.getAllDep(this.maxsize);
		List<DepService> deplist = convertStringToDServiceBean(datalist);

		return deplist;
	}//-------------------------------------------------------------------------------------

	// Get dep list map by location
	public TreeMap<String, String> getAllDepMap(String siteid) {
		List<String> depdata = depRepo.getDeplistBySiteid(siteid);

		return convertListStringToMap(depdata);
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
			String jpql;
			if (StringUtils.isNotEmpty(groupTeam))
				jpql = "SELECT DepId, DepName FROM dep WHERE DepID IN (:groupTeam)";
			else
				jpql = "SELECT DepId, DepName FROM dep WHERE DepID = :depId";

			Query query = entityManager.createNativeQuery(jpql);
			String[] groupTeamParts = groupTeam.split(";");
			if (StringUtils.isNotEmpty(groupTeam))
				query.setParameter("groupTeam", Arrays.asList(groupTeamParts));
			else
				query.setParameter("depId", depId);

			List<Object[]> searchResult = query.getResultList();
			List<String> allDep = searchResult.stream()
					.map(result -> result[0] + "," + result[1])
					.collect(Collectors.toList());

			return convertListStringToMap(allDep);
		} catch (Exception e) {
			log.error("Error getAllMgrDepMap : {}", e.getMessage(), e);
		}
        return new TreeMap<>();
    }

	// Get dep list by location 含抓代理部門
	public TreeMap<String, String>  getDepListByUsername(String username, String depid) {
		TreeMap<String, String> deplist = getAllOnOffDepMap(); //全面使用中部門的清單；depid對應中文名
		String GroupDepList = depRepo.getGroupTeamByDepid(depid); //抓此depid 有代管的GroupTeam
		String[] deplistparts = new String[]{""};
		if(GroupDepList!=null && !GroupDepList.trim().isEmpty()) {
			deplistparts = GroupDepList.trim().split(";");
		}
		TreeMap<String, String> depMap = new TreeMap<>();
		if(deplistparts.length>1) {
			for(int i=0; i<deplistparts.length; i++) {
				depMap.put(deplistparts[i], deplist.get(deplistparts[i]));
			}
			depMap.computeIfAbsent(depid, deplist::get);
		}
		else {
			depMap.put(depid, deplist.get(depid));
		}
		return depMap;
	}//-------------------------------------------------------------------------------------

	//input data List<String> convert to TreeMap Key, value
	private TreeMap<String, String> convertListStringToMap(List<String> datalist) {
		TreeMap<String, String> rtmap = new TreeMap<>();
        for (String s : datalist) {
			String[] parts = s.split(",");
            rtmap.put(HtmlUtils.htmlEscape(parts[0]), HtmlUtils.htmlEscape(parts[1]));
        }
		return rtmap;
	}//----------------------------------------------------------------------------

	// Get one Dep data by given DepId
	public DepService getDepById(String empid){
		List<String> updatedata = depRepo.getDepById(empid);
		List<DepService> updatelist = convertStringToDServiceBean(updatedata);

		return updatelist.get(0);
	}//-------------------------------------------------------------------------------------

	// Get DepName by given DepId
	public String getDepNameByDepid(String depid){
		try {
			return depRepo.getDepNameByDepid(depid);
		} catch (Exception e) {
			log.error("Error to getDepNameByDepid");
			throw e;
		}
	}

	// Get 代理Group DepID List by given DepId
	public String getGroupDepIDList(String depid){
		try {
       		return depRepo.getGroupDepIDList(depid);
		} catch (Exception e) {
			log.error("Error to getGroupDepIDList");
			throw e;
		}
	}

	// Search dep data by given parameters
	public List<DepService> searchDep(String sDepID, String sDepName){
		List<String> searchdata;
		if(!sDepID.isEmpty()) {//參數全填完全比對
				searchdata = !sDepName.isEmpty() ?
						depRepo.searchbyIDName(sDepID, sDepName) : depRepo.getDepById(sDepID);
		} else {
			searchdata = !sDepName.isEmpty() ?
					depRepo.getDepByName(sDepName) : depRepo.getAllDep(this.maxsize);
		}
		return convertStringToDServiceBean(searchdata);

	}//-------------------------------------------------------------------------------------

	// Search Mgr dep data by given parameters,只搜自己和代管的部門
	public List<DepService> searchMgrDep(String sDepID, String sDepName,String groupTeam){
		try {
			List<String> searchdata = new ArrayList<>();
			if (StringUtils.isNotEmpty(sDepID)) {
				searchdata = StringUtils.isNotEmpty(sDepName) ? depRepo.searchbyIDName(sDepID, sDepName) : depRepo.getDepById(sDepID);
			} else if (StringUtils.isNotEmpty(sDepName)) {
				searchdata = depRepo.getDepByName(sDepName);
			} else {
				String[] groupTeamParts = groupTeam.split(";");
				String jpql = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, " +
						"REPLACE(IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), ',', '|') as DepIDinUnisys, " +
						"REPLACE(IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'), ',', '|') as PlaceGroup, RecKeepYear, " +
						"IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam, " +
						"IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup " +
						"FROM dep WHERE DepID IN :groupTeam";

				Query query = entityManager.createNativeQuery(jpql)
						.setParameter("groupTeam", Arrays.asList(groupTeamParts));
				List<Object[]> searchResult = query.getResultList();

				for (Object[] result : searchResult) {
					searchdata.add(result[0] + "," + result[1] + "," + result[2] + "," + result[3] + ","
							+ result[4] + "," + result[5] + "," + result[6] + "," + result[7] + "," + result[8]);
				}
			}
			return convertStringToDServiceBean(searchdata);
		} catch (Exception e) {
			log.error("Error searchMgrDep : {}", e.getMessage(), e);
		}
        return Collections.emptyList();
    }//-------------------------------------------------------------------------------------


    // 新增部門data to DB
    public void addDep(DepDto addDep, String username) {
        depRepo.save(convertServiceToEntityBean(addDep));
        String managerDepId = userService.getDepIDByUsername(username);
        insertNewDepToManagerGroupTeam(managerDepId, addDep.getDepID());

        LogDto logDto = new LogDto();
        logDto.setFunctionName("depmgr");
        logDto.setActionType("ADD");
        logDto.setInfo("新增成功: DepID=" + addDep.getDepID() + " by user:" + username);
        logService.addLog(logDto, username);
    }

    //更新部門資料
    public void updateDepByDepID(DepService updatedep, String username) {

        try {

            String auditGroup = updatedep.getAuditGroup().replace(",", ";");

            if (ToolPlugins.checkAuthorization("authorization")) {
                depRepo.updateByDepId(updatedep.getDepID(), updatedep.getDepName(), updatedep.getSiteID(),
                         updatedep.getGroupTeam(), auditGroup);
            }

            LogDto logDto = new LogDto();
            logDto.setFunctionName("depmgr");
            logDto.setActionType("UPDATE");
            logDto.setInfo("資料更新: DepID=" + updatedep.getDepID() + " by user= " + username);
            logService.addLog(logDto, username);
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
            logDto.setFunctionName("depmgr");
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

            // 更新代管部門的groupTeam欄位
            updateGroupByDelDepId(delDepId);
            // 更新可調聽部門的AuditGroup欄位
            updateAuditGroupByDelDepId(delDepId);

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

    private void updateAuditGroupByDelDepId(String delDepId) {
        try {
            String jpql = "SELECT d FROM DepDB1Master d WHERE d.AuditGroup LIKE :delDepIdParam";
            List<DepDB1Master> groupTeamDeps = entityManager.createQuery(jpql, DepDB1Master.class)
                    .setParameter("delDepIdParam", "%" + delDepId + "%")
                    .getResultList();

            for (DepDB1Master dep : groupTeamDeps) {

                String[] auditGroupArr = dep.getAuditGroup().split(";");
                String newAuditGroup = Arrays.stream(auditGroupArr)
                        .filter(depId -> !depId.equals(delDepId))
                        .collect(Collectors.joining(";"));

                if (ToolPlugins.checkAuthorization("authorization")) {
                    depRepo.updateAuditGroupByDepId(newAuditGroup, dep.getDepID());
                }

            }

        } catch (Exception e) {
            log.error("Error update auditGroup by delDepId : {}", e.getMessage(), e);
        }
    }

    // 刪除部門後，更新有代管該部門的GroupTeam欄位
    public void updateGroupByDelDepId(String delDepId) {
        try {
            String jpql = "SELECT d FROM DepDB1Master d WHERE d.GroupTeam LIKE :delDepIdParam";
            List<DepDB1Master> groupTeamDeps = entityManager.createQuery(jpql, DepDB1Master.class)
                    .setParameter("delDepIdParam", "%" + delDepId + "%")
                    .getResultList();

            for (DepDB1Master dep : groupTeamDeps) {

                String[] groupTeamArr = dep.getGroupTeam().split(";");
                String newGroupTeam = Arrays.stream(groupTeamArr)
                        .filter(depId -> !depId.equals(delDepId))
                        .collect(Collectors.joining(";"));

                if (ToolPlugins.checkAuthorization("authorization")) {
                    depRepo.updateGroupTeamByDepId(newGroupTeam, dep.getDepID());
                }

            }

        } catch (Exception e) {
            log.error("Error update group by delDepId : {}", e.getMessage(), e);
        }

    }

    // 更新後取得部門清單
    public List<DepService> getDepListAfterUpdate(String groupTeam, String userRole) {
        try {
            List<String> searchdata = new ArrayList<>();
            if (userRole.equalsIgnoreCase("manager")) {
                // mgr 只撈自己部門
                List<String> groupTeamParts = Arrays.asList(groupTeam.split(";"));
                String sql = generateDepQuerySqlStr();
                Query query = entityManager.createNativeQuery(sql).setParameter("groupTeam", groupTeamParts);
                List<Object[]> searchResult = query.getResultList();
                for (Object[] result : searchResult) {
                    searchdata.add(result[0] + "," + result[1] + "," + result[2] + "," + result[3] + ","
                            + result[4] + "," + result[5] + "," + result[6] + "," + result[7] + "," + result[8]);
                }
            } else {
                searchdata = depRepo.getAllDep(this.maxsize);
            }

            return convertStringToDServiceBean(searchdata);
        } catch (Exception e) {
            log.error("Error getDepListAfterUpdate : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    // 取得代管部門，如果代管部門欄位本身未包含自己的部門，新增進去
    public String getDepGroupTeam(String depId) {
        String groupTeam = getGroupDepIDList(depId);
        List<String> groupList = StringUtils.isNotBlank(groupTeam) ?
                Arrays.stream(groupTeam.split(";")).collect(Collectors.toList()):new ArrayList<>();
        if (!groupList.contains(depId)) {
            groupList.add(depId);
        }
        return String.join(";", groupList);
    }

    public void insertNewDepToManagerGroupTeam(String managerDepId, String depIDForInsert) {

        try {

            String groupTeam = getDepGroupTeam(managerDepId);
            String newGroupTeam = groupTeam + ";" + depIDForInsert;

            if (ToolPlugins.checkAuthorization("authorization")) {
                depRepo.updateGroupTeamByDepId(newGroupTeam, managerDepId);
            }

        } catch (Exception e) {
            log.error("Error to insertNewDepToManagerGroupTeam" + e.getMessage());
            throw e;
        }

    }

    public Map<String, Object> checkDelDep(String delDepId) {
        Map<String, Object> responseMap = new HashMap<>();
        boolean isCfgPersonExist = cfgPersonDB1MasterRepository.existsByDepId(delDepId);
        boolean isDepExtExist = depExtRepo.existsByDepId(delDepId);
        boolean isDepAgentIdExist = depAgentIDRepo.existsByDepId(delDepId);
        List<String> auditDepNames = depRepo.searchByAuditGroup(delDepId);
        responseMap.put("isCfgPersonExist", isCfgPersonExist);
        responseMap.put("isDepExtExist", isDepExtExist);
        responseMap.put("isDepAgentIdExist", isDepAgentIdExist);
        responseMap.put("auditDepNames", String.join("、", auditDepNames));
        return responseMap;
    }

    public boolean checkDepExist(String depId) {

        try {

            if (ToolPlugins.checkAuthorization("authorization")) {
                return depRepo.existsByDepID(depId);
            }

            return false;

        } catch (Exception e) {
            log.error("Error to checkDepExist" + e.getMessage());
            throw e;
        }

    }

    //共用功能 把List<String> 轉成List<UBGIR_DepService>
    private List<DepService> convertStringToDServiceBean(List<String> datalist) {
        List<DepService> dlist = new ArrayList<>();
        DepService thedep;
        String[] parts;

        for (String s : datalist) {
            parts = s.split(",", -1);
            thedep = new DepService();
            thedep.setDepID(HtmlEscapeUtils.escapeString(parts[0]));
            thedep.setDepName(HtmlEscapeUtils.escapeString(parts[1]));
            thedep.setSiteID(HtmlEscapeUtils.escapeString(parts[2]));
            thedep.setStatus(HtmlEscapeUtils.escapeString(parts[3]));
            thedep.setDepIDinUnisys(HtmlEscapeUtils.escapeString(parts[4]));
            thedep.setPlaceGroup(HtmlEscapeUtils.escapeString(parts[5]));
            thedep.setRecKeepYear(Integer.parseInt(HtmlEscapeUtils.escapeString(parts[6])));
            thedep.setGroupTeam(HtmlEscapeUtils.escapeString(parts[7]));
            String auditGroup = parts[8].replace(";", ",");
            thedep.setAuditGroup(HtmlEscapeUtils.escapeString(auditGroup));
            dlist.add(thedep);
        }
        return dlist;
    }//-------------------------------------------------------------------------------------

    //共用功能 把ServiceBean 轉成Entity Bean
    private DepDB1Master convertServiceToEntityBean(DepDto adddep) {
        DepDB1Master thedep = new DepDB1Master();
        thedep.setDepID(adddep.getDepID());
        thedep.setDepName(adddep.getDepName());
        thedep.setSiteID(adddep.getSiteID());
        thedep.setStatus("ON");
        thedep.setDepIDinUnisys(adddep.getDepIDinUnisys());
        thedep.setPlaceGroup(adddep.getPlaceGroup());
        thedep.setRecKeepYear(adddep.getRecKeepYear());
        String groupteam = adddep.getGroupTeam();
        if (groupteam.isEmpty()) {
            thedep.setGroupTeam(groupteam);
        } else {
            String newStr = groupteam.replace(",", ";");
            thedep.setGroupTeam(newStr);
        }

        List<String> auditGroupList = adddep.getAuditGroup();
        if (!auditGroupList.isEmpty()) {
            String newStr = String.join(",", auditGroupList).replace(",", ";");
            thedep.setAuditGroup(newStr);
        }
        return thedep;
    }//-------------------------------------------------------------------------------------

    //共用功能 部門查詢
    private String generateDepQuerySqlStr() {
        return "SELECT d.DepId, d.DepName, " +
                "COALESCE(SITEID, 'NA') as SiteID, Status, " +
                "REPLACE(COALESCE(DepIDinUnisys, 'NA'), ',', '|') as DepIDinUnisys, " +
                "REPLACE(COALESCE(PlaceGroup, 'NA'), ',', '|') as PlaceGroup, " +
                "RecKeepYear, COALESCE(GroupTeam, 'NA') as GroupTeam, " +
                "COALESCE(AuditGroup, 'NA') as AuditGroup " +
                "FROM dep d " +
                "WHERE d.DepId IN :groupTeam " +
                "AND d.Status = 'ON'";
    }


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
        this.GroupTeam = groupTeam;
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
        return "UBGIR_DepService Service Bean data: [depRepo=" + depRepo + ", logService=" + logService + ", maxsize=" + maxsize
                + ", DepID=" + DepID + ", DepName=" + DepName + ", SiteID=" + SiteID + ", Status=" + Status
                + ", DepIDinUnisys=" + DepIDinUnisys + ", PlaceGroup=" + PlaceGroup + ", RecKeepYear=" + RecKeepYear
                + ", GroupTeam=" + GroupTeam + ", AuditGroup=" + AuditGroup + ", errmsg=" + errmsg + "]";
    }

}
