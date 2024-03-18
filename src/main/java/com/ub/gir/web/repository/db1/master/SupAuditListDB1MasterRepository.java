package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.SupAuditListDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


@Repository

public interface SupAuditListDB1MasterRepository extends JpaRepository<SupAuditListDB1Master, Long> {
    /**
     * 透過 username 取得sup_auditAgentID list by DepID and username, 空白欄位用NA替代
     */
    @Query(nativeQuery = true, value = "SELECT ID, UserName, IF(char_length(ableAgentID)>0, ableAgentID, 'NA') as ableAgentID , \n"
            + "IF(char_length(ableExt)>0, ableExt, 'NA') as ableExt,  \n"
            + "DepID, Location, AuditStartDate, AuditEndDate, CanDL, Setrec, IF(char_length(Memo)>0, Memo, 'NA') as Memo \n"
            + "From sup_auditlist WHERE UserName=?1 and ableAgentID !='NA' order by ableAgentID")
    List<String> getAgentAuditListByUsername(String username);

    //撈sup_auditAgentID list by DepID and username, 空白欄位用NA替代
    @Query(nativeQuery = true, value = "SELECT ID, UserName, IF(char_length(ableAgentID)>0, ableAgentID, 'NA') as ableAgentID , \n"
            + "IF(char_length(ableExt)>0, ableExt, 'NA') as ableExt,  \n"
            + "DepID, Location, AuditStartDate, AuditEndDate, CanDL, Setrec, IF(char_length(Memo)>0, Memo, 'NA') as Memo \n"
            + "From sup_auditlist WHERE DepID=?1 and UserName=?2 and ableAgentID !='NA' order by ableAgentID")
    List<String> getAgentAuditList(String depid, String username);
    //撈取單筆agentid 權限設定狀態
    @Query(nativeQuery = true, value = "SELECT ID, UserName, IF(char_length(ableAgentID)>0, ableAgentID, 'NA') as ableAgentID , \n"
            + "IF(char_length(ableExt)>0, ableExt, 'NA') as ableExt,  \n"
            + "DepID, Location, AuditStartDate, AuditEndDate, CanDL, Setrec, IF(char_length(Memo)>0, Memo, 'NA') as Memo \n"
            + "From sup_auditlist WHERE ableAgentID=?1 and UserName=?2 and ableAgentID !='NA' order by ableAgentID")
    List<String> getAgentstatus(String agentID, String username);
    @Query(nativeQuery = true, value = "SELECT ID, UserName, IF(char_length(ableAgentID)>0, ableAgentID, 'NA') as ableAgentID , \n"
            + "IF(char_length(ableExt)>0, ableExt, 'NA') as ableExt,  \n"
            + "DepID, Location, AuditStartDate, AuditEndDate, CanDL, Setrec, IF(char_length(Memo)>0, Memo, 'NA') as Memo \n"
            + "From sup_auditlist WHERE ableExt=?1 and UserName=?2 and ableExt !='NA' order by ableExt")
    List<String> getExtstatus(String ext, String username);
    /**
     * 透過 username 取得撈sup_auditExt list by DepID and username, 空白欄位用NA替代
     */
    @Query(nativeQuery = true, value = "SELECT ID, UserName, IF(char_length(ableAgentID)>0, ableAgentID, 'NA') as ableAgentID , \n"
            + "IF(char_length(ableExt)>0, ableExt, 'NA') as ableExt,  \n"
            + "DepID, Location, AuditStartDate, AuditEndDate, CanDL, Setrec, IF(char_length(Memo)>0, Memo, 'NA') as Memo \n"
            + "From sup_auditlist WHERE UserName=?1 and ableExt !='NA' order by ableExt")
    List<String> getExtAuditListByUsername(String username);

    //撈sup_auditExt list by DepID and username, 空白欄位用NA替代
    @Query(nativeQuery = true, value = "SELECT ID, UserName, IF(char_length(ableAgentID)>0, ableAgentID, 'NA') as ableAgentID , \n"
            + "IF(char_length(ableExt)>0, ableExt, 'NA') as ableExt,  \n"
            + "DepID, Location, AuditStartDate, AuditEndDate, CanDL, Setrec, IF(char_length(Memo)>0, Memo, 'NA') as Memo \n"
            + "From sup_auditlist WHERE UserName=?1 and ableExt !='NA' order by ableExt")
    List<String> getExtAuditList( String username);

    //撈sup_audit AgentID List by supervisor's username and DepID
    @Query(nativeQuery = true, value = "SELECT ableAgentID "
            + "FROM sup_auditlist WHERE UserName = ?1 AND ableAgentID IS NOT NULL order by ableAgentID")
    List<String> getAbleAgentIDList(String username);


    //撈sup_audit AgentID List by supervisor's username and DepID
    @Query(nativeQuery = true, value = "SELECT ableExt "
            + "FROM sup_auditlist WHERE UserName = ?1 and ableExt IS NOT NULL order by ableExt")
    List<String> getAbleExtList(String username);

    /**
     * 是否存在 Username + AgentID + DepID
     */
    @Query(nativeQuery = true, value = "SELECT count(*) FROM sup_auditlist WHERE UserName=?1 and ableAgentID=?2 and DepID=?3 ")
    int chkAgentIDExist(String username, String agentId, String depId);

    /**
     * 是否存在 Username + ExtNum + DepID
     */
    @Query(nativeQuery = true, value = "SELECT count(*) FROM sup_auditlist WHERE UserName=?1 and ableExt=?2 and DepID=?3 ")
    int chkExtNumExist(String username, String extNum, String depId);

    /**
     * 刪除 可調聽ableAgentID By username + ableAgentID
     */
    @Modifying
    @Query("DELETE FROM SupAuditListDB1Master WHERE UserName = ?1 and ableAgentID = ?2")
    void deleteByAbleAgentID(String username, String ableAgentID);

    /**
     * 刪除 可調聽ableExt By username + ableExt
     */
    @Modifying
    @Query("DELETE FROM SupAuditListDB1Master WHERE UserName=?1 and ableExt=?2")
    void deleteByExt(String username, String ableExt);

    @Modifying
    @Query("DELETE FROM SupAuditListDB1Master WHERE UserName=?1")
    void deleteByUserName(String username);

    // 修改DL欄位 Status
    @Modifying
    @Query("UPDATE SupAuditListDB1Master SET CanDL=?3 WHERE UserName=?1 AND ableAgentID=?2")
    void setDLIDStatus(String username, String AgentID, String CanDL);

    // 修改rec欄位 Status
    @Modifying
    @Query("UPDATE SupAuditListDB1Master SET setrec=?3 WHERE UserName=?1 AND ableAgentID=?2")
    void setrecStatus(String username, String AgentID, String setrec);

    // 修改DL by Ext
    @Modifying
    @Query("UPDATE SupAuditListDB1Master SET CanDL=?3 WHERE UserName=?1 AND ableExt=?2")
    void setDLExtStatus(String username, String Ext, String CanDL);

    // 修改rec by Ext
    @Modifying
    @Query("UPDATE SupAuditListDB1Master SET setrec=?3 WHERE UserName=?1 AND ableExt=?2")
    void setrecExtStatus(String username, String Ext, String setrec);

    // 查詢CanDL的Status by Ext
    @Query("SELECT CanDL FROM SupAuditListDB1Master WHERE UserName=?1 AND ableExt=?2")
    String chDLStatusByExt(String username, String Ext);

    @Query(nativeQuery = true, value = "SELECT * FROM sup_auditlist WHERE UserName= :username AND ableExt = :ableExt")
    Optional<SupAuditListDB1Master> findByUserNameAndAbleExt(String username, String ableExt);

    @Query(nativeQuery = true, value = "SELECT * FROM sup_auditlist WHERE UserName= :username AND ableAgentID = :ableAgentId")
    Optional<SupAuditListDB1Master> findByUserNameAndAbleAgentId(String username, String ableAgentId);

    // 查詢Setrec的Status by Ext
    @Query("SELECT Setrec FROM SupAuditListDB1Master WHERE UserName=?1 AND ableExt=?2")
    String chrecStatusByExt(String username, String Ext);

    // 查詢CanDL的Status by AgentID
    @Query("SELECT CanDL FROM SupAuditListDB1Master WHERE UserName=?1 AND ableAgentID=?2")
    String chDLStatusByAgemtID(String username, String AgentID);

    // 查詢Setrec的Status by AgentID
    @Query("SELECT Setrec FROM SupAuditListDB1Master WHERE UserName=?1 AND ableAgentID=?2")
    String chrecStatusByAgemtID(String username, String AgentID);

    //確認是否有加入調聽權限 by agentid
    @Query("SELECT count(ID) FROM SupAuditListDB1Master WHERE UserName=?1 AND ableAgentID=?2")
    int chkSupAgentExist(String username, String agentID);

    //確認是否有加入調聽權限 by Ext
    @Query("SELECT count(ID) FROM SupAuditListDB1Master WHERE UserName=?1 AND ableExt=?2")
    int chkSupExtExist(String username, String ext);

    // 查詢agentId調聽權限的開始時間 by AgentID
    @Query("SELECT AuditStartDate FROM SupAuditListDB1Master WHERE UserName=?1 AND ableAgentID=?2")
    String chStartdateByAgemtID(String username, String AgentID);

    // 查詢agentId調聽權限的結束時間 by AgentID
    @Query("SELECT AuditEndDate FROM SupAuditListDB1Master WHERE UserName=?1 AND ableAgentID=?2")
    String chEnddateByAgemtID(String username, String AgentID);

    // 查詢Ext調聽權限的開始時間 by Ext
    @Query("SELECT AuditStartDate FROM SupAuditListDB1Master WHERE UserName=?1 AND ableExt=?2")
    String chStartdateByExt(String username, String Ext);

    // 查詢Ext調聽權限的結束時間 by Ext
    @Query("SELECT AuditEndDate FROM SupAuditListDB1Master WHERE UserName=?1 AND ableExt=?2")
    String chEnddateByExt(String username, String Ext);

    @Modifying
    @Query("update SupAuditListDB1Master set AuditStartDate=?1, AuditEndDate=?2, CanDL=?3, Setrec=?4 WHERE UserName = ?5 and ableAgentID=?6 ")
    void updateStatusByAgentID(Timestamp starttime , Timestamp endtime , String candl , String setrec,String username,String agentID);

    @Modifying
    @Query("update SupAuditListDB1Master set AuditStartDate=?1, AuditEndDate=?2, CanDL=?3, Setrec=?4 WHERE UserName = ?5 and ableExt=?6 ")
    void updateStatusByExt(Timestamp starttime , Timestamp endtime , String candl , String setrec,String username,String ext);

}