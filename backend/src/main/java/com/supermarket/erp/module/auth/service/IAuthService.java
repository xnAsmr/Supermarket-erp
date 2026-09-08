package com.supermarket.erp.module.auth.service;

import com.supermarket.erp.module.auth.dto.LoginDTO;
import com.supermarket.erp.module.auth.dto.LoginVO;

public interface IAuthService {

    LoginVO login(LoginDTO dto);

    void logout(String token);
}
