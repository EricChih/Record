/**
 * Copyright (c) 2014 GateWeb, Inc.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.datasource;


import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;


/**
 * <p>建立資料庫共用來源設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/12
 */
public abstract class AbstractDataSource {

    @SuppressWarnings("unchecked")
    protected <T extends DataSource> T createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {

        return (T) properties.initializeDataSourceBuilder()
                .type(type)
                .build();

    }

}
