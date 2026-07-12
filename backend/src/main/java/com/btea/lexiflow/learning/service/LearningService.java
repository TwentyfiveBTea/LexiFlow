package com.btea.lexiflow.learning.service;

import com.btea.lexiflow.learning.dto.resp.DueWordRespDTO;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 单词学习服务接口
 */
public interface LearningService {

    /**
     * 获取当前用户指定词汇库的待复习单词列表
     *
     * @param libraryId 词汇库ID
     * @return 待复习单词列表
     */
    List<DueWordRespDTO> listDueWords(String libraryId);
}
