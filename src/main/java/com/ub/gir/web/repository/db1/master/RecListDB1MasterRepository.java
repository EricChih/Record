package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.RecListDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface RecListDB1MasterRepository extends JpaRepository<RecListDB1Master, Long> {

    //撈 reclist by maxsize; maxsize 設定在application.properties 中
    @Query(nativeQuery = true, value = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, 'NA') as Dnis, IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID, \r\n"
            + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, 'NA') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, 'NA') as CallTypeName, \r\n"
            + "IF(char_length(CustomerID)>0, CustomerID, 'NA') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
            + "IF(char_length(Location)>0, Location, 'NA') as Location, IF(char_length(CallDir)>0, CallDir, 'NA') as CallDir, \r\n"
            + "UUID, IF(char_length(WorkID)>0, WorkID, 'NA') as WorkID, AgentDN, IF(char_length(AD)>0, AD, 'NA') as AD \r\n"
            + "FROM reclist Order by StartDate DESC Limit ?1")
    List<String> findAllRec(int maxsize);

    @Query(nativeQuery = true, value = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, 'NA') as Dnis, IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID, \r\n"
            + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, 'NA') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, 'NA') as CallTypeName, \r\n"
            + "IF(char_length(CustomerID)>0, CustomerID, 'NA') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
            + "IF(char_length(Location)>0, Location, 'NA') as Location, IF(char_length(CallDir)>0, CallDir, 'NA') as CallDir, \r\n"
            + "UUID, IF(char_length(WorkID)>0, WorkID, 'NA') as WorkID, AgentDN, IF(char_length(AD)>0, AD, 'NA') as AD \r\n"
            + "FROM reclist WHERE ConnID =?1")
    List<String> findRecByConnid(String connid);

    //撈 reclist by filename
    @Query(nativeQuery = true, value = "Select ID, ConnID, Ani, IF(char_length(Dnis)>0, Dnis, 'NA') as Dnis, IF(char_length(AgentID)>0, AgentID, 'NA') as AgentID, \r\n"
            + "StartDate, EndDate, FileName, IF(char_length(CallType)>0, CallType, 'NA') as CallType, IF(char_length(CallTypeName)>0, CallTypeName, 'NA') as CallTypeName, \r\n"
            + "IF(char_length(CustomerID)>0, CustomerID, 'NA') as CustomerID, IF(char_length(Length)>0, Length, 0) as Length, \r\n"
            + "IF(char_length(Location)>0, Location, 'NA') as Location, IF(char_length(CallDir)>0, CallDir, 'NA') as CallDir, \r\n"
            + "UUID, IF(char_length(WorkID)>0, WorkID, 'NA') as WorkID, AgentDN, IF(char_length(AD)>0, AD, 'NA') as AD \r\n"
            + "FROM reclist WHERE FileName=?1 Order by StartDate DESC")
    List<String> getRecByFilename(String filename);

    //設定永久音檔
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "INSERT INTO keepreclist (ConnID, Ani, Dnis, AgentID, StartDate, \r\n"
            + " FileName, CallType, CallTypeName, CustomerID, Length, Location, CallDir, UUID, WorkID, AgentDN, AD) \r\n"
            + " SELECT  ConnID, Ani, Dnis, AgentID, StartDate, FileName, CallType, CallTypeName, CustomerID, \r\n"
            + " Length, Location, CallDir, UUID, WorkID, AgentDN, AD\r\n"
            + " FROM reclist WHERE ConnID=?1")
    void setKeepRec(String connid);

    @Query(nativeQuery = true,value="SELECT * FROM reclist WHERE ConnID=?1")
    Optional<RecListDB1Master> findAllByconnId(String connId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM reclist WHERE ID = ?1")
    void deleteRecById(int id);

    @Query(nativeQuery = true,value="SELECT * FROM reclist WHERE StartDate < ?1")
    List<RecListDB1Master> findRecListByStartDate(Date startDate);
}
