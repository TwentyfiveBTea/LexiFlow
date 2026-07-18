package com.btea.lexiflow.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.user.constant.UserConstant;
import com.btea.lexiflow.user.constant.UserProfileConstant;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.infrastructure.security.util.JwtUtil;
import com.btea.lexiflow.pay.service.CreditAccountService;
import com.btea.lexiflow.user.dao.entity.BizUsersDO;
import com.btea.lexiflow.user.dao.mapper.BizUsersMapper;
import com.btea.lexiflow.user.dto.req.UserLoginReqDTO;
import com.btea.lexiflow.user.dto.req.UserRegisterReqDTO;
import com.btea.lexiflow.user.dto.resp.UserLoginRespDTO;
import com.btea.lexiflow.user.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 22:16
 * @Description: 用户认证接口实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final BizUsersMapper bizUsersMapper;
    private final JwtUtil jwtUtil;
    private final CreditAccountService creditAccountService;

    /**
     * 登录
     *
     * @param reqDTO 登录请求参数
     * @return 登录响应参数
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO reqDTO) {
        BizUsersDO user = bizUsersMapper.selectOne(new LambdaQueryWrapper<BizUsersDO>()
                .eq(BizUsersDO::getEmail, reqDTO.getEmail()));
        if (user == null || !BCrypt.checkpw(reqDTO.getPassword(), user.getPasswordHash())) {
            throw new ClientException(BaseErrorCode.USER_NOT_FOUND_OR_PASSWORD_ERROR);
        }
        validateAccountStatus(user);

        String token = jwtUtil.generateUserToken(user.getId());
        log.info("用户登录成功: userId={}, token={}", user.getId(), token);

        return UserLoginRespDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .token(token)
                .build();
    }

    /**
     * 注册
     *
     * @param reqDTO 注册请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterReqDTO reqDTO) {
        if (!reqDTO.getPassword().equals(reqDTO.getConfirmPassword())) {
            throw new ClientException(BaseErrorCode.PASSWORD_NOT_MATCH);
        }
        Long count = bizUsersMapper.selectCount(new LambdaQueryWrapper<BizUsersDO>()
                .eq(BizUsersDO::getEmail, reqDTO.getEmail()));

        if (count > 0) {
            throw new ClientException(BaseErrorCode.EMAIL_EXIST);
        }

        BizUsersDO user = BizUsersDO.builder()
                .email(reqDTO.getEmail())
                .username(reqDTO.getUsername())
                .passwordHash(BCrypt.hashpw(reqDTO.getPassword()))
                .avatar(UserProfileConstant.DEFAULT_AVATAR)
                .status(UserConstant.STATUS_NORMAL)
                .build();
        bizUsersMapper.insert(user);
        creditAccountService.initializeAccount(user.getId());
        log.info("用户注册成功: userId={}", user.getId());
    }

    /**
     * 校验账号状态
     *
     * @param user 用户实体
     */
    private void validateAccountStatus(BizUsersDO user) {
        if (Integer.valueOf(UserConstant.STATUS_DISABLED).equals(user.getStatus())) {
            throw new ClientException(BaseErrorCode.USER_DISABLED);
        }
        if (Integer.valueOf(UserConstant.STATUS_CANCELED).equals(user.getStatus())) {
            throw new ClientException(BaseErrorCode.USER_CANCELED);
        }
        if (!Integer.valueOf(UserConstant.STATUS_NORMAL).equals(user.getStatus())) {
            throw new ClientException(BaseErrorCode.USER_DISABLED);
        }
    }

    /**
     * 退出登录
     */
    @Override
    public void logout() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        jwtUtil.logout(userId);
        log.info("用户退出登录成功: userId={}", userId);
    }
}
