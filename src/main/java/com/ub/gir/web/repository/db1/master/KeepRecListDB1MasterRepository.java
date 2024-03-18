package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.KeepRecListDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface KeepRecListDB1MasterRepository extends JpaRepository<KeepRecListDB1Master, Long> {

    //撈 reclist by maxsize; maxsize 設定在application.properties 中
    @Query(nativeQuery = true, value = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, 'NA') as Dnis, IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID, \r\n"
            + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, 'NA') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, 'NA') as CallTypeName, \r\n"
            + "IF(char_length(CustomerID)>0, CustomerID, 'NA') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
            + "IF(char_length(Location)>0, Location, 'NA') as Location, IF(char_length(CallDir)>0, CallDir, 'NA') as CallDir, \r\n"
            + "UUID, IF(char_length(WorkID)>0, WorkID, 'NA') as WorkID, AgentDN, IF(char_length(AD)>0, AD, 'NA') as AD \r\n"
            + "FROM keepreclist Order by StartDate DESC Limit ?1")
    List<String> findAllRec(int maxsize);

    //撈 keepreclist by filename
    @Query(nativeQuery = true, value = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, 'NA') as Dnis, IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID, \r\n"
            + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, 'NA') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, 'NA') as CallTypeName, \r\n"
            + "IF(char_length(CustomerID)>0, CustomerID, 'NA') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
            + "IF(char_length(Location)>0, Location, 'NA') as Location, IF(char_length(CallDir)>0, CallDir, 'NA') as CallDir, \r\n"
            + "UUID, IF(char_length(WorkID)>0, WorkID, 'NA') as WorkID, AgentDN, IF(char_length(AD)>0, AD, 'NA') as AD \r\n"
            + "FROM keepreclist WHERE FileName=?1 Order by StartDate DESC")
    List<String> getRecByFilename(String filename);

    //確認ConnID 是否已經存在, 代表被Keep 永久音檔
    @Query(nativeQuery = true, value = "SELECT COUNT(ConnID) FROM keepreclist WHERE ConnID = ?1")
    int chkKeepConnIDExist(String connid);

    //刪除永久音檔
    @Modifying
    @Transactional()
    @Query("Delete from KeepRecListDB1Master WHERE ConnID= ?1")
    void delKeepRec(String connid);

    @Query(nativeQuery = true,value="SELECT * FROM keepreclist WHERE ConnID=?1")
    Optional<KeepRecListDB1Master> findAllByConnId(String connId);
}
