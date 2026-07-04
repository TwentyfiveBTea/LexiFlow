package com.btea.lexiflow.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.btea.lexiflow.common.constant.UserProfileConstant;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.infrastructure.s3.S3Util;
import com.btea.lexiflow.user.dao.entity.BizUsersDO;
import com.btea.lexiflow.user.dao.mapper.BizUsersMapper;
import com.btea.lexiflow.user.dto.req.UserChangePasswordReqDTO;
import com.btea.lexiflow.user.dto.req.UserChangeUsernameReqDTO;
import com.btea.lexiflow.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/3 14:54
 * @Description: 用户个人资料服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final BizUsersMapper bizUsersMapper;
    private final S3Util s3Util;

    /**
     * 更改头像
     *
     * @param file 头像文件
     * @return 头像地址
     */
    @Override
    public String changeAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException(BaseErrorCode.AVATAR_FILE_EMPTY);
        }

        BizUsersDO user = getCurrentUser();
        String objectKey = UserProfileConstant.AVATAR_DIR + user.getId() + UserProfileConstant.AVATAR_SUFFIX;
        String uploadedObjectKey = s3Util.uploadFile(file, objectKey);
        String avatar = UserProfileConstant.AVATAR_BASE_URL + uploadedObjectKey;

        user.setAvatar(avatar);
        bizUsersMapper.updateById(user);
        log.info("用户头像修改成功: userId={}, avatar={}", user.getId(), avatar);
        return avatar;
    }

    /**
     * 更改用户名
     *
     * @param reqDTO 更改用户名请求参数
     */
    @Override
    public void changeUsername(UserChangeUsernameReqDTO reqDTO) {
        BizUsersDO user = getCurrentUser();
        if (reqDTO.getUsername().equals(user.getUsername())) {
            throw new ClientException(BaseErrorCode.NEW_USERNAME_SAME_AS_OLD_USERNAME);
        }

        user.setUsername(reqDTO.getUsername());
        bizUsersMapper.updateById(user);
        log.info("用户用户名修改成功: userId={}", user.getId());
    }

    /**
     * 更改密码
     *
     * @param reqDTO 更改密码请求参数
     */
    @Override
    public void changePassword(UserChangePasswordReqDTO reqDTO) {
        if (!reqDTO.getNewPassword().equals(reqDTO.getConfirmPassword())) {
            throw new ClientException(BaseErrorCode.PASSWORD_NOT_MATCH);
        }

        BizUsersDO user = getCurrentUser();
        if (!BCrypt.checkpw(reqDTO.getOldPassword(), user.getPasswordHash())) {
            throw new ClientException(BaseErrorCode.PASSWORD_ERROR);
        }
        if (BCrypt.checkpw(reqDTO.getNewPassword(), user.getPasswordHash())) {
            throw new ClientException(BaseErrorCode.NEW_PASSWORD_SAME_AS_OLD_PASSWORD);
        }

        user.setPasswordHash(BCrypt.hashpw(reqDTO.getNewPassword()));
        bizUsersMapper.updateById(user);
        log.info("用户密码修改成功: userId={}", user.getId());
    }

    /**
     * 获取当前登录用户
     *
     * @return 用户信息
     */
    private BizUsersDO getCurrentUser() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }

        BizUsersDO user = bizUsersMapper.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
