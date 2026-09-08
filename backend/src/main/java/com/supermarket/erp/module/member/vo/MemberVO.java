package com.supermarket.erp.module.member.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberVO {

    private Long id;

    private String memberNo;

    private String name;

    private String phone;

    private Integer gender;

    private LocalDateTime birthday;

    private Long levelId;

    private String levelName;

    private Integer points;

    private BigDecimal balance;

    private BigDecimal totalConsume;

    private Integer totalPoints;

    private String email;

    private String address;

    private Integer status;

    private LocalDateTime registerTime;

    private LocalDateTime createTime;
}
