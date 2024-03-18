/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.repository;


import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;


import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/10
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepositoryImplementation<T, ID> {

    EntityManager getEntityManager();

    /**
     * Batch saving, the default is 1000 submissions at a time
     *
     * @param entities
     */
    default void saveAllInBatch(List<T> entities) {
        saveAllInBatch(entities, 1000);
    }

    /**
     * Batch save
     *
     * @param entities
     * @param batchSize
     */
    default void saveAllInBatch(List<T> entities, int batchSize) {

        for (int i = 0; i < entities.size(); i++) {

            getEntityManager().persist(entities.get(i));

            if (i % batchSize == 0 || i == entities.size() - 1) {
                getEntityManager().flush();
                getEntityManager().clear();
            }

        }

    }

}
