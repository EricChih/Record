/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.dto;


import lombok.*;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/3/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
public class LogDTO {

    private int id;

    private String actionTime;

    private String actionUser;

    private String functionName;

    private String actionType;

    private String info;

    private String actionDepId;

    private String actionRole;

    private String startTime;

    private String endTime;

    private String actionTypeCn; //非DB欄位

}
