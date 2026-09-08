package com.supermarket.erp.module.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {

    private String token;
    private String refreshToken;
    private Long expiresIn;
    private UserInfoVO userInfo;

    @Data
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private String avatar;
        private String phone;
        private String email;
        private List<String> permissions;
    }
}
