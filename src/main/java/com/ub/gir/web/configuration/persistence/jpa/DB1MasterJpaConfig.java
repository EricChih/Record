/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.jpa;


import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import com.ub.gir.web.repository.BaseRepositoryFactoryBean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * <p>定義 DB1 Master JPA 設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.ub.gir.web.repository.db1.master"},
        repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class,
        entityManagerFactoryRef = "db1MasterEntityManagerFactory",
        transactionManagerRef = "db1MasterJpaTransactionManager")
@Slf4j
public class DB1MasterJpaConfig extends AbstractJpaConfig {

    @Resource(name = "db1MasterDataSource")
    private DataSource dataSource;

    public DB1MasterJpaConfig() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean db1MasterEntityManagerFactory() {

        JpaVendorAdapter jpaVendorAdapter = jpaVendorAdapter();
        Map<String, Object> jpaProperties = getVendorHibernateProperties();

        final EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties, null);

        return builder
                .dataSource(dataSource)
                .persistenceUnit("db1MasterPersistenceUnit")
                .packages("com.ub.gir.web.entity.db1.master")
                .jta(false)
                .build();

    }

    @Bean
    public EntityManager db1MasterEntityManager() {
        return Objects.requireNonNull(db1MasterEntityManagerFactory().getObject()).createEntityManager();
    }

    @Bean
    @Primary
    public PlatformTransactionManager db1MasterJpaTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(db1MasterEntityManagerFactory().getObject());
        return transactionManager;
    }

}
