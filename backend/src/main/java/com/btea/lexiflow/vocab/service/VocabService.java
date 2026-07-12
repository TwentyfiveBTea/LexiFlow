package com.btea.lexiflow.vocab.service;

import com.btea.lexiflow.vocab.dto.req.VocabLibraryCreateReqDTO;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryWordAddReqDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryWordRespDTO;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库服务接口
 */
public interface VocabService {

    /**
     * 创建词汇库
     *
     * @param reqDTO 创建词汇库请求参数
     * @return 词汇库信息
     */
    VocabLibraryRespDTO createLibrary(VocabLibraryCreateReqDTO reqDTO);

    /**
     * 获取当前用户的词汇库列表
     *
     * @return 词汇库列表
     */
    List<VocabLibraryRespDTO> listLibraries();

    /**
     * 删除词汇库
     *
     * @param libraryId 词汇库ID
     */
    void deleteLibrary(String libraryId);

    /**
     * 将文章命中词汇加入指定词汇库
     *
     * @param libraryId 词汇库ID
     * @param reqDTO 词汇加入请求参数
     */
    void addArticleVocab(String libraryId, VocabLibraryWordAddReqDTO reqDTO);

    /**
     * 获取指定词汇库中的词条列表
     *
     * @param libraryId 词汇库ID
     * @return 词汇库词条列表
     */
    List<VocabLibraryWordRespDTO> listLibraryWords(String libraryId);

    /**
     * 从指定词汇库中删除词条
     *
     * @param libraryId 词汇库ID
     * @param wordId 词条ID
     * @param languageCode 语言标识
     */
    void deleteLibraryWord(String libraryId, Long wordId, String languageCode);
}
