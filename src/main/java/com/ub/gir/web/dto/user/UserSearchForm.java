package com.ub.gir.web.dto.user;

import lombok.Data;

@Data
public class UserSearchForm {
    // 帳號
    private String name;
    private String lastName;
    private String firstName;
    private String agentId;
    private String depId;
    private String role;
    private String ext;
    private String status;
}
