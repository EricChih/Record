package com.ub.gir.web.repository.db1.master;


import com.ub.gir.web.entity.db1.master.DepAgentIDDB1Master;
import com.ub.gir.web.entity.db1.master.DepMappingCodeDB1Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DepMappingCodeDB1MasterRepository extends JpaRepository<DepMappingCodeDB1Master,Long> {

}