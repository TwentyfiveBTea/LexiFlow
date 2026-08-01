package com.btea.lexiflow.learning.service;

import com.btea.lexiflow.learning.dto.req.WordReviewReqDTO;
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

    /**
     * 获取当前用户所有词汇库的待复习单词列表
     *
     * @return 待复习单词列表
     */
    List<DueWordRespDTO> listDueWords();

    /**
     * 查询当前用户尚未完成的复习会话队列。
     *
     * @return 当前复习会话队列，无会话时返回空列表
     */
    List<DueWordRespDTO> listReviewQueue();

    /**
     * 获取或创建当前用户的复习会话队列。
     *
     * @return 当前复习会话队列
     */
    List<DueWordRespDTO> startReviewSession();

    /**
     * 提交单词复习按钮结果并更新学习进度
     *
     * @param wordId 单词ID
     * @param reqDTO 复习按钮请求参数
     */
    void reviewWord(Long wordId, WordReviewReqDTO reqDTO);

    /**
     * 提交当前复习队列首个单词的结果，并返回更新后的队列。
     *
     * @param wordId 单词ID
     * @param reqDTO 复习结果请求参数
     * @return 更新后的复习会话队列
     */
    List<DueWordRespDTO> reviewSessionWord(Long wordId, WordReviewReqDTO reqDTO);

}
