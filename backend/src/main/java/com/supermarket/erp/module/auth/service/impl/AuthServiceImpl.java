package com.supermarket.erp.module.auth.service.impl;

import com.supermarket.erp.common.enums.ResultCodeEnum;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.security.JwtTokenProvider;
import com.supermarket.erp.common.security.LoginUser;
import com.supermarket.erp.module.auth.dto.LoginDTO;
import com.supermarket.erp.module.auth.dto.LoginVO;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.auth.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 验证用户名密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 生成 Token
        String token = jwtTokenProvider.generateToken(
                loginUser.getId(),
                loginUser.getUsername(),
                loginUser.getTenantId(),
                loginUser.getStoreId()
        );

        // 更新登录信息
        User user = new User();
        user.setId(loginUser.getId());
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(loginUser.getLoginCount() == null ? 1 : loginUser.getLoginCount() + 1);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(loginUser.getId());
        userMapper.updateById(user);

        // 构建返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setRefreshToken(token); // 简化处理，实际应生成不同的 refreshToken
        vo.setExpiresIn(7200L);

        LoginVO.UserInfoVO userInfoVO = new LoginVO.UserInfoVO();
        userInfoVO.setId(loginUser.getId());
        userInfoVO.setUsername(loginUser.getUsername());
        userInfoVO.setRealName(loginUser.getName());
        userInfoVO.setRole(loginUser.getUserType() == 3 ? "system_admin" : "admin");
        userInfoVO.setAvatar(loginUser.getAvatar());
        userInfoVO.setPhone(loginUser.getPhone());
        userInfoVO.setEmail(loginUser.getEmail());
        userInfoVO.setPermissions(loginUser.getPermissions() != null
                ? new ArrayList<>(loginUser.getPermissions())
                : List.of());
        vo.setUserInfo(userInfoVO);

        return vo;
    }

    @Override
    @Transactional
    public void logout(String token) {
        // JWT 无状态，服务端无需额外处理
        // 如果使用 Redis 存储 token，此处应删除
    }
}
