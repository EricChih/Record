package com.ub.gir.web.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserUpdateDto {
    private int ID;

    @NotBlank(message = "登入帳號不可為空")
    @Size(min = 1, max = 20,message = "登入帳號長度範圍1-20")
    private String Name;

    @NotBlank(message = "名字不可為空")
    @Size(min = 1, max = 20,message = "名字長度範圍1-20")
    private String LastName;

    @NotBlank(message = "姓氏不可為空")
    @Size(min = 1, max = 20,message = "登入帳號長度範圍1-20")
    private String FirstName;

    private String Role="admin";

    @NotBlank(message = "部門不可為空")
    private String DepID;

    private String Status="1";
}