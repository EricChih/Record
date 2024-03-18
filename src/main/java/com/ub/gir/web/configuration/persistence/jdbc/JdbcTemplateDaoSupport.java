/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.jdbc;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;


/**
 * <p>定義操作 native query JDBC 設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
public class JdbcTemplateDaoSupport extends NamedParameterJdbcDaoSupport {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcTemplateDaoSupport(DataSource dataSource) {
        setDataSource(dataSource);
        this.namedParameterJdbcTemplate = getNamedParameterJdbcTemplate();
    }

    public JdbcTemplateDaoSupport(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public NamedParameterJdbcTemplate referenceTemplate() {
        return getNamedParameterJdbcTemplate();
    }

    public void setCacheLimit(int cacheLimit) {
        namedParameterJdbcTemplate.setCacheLimit(cacheLimit);
    }

    public <T> T execute(String sql, PreparedStatementCallback<T> action) {

        try {
            return namedParameterJdbcTemplate.execute(sql, action);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; sql : %s", sql));
        }

    }

    public <T> T execute(String sql, SqlParameterSource params, PreparedStatementCallback<T> action) {

        try {
            return namedParameterJdbcTemplate.execute(sql, params, action);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public <T> T queryForObject(String sql, SqlParameterSource params, Class<T> clazz) {

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, clazz);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public <T> T queryForSingle(String sql, SqlParameterSource params, BeanPropertyRowMapper<T> rowMapper) {

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, rowMapper);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public <T> T queryForSingle(String sql, SqlParameterSource params, Class<T> model) {
        return queryForSingle(sql, params, new BeanPropertyRowMapper<>(model));
    }

    public <T> List<T> queryForMultiple(String sql, BeanPropertyRowMapper<T> rowMapper) {

        try {
            return namedParameterJdbcTemplate.query(sql, rowMapper);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; sql : %s", sql));
        }

    }

    public <T> List<T> queryForMultiple(String sql, Class<T> model) {
        return queryForMultiple(sql, new BeanPropertyRowMapper<>(model));
    }

    public <T> List<T> queryForMultiple(String sql, SqlParameterSource params, BeanPropertyRowMapper<T> rowMapper) {

        try {
            return namedParameterJdbcTemplate.query(sql, params, rowMapper);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public <T> List<T> queryForMultiple(String sql, SqlParameterSource params, Class<T> model) {
        return queryForMultiple(sql, params, new BeanPropertyRowMapper<>(model));
    }

    public Map<String, Object> queryForMap(String sql, SqlParameterSource params) {

        try {
            return namedParameterJdbcTemplate.queryForMap(sql, params);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public <T> List<T> queryForList(String sql, SqlParameterSource params, Class<T> model) {

        try {
            return namedParameterJdbcTemplate.queryForList(sql, params, model);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public List<Map<String, Object>> queryForList(String sql, SqlParameterSource params) {

        try {
            return namedParameterJdbcTemplate.queryForList(sql, params);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public <T> Stream<T> queryForStream(String sql, SqlParameterSource params, BeanPropertyRowMapper<T> rowMapper) {

        try {
            return namedParameterJdbcTemplate.queryForStream(sql, params, rowMapper);
        } catch (DataAccessException ex) {
            throw new DataRetrievalFailureException(String.format("Incorrect Result Size Data Access Exception; params : %s , sql : %s", params, sql));
        }

    }

    public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return namedParameterJdbcTemplate.update(sql, paramSource);
    }

    public <T> int update(String sql, T model) throws DataAccessException {
        return namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model));
    }

    public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return namedParameterJdbcTemplate.update(sql, paramMap);
    }

    @SuppressWarnings("unchecked")
    public int[] batchUpdate(String sql, Map<String, ?>... paramMaps) throws DataAccessException {
        return namedParameterJdbcTemplate.batchUpdate(sql, paramMaps);
    }

    public int[] batchUpdate(String sql, SqlParameterSource... paramSources) throws DataAccessException {
        return namedParameterJdbcTemplate.batchUpdate(sql, paramSources);
    }

    public int save(String sql, SqlParameterSource paramSource) {
        return update(sql, paramSource);
    }

    public <T> int save(String sql, T model) {
        return update(sql, model);
    }

    public int saveAndGetKey(String sql, SqlParameterSource paramSource) throws DataAccessException {

        final KeyHolder keyHolder = key();

        int insertRow = namedParameterJdbcTemplate.update(sql, paramSource, keyHolder);

        if (insertRow != 1) {
            throw new IncorrectResultSizeDataAccessException(String.format("Sql save fails, # of save rows : %d", insertRow), 1);
        }

        return Objects.requireNonNull(keyHolder.getKey()).intValue();

    }

    public <T> int saveAndGetKey(String sql, T model) throws DataAccessException {

        final KeyHolder keyHolder = key();

        int insertRow = namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(model), keyHolder);

        if (insertRow != 1) {
            throw new IncorrectResultSizeDataAccessException(String.format("Sql save fails, # of save rows : %d", insertRow), 1);
        }

        return Objects.requireNonNull(keyHolder.getKey()).intValue();

    }

    public int saveOrUpdate(String sql, SqlParameterSource paramSource) {
        return update(sql, paramSource);
    }

    public <T> int saveOrUpdate(String sql, T model) {
        return update(sql, model);
    }

    public boolean delete(String sql, SqlParameterSource paramSource) {

        int insertRow = update(sql, paramSource);

        if (insertRow != 1) {
            throw new IncorrectResultSizeDataAccessException(String.format("Sql delete fails, # of deleted rows : %d", insertRow), 1);
        }

        return true;

    }

    private KeyHolder key() {
        return new GeneratedKeyHolder();
    }

}
