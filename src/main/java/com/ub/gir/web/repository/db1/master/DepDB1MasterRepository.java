package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.DepDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DepDB1MasterRepository extends JpaRepository<DepDB1Master, Long> {

    //撈dep table 所有部門, 空白欄位用NA替代, 資料中若有逗點用|替代
    @Query(nativeQuery = true, value = "Select DepID, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID , Status,  \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n"
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear,  \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "From dep Limit ?1")
    List<String> getAllDep(int maxsize);

    // 撈all dep list and status=ON
    @Query(nativeQuery = true, value = "SELECT DepId, DepName FROM dep WHERE Status='ON'")
    List<String> getAllDeplist();

    // 撈all dep list by mgr depid
    @Query(nativeQuery = true, value = "SELECT DepId, DepName FROM dep WHERE DepID= ?1")
    List<String> getMgrDeplist(String depid);

    // 撈all dep list and status=ALL
    @Query(nativeQuery = true, value = "SELECT DepId, DepName FROM dep")
    List<String> getAllOnOffDeplist();

    // 撈dep list by given siteid and status=ON
    @Query(nativeQuery = true, value = "SELECT DepId, DepName FROM dep WHERE Status='ON' and SiteID = ?1")
    List<String> getDeplistBySiteid(String siteid);

    // 撈dep list by given siteid and status=ON
    @Query(nativeQuery = true, value = "SELECT GroupTeam FROM dep WHERE Status='ON' and DepID = ?1")
    String getGroupTeamByDepid(String depid);

    // 撈dep data by given depId
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n"
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE DepID like :depid%")
    List<String> getDepById(String depid);

    // 撈dep data by given depName
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n"
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE DepName like :depname%")
    List<String> getDepByName(String depname);

    // 撈dep data by given Status
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n"
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE Status=:status")
    List<String> getDepByStatus(String status);

    // 撈dep by depid + depname+ status 比對
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n "
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE DepID like :depid% and DepName like :depname% and Status=:status")
    List<String> searchbyIDNameStatus(String depid, String depname, String status);

    // 撈dep by depid + depname 比對
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n"
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE DepID like :depid% and DepName like :depname%")
    List<String> searchbyIDName(String depid, String depname);

    // 撈dep by depid + status 比對
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n"
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'), \",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE DepID like :depid% and Status=:status")
    List<String> searchbyIDStatus(String depid, String status);

    // 撈dep by depname+ status 比對
    @Query(nativeQuery = true, value = "SELECT DepId, DepName, IF(char_length(SiteID)>0, SiteID, 'NA') as SiteID, Status, \n"
            + "REPLACE( IF(char_length(DepIDinUnisys)>0, DepIDinUnisys, 'NA'), \",\", \"|\" ) as DepIDinUnisys, \n "
            + "REPLACE( IF(char_length(PlaceGroup)>0, PlaceGroup, 'NA'),\",\", \"|\" ) as PlaceGroup, RecKeepYear, \n"
            + "IF(char_length(GroupTeam)>0, GroupTeam, 'NA') as GroupTeam ,IF(char_length(AuditGroup)>0, AuditGroup, 'NA') as AuditGroup\n"
            + "FROM dep WHERE DepName like :depname% and Status=:status")
    List<String> searchbyNameStatus(String depname, String status);

    //撈部門代理的Group DepID
    @Query(nativeQuery = true, value = "Select GroupTeam FROM dep WHERE DepID = ?1")
    String getGroupDepIDList(String depId);

    @Query(nativeQuery = true, value = "Select AuditGroup FROM dep WHERE DepID = ?1")
    Optional<String> geAuditGroupIDList(String depId);

    //撈DepName by given DepID
    @Query(nativeQuery = true, value = "Select DepName FROM dep WHERE DepID = ?1")
    String getDepNameByDepid(String depid);

    //確認Depid 是否已經存在
    @Query(nativeQuery = true, value = "SELECT COUNT(DepID) FROM dep WHERE DepID = ?1")
    int chkDepidExist(String depid);

    //刪除部門byDepID
    @Modifying
    @Query("delete from DepDB1Master where DepID=:depid")
    void deleteByDepId(String depid);

    //停用部門byDepID
    @Modifying
    @Query("update DepDB1Master set Status = 'OFF' where DepID=:depid")
    void stopByDepId(String depid);

    //更新部門byDepID,非全面欄位更新
    @Modifying
    @Query("update DepDB1Master set DepName=:depname, SiteID=:siteid, "
            + "GroupTeam=:groupteam, AuditGroup=:auditgroup WHERE DepID=:depid")
    void updateByDepId(String depid, String depname, String siteid, String groupteam,String auditgroup);

    @Modifying
    @Query("update DepDB1Master set RecKeepYear=:keepyear WHERE DepID=:depid")
    void updateKeepYearByDepId(String depid, int keepyear);
    @Modifying
    @Query("update DepDB1Master set GroupTeam=:groupteam WHERE DepID=:depid")
    void updategroupteamByDepId(String depid, String groupteam);

    @Modifying
    @Query("update DepDB1Master set GroupTeam=:groupteam WHERE DepID=:depid")
    void updateGroupTeamByDepId(String groupteam, String depid);

    @Modifying
    @Query("update DepDB1Master set AuditGroup=:auditGroup WHERE DepID=:depid")
    void updateAuditGroupByDepId(String auditGroup, String depid);

    @Query(nativeQuery = true, value = "SELECT DepName FROM dep WHERE AuditGroup LIKE %?1%")
    List<String> searchByAuditGroup(String depId);

    @Query(nativeQuery = true, value = "SELECT * FROM dep")
    List<DepDB1Master> findAll();

    @Query(nativeQuery = true, value = "SELECT * FROM dep WHERE DepID=:depid")
    Optional<DepDB1Master> findByDepId(String depid);

    @Query(value = "SELECT CASE WHEN COUNT(d)> 0 THEN true ELSE false END FROM DepDB1Master d WHERE DepID = :depId")
    boolean existsByDepID(String depId);
}