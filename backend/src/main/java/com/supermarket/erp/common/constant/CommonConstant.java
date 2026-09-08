package com.supermarket.erp.common.constant;

public class CommonConstant {

    private CommonConstant() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";

    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;

    public static final Integer DELETED_NO = 0;
    public static final Integer DELETED_YES = 1;

    public static final Integer DEFAULT_PAGE = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    public static final Long SYSTEM_TENANT_ID = 0L;

    public static final String USER_TYPE_NORMAL = "1";
    public static final String USER_TYPE_ADMIN = "2";
    public static final String USER_TYPE_SUPER_ADMIN = "3";
}
