/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.dao;


import javax.annotation.Resource;

import com.ub.gir.web.configuration.persistence.jdbc.JdbcTemplateDaoSupport;


/**
 * <p>資料處理層的基底類別</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public abstract class AbstractDao {

    @Resource(name = "db1MasterJdbcTemplateDaoSupport")
    protected JdbcTemplateDaoSupport db1MasterJdbcTemplateDaoSupport;

    @Resource(name = "db2MasterJdbcTemplateDaoSupport")
    protected JdbcTemplateDaoSupport db2MasterJdbcTemplateDaoSupport;

}
