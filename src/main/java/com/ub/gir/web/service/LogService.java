package com.ub.gir.web.service;


import com.ub.gir.web.dto.LogDTO;
import com.ub.gir.web.dto.log.LogDto;
import com.ub.gir.web.dto.log.SearchLogReqDto;
import com.ub.gir.web.dto.log.SearchLogResDto;
import com.ub.gir.web.entity.db1.master.UsageLogDB1Master;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;
import com.ub.gir.web.repository.db1.master.UsageLogDB1MasterRepository;
import com.ub.gir.web.util.DateTimeUtil;
import com.ub.gir.web.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.ub.gir.web.util.DateTimeUtil.getCurrentDate;
import static com.ub.gir.web.util.RocDateUtil.convertStringDateToDate;


@Slf4j
@Service
public class LogService {
    @Autowired
    private UsageLogDB1MasterRepository logRepo;
    @Autowired
    private ConfigService configservice;
    @Autowired
    private DepService depService;
    @Autowired
    private CfgPersonDB1MasterRepository cfgPersonRepo;
    @Resource
    private UserService userService;
    @PersistenceContext(unitName = "db1MasterPersistenceUnit")
    private EntityManager entityManager;

    @Value("${ub.view.page.max-size}")
    private int maxsize;

    // 統計目前DB裡的席次使用總筆數
    public Long count() {
        try{
            return logRepo.findTokenTotal();
        } catch (Exception e) {
            log.error("Error to count");
            throw e;
        }
    }

    /**
     * 刪除操作軌跡 By expiryDate
     *
     * @param expiryDate 到期日期
     * @param contentLog 內容紀錄
     */
    @Transactional
    public void deleteByExpiryDate(Date expiryDate,StringBuilder contentLog) {
        int deletedQuantity = logRepo.deleteByActionTimeAfter(expiryDate);
        contentLog.append("Total deleted records: ").append(deletedQuantity);
    }

    @Transactional(rollbackFor = Exception.class)
    //清除DB所有的席次資料
    public void resetToken() {
        try{
            logRepo.resetToken();
        } catch (Exception e) {
            log.error("Error to resetToken");
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    //寫log 進DB
    public void addLog(LogDTO logDTO) {

        UsageLogDB1Master usageLogDB1Master = new UsageLogDB1Master();

        Date in = new Date();
        LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
        Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        usageLogDB1Master.setActionUser(logDTO.getActionUser());
        usageLogDB1Master.setFunctionName(logDTO.getFunctionName());
        usageLogDB1Master.setActionType(logDTO.getActionType());
        usageLogDB1Master.setInfo(logDTO.getInfo());
        usageLogDB1Master.setActionDepID(logDTO.getActionDepId());
        usageLogDB1Master.setActionRole(logDTO.getActionRole());
        usageLogDB1Master.setActionTime(out);

        logRepo.save(usageLogDB1Master);

    }

    @Transactional(rollbackFor = Exception.class)
    public void addLog(LogDto dto,String username) {
        String role = cfgPersonRepo.getUserRole(username);

        UsageLogDB1Master entity = JsonUtil.convertObjectToClass(dto, UsageLogDB1Master.class);
        entity.setActionTime(getCurrentDate());
        entity.setActionUser(username);
        entity.setActionRole(role);
        entity.setActionDepID(userService.getDepIdByUsernameRole(username, role));
        logRepo.save(entity);
    }

    //搜尋all log 資料
    public List<SearchLogResDto> searchLog(SearchLogReqDto logDto, String username) {
        logDto.setFunctionNameCN(findFunctionNameCNFromKey(logDto.getFunctionName()));
        logDto.setActionTypeCN(findActionTypeCNFromKey(logDto.getActionType()));
        String role = cfgPersonRepo.getUserRole(username);
        logDto.setActionRole(role);
        logDto.setActionDepID(userService.getDepIdByUsernameRole(username, role));

        List<UsageLogDB1Master> searchResult = queryUsageLogs(logDto);
        return convertEntityToServiceDto(searchResult);
    }//--------------------------------------------------------------------------------------------

    // 如果是manager，搜尋自己部門所有的manager軌跡及代管部門manager的軌跡
    private List<UsageLogDB1Master> queryUsageLogs(SearchLogReqDto logDto) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT u FROM UsageLogDB1Master u ")
                    .append("WHERE u.ActionTime >= :startTime AND u.ActionTime <= :endTime ");
            Map<String, Object> parameters = new HashMap<>();
            Date startTime = DateTimeUtil.dateStrToDate(logDto.getStarttime());
            Date endTime = DateTimeUtil.dateStrToDate(logDto.getEndtime());
            parameters.put("startTime", startTime);
            parameters.put("endTime", endTime);

            String actionRole = logDto.getActionRole();
            // supervisor 跟manager 只能查到自己角色的log
            if (actionRole.equals("supervisor") || actionRole.equals("manager")) {
                jpql.append("AND u.ActionRole = :actionRole ");
                parameters.put("actionRole", logDto.getActionRole());
                if (actionRole.equals("manager")) {
                    String depGroupTeam = depService.getDepGroupTeam(logDto.getActionDepID());
                    jpql.append("AND u.ActionDepID in :depGroupTeam ");
                    parameters.put("depGroupTeam", Arrays.asList(depGroupTeam.split(";")));
                } else {
                    // supervisor 只能查到自己部門的log
                    jpql.append("AND u.ActionDepID = :actionDepID ");
                    parameters.put("actionDepID", logDto.getActionDepID());
                }
            }

            if (StringUtils.isNotBlank(logDto.getFunctionName())) {
                jpql.append("AND u.FunctionName = :functionName ");
                parameters.put("functionName", logDto.getFunctionName());
            }

            if (StringUtils.isNotBlank(logDto.getActionType())) {
                jpql.append("AND u.ActionType = :actionType ");
                parameters.put("actionType", logDto.getActionType());
            }

            if (StringUtils.isNotBlank(logDto.getActionUser())) {
                jpql.append("AND u.ActionUser = :actionUser ");
                parameters.put("actionUser", logDto.getActionUser());
            }
            TypedQuery<UsageLogDB1Master> query = entityManager.createQuery(jpql.toString(), UsageLogDB1Master.class);
            parameters.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error queryUsageLogs : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<SearchLogResDto> convertEntityToServiceDto(List<UsageLogDB1Master> logDB1Masters){
        TreeMap modulemap = configservice.getAllModuleMap();
        TreeMap actmap = configservice.getAllActMap();
        return logDB1Masters.stream().map(data -> {
            SearchLogResDto logDto = new SearchLogResDto();
            logDto.setID(data.getID());
            logDto.setActionTime(DateTimeUtil.dateToString(data.getActionTime()));
            logDto.setActionUser(data.getActionUser());

            logDto.setFunctionName(data.getFunctionName());
            String functionNameLowerCase = data.getFunctionName().toLowerCase();
            logDto.setFunctionNameCN(((String) modulemap.get(functionNameLowerCase))); //非DB欄位

            logDto.setActionType(data.getActionType());
            String actionTypeLowerCase = data.getActionType().toLowerCase();
            logDto.setActionTypeCN(((String) actmap.get(actionTypeLowerCase))); //非DB欄位

            String info = data.getInfo().replaceAll(",", "，");
            logDto.setInfo(info);
            return logDto;
        }).collect(Collectors.toList());
    }

    private UsageLogDB1Master convertToDto(LogDto dto) throws ParseException {
        UsageLogDB1Master entity = JsonUtil.convertObjectToClass(dto, UsageLogDB1Master.class);
        entity.setActionTime(convertStringDateToDate(dto.getActionTime(), "yyyy-MM-dd HH:mm:ss",false));

        return entity;
    }

    private String findActionTypeCNFromKey(String actionType) {
        TreeMap actmap = configservice.getAllActMap();
        String actionTypeLowerCase = actionType.toLowerCase();
        return (String) actmap.get(actionTypeLowerCase);
    }

    private String findFunctionNameCNFromKey(String functionName) {
        TreeMap modulemap = configservice.getAllModuleMap();
        String functionNameLowerCase = functionName.toLowerCase();
        return (String) modulemap.get(functionNameLowerCase);
    }
}