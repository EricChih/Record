package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.CfgPersonDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


@Repository
public interface CfgPersonDB1MasterRepository extends JpaRepository<CfgPersonDB1Master, Long> {

    //依造username 登入帳號撈取 user role
    @Query(nativeQuery = true, value = "SELECT Role FROM cfg_person WHERE Name = ?1")
    List<String> getUserRoleByUsername(String username);

    //依造username 登入帳號撈取 user role
    @Query(nativeQuery = true, value = "SELECT Role FROM cfg_person WHERE Name = ?1")
    String getUserRole(String username);

    //依造username 登入帳號撈取 user siteid_locatoin
    @Query(nativeQuery = true, value = "SELECT Location FROM cfg_person WHERE Name = ?1")
    Optional<String> getUserSiteIDByUsername(String username);

    // 撈user的depid by given username and role
    @Query(nativeQuery = true, value = "SELECT DepID FROM cfg_person WHERE Name = ?1 and Role= ?2")
    Optional<String> getDepIDByUsernameRole(String username, String role);

    // 撈user的depid by given username and role
    @Query(nativeQuery = true, value = "SELECT DepID FROM cfg_person WHERE Name = ?1 and Role= ?2")
    List<String> getDepIDByUsernameNRole(String username, String role);

    // 撈user的depid by given username and role
    @Query(nativeQuery = true, value = "SELECT DepID FROM cfg_person WHERE Name = ?1")
    Optional<String> getDepIDByUsername(String username);

    // 撈user的depid by given userID
    @Query(nativeQuery = true, value = "SELECT DepID FROM cfg_person WHERE ID = ?1")
    List<String> getDepIDByID(String uid);

    // 撈user的agentid by given username
    @Query(nativeQuery = true, value = "SELECT IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID \r\n"
            + "FROM cfg_person WHERE Name = ?1")
    List<String> getAgentIDByUsername(String username);

    // 撈user的ext分機號碼 by given username
    @Query(nativeQuery = true, value = "SELECT IF(char_length(Ext)>0, Ext, 'NA') as Ext \r\n"
            + "FROM cfg_person WHERE Name = ?1")
    List<String> getExtByUsername(String username);

    // 撈user的login name by given userID
    @Query(nativeQuery = true, value = "SELECT Name FROM cfg_person WHERE ID = ?1")
    List<String> getLoginNameByID(String uid);

    // 撈user的ID by given login name
    @Query(nativeQuery = true, value = "SELECT ID FROM cfg_person WHERE Name = ?1")
    String findIDByName(String name);

    //撈user list by maxsize; maxsize 設定在application.properties 中
    @Query(nativeQuery = true, value = "SELECT ID, Name, FirstName, LastName, Role, DepID, IF(char_length(AgentID)>0, AgentID, '0') as AgentID, IF(char_length(Ext)>0, Ext, '0') as Ext, Location, Status, \r\n"
            + "IF(char_length(Password)>0, Password, 'NA') as Password, IF(char_length(Email)>0, Email, 'NA') as Email, FinalChangeDate, LastLoginDate, IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew \r\n"
            + "From cfg_person  Order by FinalChangeDate DESC  Limit ?1")
    List<String> findAllWithRole(int maxsize);

    // 撈user data by given username
    @Query(nativeQuery = true, value = "SELECT ID, Name, FirstName, LastName, Role, DepID, IF(char_length(AgentID)>0, AgentID, '0') as AgentID, IF(char_length(Ext)>0, Ext, '0') as Ext, Location, Status, \r\n"
            + "IF(char_length(Password)>0, Password, 'NA') as Password, IF(char_length(Email)>0, Email, 'NA') as Email, FinalChangeDate, LastLoginDate, IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew \r\n"
            + "FROM cfg_person WHERE Name = ?1")
    List<String> getUserByUsername(String username);

    // 撈user data by given uid
    @Query(nativeQuery = true, value = "SELECT ID, Name, FirstName, LastName, Role, DepID, IF(char_length(AgentID)>0, AgentID, '0') as AgentID, IF(char_length(Ext)>0, Ext, '0') as Ext, Location, Status, \r\n"
            + "IF(char_length(Password)>0, Password, 'NA') as Password, IF(char_length(Email)>0, Email, 'NA') as Email, FinalChangeDate, LastLoginDate, IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew \r\n"
            + "FROM cfg_person WHERE ID = ?1")
    List<String> getUserByID(String uid);

    // 撈user data by given DepID
    @Query(nativeQuery = true, value = "SELECT ID, Name, FirstName, LastName, Role, DepID, IF(char_length(AgentID)>0, AgentID, '0') as AgentID, IF(char_length(Ext)>0, Ext, '0') as Ext, Location, Status, \r\n"
            + "IF(char_length(Password)>0, Password, 'NA') as Password, IF(char_length(Email)>0, Email, 'NA') as Email, FinalChangeDate, LastLoginDate, IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew \r\n"
            + "FROM cfg_person WHERE DepID = ?1")
    List<String> getUserByDepID(String depid);

    // 撈supervisor list by given DepID
    @Query(nativeQuery = true, value = "SELECT ID, Name, FirstName, LastName, Role, DepID, IF(char_length(AgentID)>0, AgentID, '0') as AgentID, IF(char_length(Ext)>0, Ext, '0') as Ext, Location, Status, \r\n"
            + "IF(char_length(Password)>0, Password, 'NA') as Password, IF(char_length(Email)>0, Email, 'NA') as Email, FinalChangeDate, LastLoginDate, IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew \r\n"
            + "FROM cfg_person WHERE Role='supervisor' and DepID = ?1")
    List<String> getSupByDepID(String depid);

    // 撈user data by given DepID
    @Query(nativeQuery = true, value = "Select IF(char_length(PasswordNew)>0, PasswordNew, 'NA') as PasswordNew \r\n"
            + "FROM cfg_person WHERE Name = ?1")
    String getPasswordNewByUsername(String username);

    //刪除User by give username
    @Transactional
    @Modifying
    @Query("delete from CfgPersonDB1Master WHERE Name= ?1")
    void deleteByUsername(String delusername);

    //停用User by give username
    @Modifying
    @Query("update CfgPersonDB1Master set Status='0' WHERE Name= ?1")
    void stopByUsername(String stopusername);

    //User密碼重設,密碼進入加密且 status=3 準備下次登入馬上密碼變更
    @Modifying
    @Query("update CfgPersonDB1Master set Status='1', PasswordNew=?2 WHERE Name= ?1")
    void resetUserPwdByName(String resetusername, String encodedusername);

    //更新user by username ,非全面cfg_person欄位更新
    @Transactional
    @Modifying
    @Query("update CfgPersonDB1Master set FirstName=?2 , LastName=?3 , Role=?4, DepID=?5, \r\n"
            + "AgentID=?6, Ext=?7, Email=?8, Status=?9, FinalChangeDate=?10 WHERE Name = ?1 ")
    void updateUserByName(String name, String firstname, String lastname,
                          String role, String depid, String agentid, String ext,
                          String email, String status, Timestamp updatetime);

    @Query("SELECT Status FROM CfgPersonDB1Master WHERE Name = ?1 ")
    int LookUserStatusByusername(String username);

    //更新user的 lastLoginDate by uid
    @Modifying
    @Query("update CfgPersonDB1Master set LastLoginDate=?2  WHERE ID = ?1 ")
    void updateUserLoginDateByID(int uid, Timestamp logintime);

    //更新user的 PasswordNew 欄位與 Status=3代表密碼已轉移新欄位 by UserID
    @Modifying
    @Query("update CfgPersonDB1Master set Status='1', PasswordNew=?2  WHERE ID = ?1 ")
    void updatePasswordNewByID(int uid, String pwdnew);

    //更新user的 PasswordNew 欄位與 Status=3代表密碼已轉移新欄位 by Username
    @Modifying
    @Query("update CfgPersonDB1Master set Status='1', FinalChangeDate =NOW(), PasswordNew=?1  WHERE Name = ?2 ")
    void updatePasswordNewByUsername(String pwdnew, String username);

    //更新user的 Status by input statusNum and uname
    @Modifying
    @Query("update CfgPersonDB1Master set Status=?1  WHERE Name = ?2 ")
    void updateStatusByUsername(String statusNum, String username);

    @Query("SELECT cfgPerson FROM CfgPersonDB1Master cfgPerson WHERE Name = :username")
    Optional<CfgPersonDB1Master> findByName(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(c)> 0 THEN true ELSE false END FROM CfgPersonDB1Master c WHERE Name = :username")
    boolean existsUserUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(c)> 0 THEN true ELSE false END FROM CfgPersonDB1Master c WHERE DepID = :depId")
    boolean existsByDepId(String depId);

}
