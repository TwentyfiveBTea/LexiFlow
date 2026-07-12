package com.btea.lexiflow.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.learning.dao.entity.RelUserWordProgressDO;
import com.btea.lexiflow.learning.dao.mapper.RelUserWordProgressMapper;
import com.btea.lexiflow.learning.dto.resp.DueWordRespDTO;
import com.btea.lexiflow.learning.service.LearningService;
import com.btea.lexiflow.vocab.constant.VocabConstant;
import com.btea.lexiflow.vocab.dao.entity.BizVocabEnDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabJpDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabLibraryDO;
import com.btea.lexiflow.vocab.dao.entity.RelVocabLibraryWordDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabLibraryMapper;
import com.btea.lexiflow.vocab.dao.mapper.RelVocabLibraryWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 单词学习服务实现类
 */
@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final RelUserWordProgressMapper progressMapper;
    private final RelVocabLibraryWordMapper libraryWordMapper;
    private final BizVocabLibraryMapper libraryMapper;
    private final BizVocabEnMapper vocabEnMapper;
    private final BizVocabJpMapper vocabJpMapper;

    /**
     * 获取当前用户指定词汇库的待复习单词列表
     *
     * @param libraryId 词汇库ID
     * @return 待复习单词列表
     */
    @Override
    public List<DueWordRespDTO> listDueWords(String libraryId) {
        String userId = getCurrentUserId();
        // 校验词汇库存在且属于当前用户，防止越权查询学习进度
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        List<RelVocabLibraryWordDO> relations = libraryWordMapper.selectList(
                new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (relations.isEmpty()) {
            return List.of();
        }
        return progressMapper.selectList(new LambdaQueryWrapper<RelUserWordProgressDO>()
                        .eq(RelUserWordProgressDO::getUserId, userId)
                        .eq(RelUserWordProgressDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelUserWordProgressDO::getLibraryStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelUserWordProgressDO::getWordId,
                                relations.stream().map(RelVocabLibraryWordDO::getWordId).distinct().toList())
                        .and(q -> q.isNull(RelUserWordProgressDO::getNextReviewAt)
                                .or()
                                .le(RelUserWordProgressDO::getNextReviewAt, new Date()))
                        .orderByAsc(RelUserWordProgressDO::getNextReviewAt))
                .stream()
                .map(this::toDueResp)
                .toList();
    }

    /**
     * 转换待复习单词响应参数
     *
     * @param progress 用户单词学习进度
     * @return 待复习单词响应参数
     */
    private DueWordRespDTO toDueResp(RelUserWordProgressDO progress) {
        DueWordRespDTO.DueWordRespDTOBuilder builder = DueWordRespDTO.builder()
                .wordId(progress.getWordId())
                .languageCode(progress.getLanguageCode());
        if ("ja".equals(progress.getLanguageCode())) {
            BizVocabJpDO word = vocabJpMapper.selectById(progress.getWordId());
            if (word != null) {
                builder.word(word.getWord())
                        .kana(word.getKana())
                        .translations(word.getTranslations());
            }
        } else {
            BizVocabEnDO word = vocabEnMapper.selectById(progress.getWordId());
            if (word != null) {
                builder.word(word.getWord())
                        .us(word.getUs())
                        .uk(word.getUk())
                        .translations(word.getTranslations())
                        .phrases(word.getPhrases());
            }
        }
        return builder.build();
    }

    /**
     * 获取当前用户拥有的有效词汇库
     *
     * @param libraryId 词汇库ID
     * @param userId 用户ID
     * @return 词汇库实体
     */
    private BizVocabLibraryDO getLibrary(String libraryId, String userId) {
        BizVocabLibraryDO library = libraryMapper.selectOne(new LambdaQueryWrapper<BizVocabLibraryDO>()
                .eq(BizVocabLibraryDO::getId, libraryId)
                .eq(BizVocabLibraryDO::getUserId, userId)
                .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (library == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_NOT_FOUND);
        }
        return library;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID
     */
    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }
}
