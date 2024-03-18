package com.ub.gir.web.service;

import com.ub.gir.web.dto.connectToGADb.DepCommonFieldDto;
import com.ub.gir.web.entity.db1.master.DepAgentIDDB1Master;
import com.ub.gir.web.entity.db1.master.DepExtDB1Master;
import com.ub.gir.web.entity.db1.master.DepMappingCodeDB1Master;
import com.ub.gir.web.repository.db1.master.DepAgentIDDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepExtDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.DepMappingCodeDB1MasterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static org.springframework.beans.BeanUtils.copyProperties;

@Slf4j
@Service
@Transactional
public class ExternalDbService {
    @Resource
    private DepMappingCodeDB1MasterRepository depMappingCodeRepository;
    @Resource
    private DepAgentIDDB1MasterRepository depAgentIDRepository;
    @Resource
    private DepExtDB1MasterRepository depExtRepository;

    private HashMap<String, String> depCodeMap;
    @Value("${external.datasource.url}")
    private String connectionUrl;

    @PostConstruct
    public void initializeDepCodeMap() {
        depCodeMap = new HashMap<>();
        List<DepMappingCodeDB1Master> resultList = depMappingCodeRepository.findAll();
        resultList.forEach(v -> {
            depCodeMap.put(v.getGaSysDepID(), v.getModifyDepID());
        });
    }

    public void loadFromExternalDb() {
        this.loadAgentIdFromExternalDb();
        this.loadExtFromExternalDb();
    }

    /**
     * 取得 外部資料庫AgentId
     */
    public void loadAgentIdFromExternalDb() {
        log.info("LoadAgentIdFromExternalDb is start");
        List<DepCommonFieldDto> resultList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM TP_gcti_urs.dbo.AgentID_RecordingInfo")) {
            if (!ObjectUtils.isEmpty(resultSet))
                depAgentIDRepository.deleteAll();

            while (resultSet.next()) {
                DepCommonFieldDto dto = new DepCommonFieldDto();
                dto.setAgentID(resultSet.getString("login_code"));
                dto.setDepID(resultSet.getString("name"));
                resultList.add(dto);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        List<DepAgentIDDB1Master> entityList = copyListProperties(resultList, DepAgentIDDB1Master::new);
        entityList.forEach(v -> {
            if (depCodeMap.containsKey(v.getDepID()))
                v.setDepID(depCodeMap.get(v.getDepID()));
        });
        depAgentIDRepository.saveAll(entityList);
        log.info("Total updated records: " + entityList.size());
        log.info("LoadAgentIdFromExternalDb is end");
    }

    /**
     * 取得 外部資料庫Ext
     */
    public void loadExtFromExternalDb() {
        log.info("LoadExtFromExternalDb is start");
        List<DepCommonFieldDto> resultList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM TP_gcti_urs.dbo.TP_DeptCheck_DN")) {
            if (!ObjectUtils.isEmpty(resultSet))
                depExtRepository.deleteAll();

            while (resultSet.next()) {
                DepCommonFieldDto dto = new DepCommonFieldDto();
                dto.setDepID(resultSet.getString("name"));
                dto.setExt(resultSet.getString("number_"));
                resultList.add(dto);
            }
        } catch (SQLException e) {
            log.error("[loadExtFromExternalDb] : {}", e.getMessage(), e);
        }

        List<DepExtDB1Master> entityList = copyListProperties(resultList, DepExtDB1Master::new);
        entityList.forEach(v -> {
            if (depCodeMap.containsKey(v.getDepID()))
                v.setDepID(depCodeMap.get(v.getDepID()));
        });
        depExtRepository.saveAll(entityList);
        log.info("Total updated records: " + entityList.size());
        log.info("LoadExtFromExternalDb is end");
    }

    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            copyProperties(source, t);
            list.add(t);
        }
        return list;
    }
}