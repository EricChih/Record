/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.jdbc;


import javax.annotation.Resource;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;


/**
 * <p>定義 DB1 Master JDBC 設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Configuration
@Slf4j
public class DB1MasterJdbcConfig {

    @Resource(name = "db1MasterDataSource")
    private DataSource dataSource;

    @Bean
    public JdbcTemplate db1MasterJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate db1MasterNamedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public DataSourceTransactionManager db1MasterJdbcTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SQLErrorCodeSQLExceptionTranslator db1MasterJdbcExceptionTranslator() {
        SQLErrorCodeSQLExceptionTranslator sqlExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator();
        sqlExceptionTranslator.setDataSource(dataSource);
        return sqlExceptionTranslator;
    }

}
