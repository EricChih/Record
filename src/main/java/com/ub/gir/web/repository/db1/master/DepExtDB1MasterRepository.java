package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.DepExtDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DepExtDB1MasterRepository extends JpaRepository<DepExtDB1Master, Long> {

    @Query(nativeQuery = true, value = "SELECT Ext FROM dep_ext WHERE DepID= :depid")
    List<String> getExtByDepID(@Param("depid") String depid);

    @Query(value = "SELECT * FROM dep_ext WHERE Ext= :ext", nativeQuery = true)
    Optional<DepExtDB1Master> findAllByExt(String ext);

    @Query(value = "SELECT COUNT(*) FROM dep_ext WHERE DepID = :depID AND Ext = :ext", nativeQuery = true)
    int existsByDepIDAndExt(String depID, String ext);

    @Query(value = "SELECT CASE WHEN COUNT(de) > 0 THEN true ELSE false END FROM DepExtDB1Master de WHERE DepID = :depId")
    boolean existsByDepId(String depId);
}
