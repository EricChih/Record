/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.validation;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;


/**
 * <p>針對物件類型的校驗檢核分組定義</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public interface ValidateGroups extends Default {

    interface Query extends ValidateGroups {}

    interface Create extends ValidateGroups {}

    interface Update extends ValidateGroups {}

    interface Delete extends ValidateGroups {}

    @GroupSequence(value = {Query.class, Create.class, Update.class, Delete.class})
    interface All extends ValidateGroups {}

}
