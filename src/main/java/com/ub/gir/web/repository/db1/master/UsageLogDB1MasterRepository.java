package com.ub.gir.web.repository.db1.master;


import java.util.Date;

import com.ub.gir.web.entity.db1.master.UsageLogDB1Master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UsageLogDB1MasterRepository extends JpaRepository<UsageLogDB1Master, Long> {

    //取得目前使用中的Token總數
    @Query(nativeQuery = true, value = "SELECT COUNT(ActionUser) from usagelog WHERE ActionType ='KEY'")
    long findTokenTotal();

    //當Server重啟時清除所有Token
    @Modifying
    @Query(nativeQuery = true, value = "delete from usagelog where ActionType ='KEY'")
    void resetToken();

    @Modifying
    @Query("delete from UsageLogDB1Master where ActionTime < :actionTime")
    int deleteByActionTimeAfter(Date actionTime);
}