/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.plugins.checkmarx;

/**
 * <p>For Checkmarx</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/9/26
 */
public class ToolPlugins {

    // Improper Resource Access Authorization
    public static boolean checkAuthorization(String userName) {
        return userName.equals("authorization");
    }

}
