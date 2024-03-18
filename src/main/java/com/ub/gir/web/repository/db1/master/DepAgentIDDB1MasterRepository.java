package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.DepAgentIDDB1Master;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DepAgentIDDB1MasterRepository extends JpaRepository<DepAgentIDDB1Master, Long> {

    //同步功能，透過dep_id抓取該部門所有agentid
    @Query(nativeQuery = true, value = "SELECT AgentID FROM dep_agentid WHERE DepID= :depid")
    List<String> getAgentIDByDepID(@Param("depid") String depid);

    @Query(value = "SELECT * FROM dep_agentid WHERE AgentID= :agentId", nativeQuery = true)
    Optional<DepAgentIDDB1Master> findAllByAgentID(String agentId);

    @Query(value = "SELECT COUNT(*) FROM dep_agentid WHERE DepID = :depID AND AgentID = :agentID", nativeQuery = true)
    int existsByDepIDAndAgentID(String depID, String agentID);

    @Query(value = "SELECT CASE WHEN COUNT(da) > 0 THEN true ELSE false END FROM DepAgentIDDB1Master da WHERE DepID = :depId")
    boolean existsByDepId(String depId);
}