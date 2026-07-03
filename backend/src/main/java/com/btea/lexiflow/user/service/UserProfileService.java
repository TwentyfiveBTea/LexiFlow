package com.btea.lexiflow.user.service;

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
}
