/**
 * Copyright (c) 2014 GateWeb, Inc.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.datasource;


import javax.annotation.Resource;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;


/**
 * <p>主要資料庫來源設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/12
 */
@Configuration
public class MasterDataSourceConfig extends AbstractDataSource {

    @Resource
    private ObjectProvider<MBeanExporter> mBeanExporter;

    @Bean
    @ConfigurationProperties("ub.datasource.db-1.master")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("ub.datasource.db-1.master.pool")
    public DataSource db1MasterDataSource(DataSourceProperties dataSourceProperties) {
        final HikariDataSource dataSource = createDataSource(dataSourceProperties, HikariDataSource.class);
        dataSource.setAutoCommit(true);
        dataSource.setRegisterMbeans(true);
        mBeanExporter.ifUnique(exporter -> exporter.addExcludedBean("db1MasterDataSource"));

        return dataSource;
    }
}