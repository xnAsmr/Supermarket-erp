package com.supermarket.erp.module.member.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class MemberCreateDTO {

    @NotBlank(message = "会员姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private Integer gender;

    private LocalDateTime birthday;

    private Long levelId;

    private String email;

    private String address;
}
