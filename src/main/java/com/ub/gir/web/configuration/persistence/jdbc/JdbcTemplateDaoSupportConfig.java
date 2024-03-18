/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.jdbc;


import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * <p>定義 JDBC Dao Support 設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
@ConditionalOnClass(value = JdbcTemplateDaoSupport.class)
@Slf4j
public class JdbcTemplateDaoSupportConfig {

    @Bean
    public JdbcTemplateDaoSupport db1MasterJdbcTemplateDaoSupport(@Qualifier("db1MasterDataSource")DataSource dataSource) {
        return new JdbcTemplateDaoSupport(dataSource);
    }

}
