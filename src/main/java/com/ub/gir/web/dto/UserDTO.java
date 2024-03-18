package com.ub.gir.web.dto;


import lombok.*;

import javax.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode( callSuper = false )
@ToString( callSuper = true )
@Builder
public class UserDTO {

    private int ID;
    private int PID;
    private String AgentID="NA"; //default NA=沒有
    @NotBlank(message = "名字不可為空")
    private String FirstName; //名字
    @NotBlank(message = "姓氏不可為空")
    private String LastName; //姓氏
    @NotBlank(message = "登入帳號不可為空")
    private String Name; //登入的username
    private String Password;
    private String Role="admin"; //預設為admin
    private String Email="NA";
    private String Ext="NA"; //default NA=沒有
    private String Location;
    private int SupervisorID;
    private int ManagerID;
    private int is_agent;
    private int is_admin;
    @NotBlank(message = "部門不可為空")
    private String DepID;
    private String Status="1"; //1=使用中; 0=停用; 3=使用中+已轉移新密碼
    private String FinalChangeDate;
    private String LastLoginDate;
    private String PasswordNew;

    private String errmsg; //非DB欄位,後端錯誤訊息
    private String sqlstr; //非DB欄位,暫存sql
    private String searchlog; //非DB欄位,存放搜尋條件log
    private String LocationCN; //非DB欄位,帳號所在地的中文 台北(002), 高雄(007)
    private String OtherLocationCN; //非DB欄位,其他點,若user本地在台北,other就是高雄
    private String StatusCN; //非DB欄位,0=停用,1=使用中, 3=密碼已加密
    private String OpenBothSite="N"; //是否同步開啟北高帳號,相同帳號002 and 007同時開
    private int Attempts = 3;

}
