/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence.jpa;


import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;


/**
 * <p>定義 JPA 共用設定</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Configuration
@EnableJpaAuditing
@Slf4j
public abstract class AbstractJpaConfig {

    @Bean
    @ConfigurationProperties("spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties("spring.jpa.hibernate")
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }

    protected JpaVendorAdapter jpaVendorAdapter() {

        AbstractJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();

        jpaVendorAdapter.setDatabase(jpaProperties().getDatabase());
        jpaVendorAdapter.setDatabasePlatform(jpaProperties().getDatabasePlatform());
        jpaVendorAdapter.setGenerateDdl(jpaProperties().isGenerateDdl());
        jpaVendorAdapter.setShowSql(jpaProperties().isShowSql());

        return jpaVendorAdapter;

    }

    protected Map<String, String> getVendorJpaProperties() {
        return jpaProperties().getProperties();
    }

    protected Map<String, Object> getVendorHibernateProperties() {
        return getVendorHibernateProperties(jpaProperties());
    }

    protected Map<String, Object> getVendorHibernateProperties(JpaProperties jpaProperties) {
        return hibernateProperties().determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

}
