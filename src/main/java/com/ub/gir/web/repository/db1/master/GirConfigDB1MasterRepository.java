package com.ub.gir.web.repository.db1.master;


import java.util.List;
import java.util.Optional;

import com.ub.gir.web.entity.db1.master.GirConfigDB1Master;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface GirConfigDB1MasterRepository extends JpaRepository<GirConfigDB1Master, Long> {


    // 撈所有的site list
    @Query(nativeQuery = true, value = "select IDCode, IDName from girconfig where IDType = 'site'")
    List<String> getAllSite();

    // 撈所有的role list
    @Query(nativeQuery = true, value = "select IDCode, IDName from girconfig where IDType = 'role'")
    List<String> getAllRole();

    // 撈所有的module list
    @Query(nativeQuery = true, value = "select IDCode, IDName from girconfig where IDType = 'module'")
    List<String> getAllModule();
    @Query(nativeQuery = true, value = "select IDName from girconfig where IDCode =?1")
    String getFunctionNameCN(String FunctionName);
    // 撈所有的act list
    @Query(nativeQuery = true, value = "select IDCode, IDName from girconfig where IDType = 'act'")
    List<String> getAllAct();

    // 撈目前操作軌跡保留天數
    @Query(nativeQuery = true, value = "select IDCode from girconfig where IDType =?1 ")
    List<String> getUserSites(String sitecode);

    List<GirConfigDB1Master> findAllByIDType(String IDType);

    Optional<GirConfigDB1Master> findByIDCode(String IDCode);

    @Modifying
    @Query(value = "update girconfig set Memo=? WHERE IDCode = ?", nativeQuery = true)
    void updateMemoByIdCode(String memo, String idCode);
}
