package com.ub.gir.web.repository.db1.master;


import java.util.List;

import com.ub.gir.web.entity.db1.master.PasswordHistoryDB1Master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface PasswordHistoryDB1MasterRepository extends JpaRepository<PasswordHistoryDB1Master, Long> {

    // 撈user 前 n代 pwd
    @Query(nativeQuery = true, value = "SELECT OLDPassword FROM passwordhistory WHERE UserName = :username ORDER BY ChangeDate Desc limit :gen")
    List<String> getPwdHistory(@Param("username") String username, @Param("gen") int gen);

}
