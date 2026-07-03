package com.btea.lexiflow.user.service;

import com.btea.lexiflow.user.dto.req.UserChangePasswordReqDTO;
import com.btea.lexiflow.user.dto.req.UserChangeUsernameReqDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/3 14:53
 * @Description: 用户个人资料接口
 */
public interface UserProfileService {

    /**
     * 更改头像
     *
     * @param file 头像文件
     * @return 头像地址
     */
    String changeAvatar(MultipartFile file);

    /**
     * 更改用户名
     *
     * @param reqDTO 更改用户名请求参数
     */
    void changeUsername(UserChangeUsernameReqDTO reqDTO);

    /**
     * 更改密码
     *
     * @param reqDTO 更改密码请求参数
     */
    void changePassword(UserChangePasswordReqDTO reqDTO);
}
