package com.btea.lexiflow.vocab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.article.constant.ArticleConstant;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.entity.RelArticleVocabDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.article.dao.mapper.RelArticleVocabMapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.learning.dao.entity.RelUserWordProgressDO;
import com.btea.lexiflow.learning.dao.mapper.RelUserWordProgressMapper;
import com.btea.lexiflow.vocab.constant.VocabConstant;
import com.btea.lexiflow.vocab.dao.entity.BizVocabEnDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabJpDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabLibraryDO;
import com.btea.lexiflow.vocab.dao.entity.RelVocabLibraryWordDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabLibraryMapper;
import com.btea.lexiflow.vocab.dao.mapper.RelVocabLibraryWordMapper;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryCreateReqDTO;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryWordAddReqDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryWordRespDTO;
import com.btea.lexiflow.vocab.service.VocabService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库服务实现类
 */
@Service
@RequiredArgsConstructor
public class VocabServiceImpl implements VocabService {

    private final BizVocabLibraryMapper bizVocabLibraryMapper;
    private final RelVocabLibraryWordMapper relVocabLibraryWordMapper;
    private final RelUserWordProgressMapper relUserWordProgressMapper;
    private final BizArticlesMapper bizArticlesMapper;
    private final RelArticleVocabMapper relArticleVocabMapper;
    private final BizVocabEnMapper bizVocabEnMapper;
    private final BizVocabJpMapper bizVocabJpMapper;

    /**
     * 创建词汇库
     *
     * @param reqDTO 创建词汇库请求参数
     * @return 词汇库信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VocabLibraryRespDTO createLibrary(VocabLibraryCreateReqDTO reqDTO) {
        String userId = getCurrentUserId();
        String languageCode = normalizeLanguage(reqDTO.getLanguageCode());
        String name = reqDTO.getName().trim();
        BizVocabLibraryDO existing = bizVocabLibraryMapper.selectOne(new LambdaQueryWrapper<BizVocabLibraryDO>()
                .eq(BizVocabLibraryDO::getUserId, userId)
                .eq(BizVocabLibraryDO::getName, name)
                .eq(BizVocabLibraryDO::getLanguageCode, languageCode));
        if (existing != null) {
            if (Integer.valueOf(VocabConstant.STATUS_NORMAL).equals(existing.getStatus())) {
                throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_EXIST);
            }
            // 恢复同名的已删除词汇库，避免触发唯一键冲突。
            existing.setStatus(VocabConstant.STATUS_NORMAL);
            existing.setDescription(reqDTO.getDescription());
            existing.setDeletedAt(null);
            bizVocabLibraryMapper.updateById(existing);
            return toLibraryResp(existing, 0L);
        }
        BizVocabLibraryDO library = BizVocabLibraryDO.builder()
                .userId(userId)
                .name(name)
                .languageCode(languageCode)
                .description(reqDTO.getDescription() == null ? null : reqDTO.getDescription())
                .status(VocabConstant.STATUS_NORMAL)
                .build();
        bizVocabLibraryMapper.insert(library);
        return toLibraryResp(library, 0L);
    }

    /**
     * 获取当前用户的词汇库列表
     *
     * @return 词汇库列表
     */
    @Override
    public List<VocabLibraryRespDTO> listLibraries() {
        String userId = getCurrentUserId();
        return bizVocabLibraryMapper.selectList(new LambdaQueryWrapper<BizVocabLibraryDO>()
                        .eq(BizVocabLibraryDO::getUserId, userId)
                        .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL)
                        .orderByDesc(BizVocabLibraryDO::getCreatedAt))
                .stream()
                .map(library -> toLibraryResp(library, relVocabLibraryWordMapper.selectCount(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getLibraryId, library.getId())
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL))))
                .toList();
    }

    /**
     * 删除词汇库
     *
     * @param libraryId 词汇库ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLibrary(String libraryId) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        library.setStatus(VocabConstant.STATUS_DELETED);
        library.setDeletedAt(new Date());
        bizVocabLibraryMapper.updateById(library);
        relVocabLibraryWordMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL)
                .set(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_DELETED)
                .set(RelVocabLibraryWordDO::getDeletedAt, new Date()));
    }

    /**
     * 将文章命中词汇加入指定词汇库
     *
     * @param libraryId 词汇库ID
     * @param reqDTO 词汇加入请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArticleVocab(String libraryId, VocabLibraryWordAddReqDTO reqDTO) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);

        // 校验文章存在、属于当前用户且处于正常状态。
        BizArticlesDO article = bizArticlesMapper.selectOne(new LambdaQueryWrapper<BizArticlesDO>()
                .eq(BizArticlesDO::getId, reqDTO.getArticleId())
                .eq(BizArticlesDO::getUserId, userId)
                .eq(BizArticlesDO::getStatus, ArticleConstant.STATUS_NORMAL));
        if (article == null) {
            throw new ClientException(BaseErrorCode.ARTICLE_NOT_FOUND);
        }

        // 同时约束文章、文章词汇和当前用户，防止跨文章或跨用户添加。
        RelArticleVocabDO articleVocab = relArticleVocabMapper.selectOne(new LambdaQueryWrapper<RelArticleVocabDO>()
                .eq(RelArticleVocabDO::getId, reqDTO.getArticleVocabId())
                .eq(RelArticleVocabDO::getArticleId, reqDTO.getArticleId())
                .eq(RelArticleVocabDO::getUserId, userId));
        if (articleVocab == null) {
            throw new ClientException(BaseErrorCode.VOCAB_SOURCE_NOT_FOUND);
        }
        if (!library.getLanguageCode().equalsIgnoreCase(articleVocab.getLanguageCode())) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_LANGUAGE_MISMATCH);
        }

        RelVocabLibraryWordDO relation = relVocabLibraryWordMapper.selectOne(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getWordId, articleVocab.getWordId())
                .eq(RelVocabLibraryWordDO::getLanguageCode, articleVocab.getLanguageCode()));
        if (relation == null) {
            relation = RelVocabLibraryWordDO.builder()
                    .libraryId(libraryId)
                    .userId(userId)
                    .wordId(articleVocab.getWordId())
                    .languageCode(articleVocab.getLanguageCode())
                    .status(VocabConstant.STATUS_NORMAL)
                    .build();
            relVocabLibraryWordMapper.insert(relation);
        } else if (Integer.valueOf(VocabConstant.STATUS_DELETED).equals(relation.getStatus())) {
            // 已软删除的相同词条直接恢复，保留原关系记录。
            relation.setStatus(VocabConstant.STATUS_NORMAL);
            relation.setDeletedAt(null);
            relVocabLibraryWordMapper.updateById(relation);
        } else {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_WORD_EXIST);
        }
        restoreProgress(userId, articleVocab.getWordId(), articleVocab.getLanguageCode());
    }

    /**
     * 获取指定词汇库中的词条列表
     *
     * @param libraryId 词汇库ID
     * @return 词汇库词条列表
     */
    @Override
    public List<VocabLibraryWordRespDTO> listLibraryWords(String libraryId) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        List<RelVocabLibraryWordDO> relations = relVocabLibraryWordMapper.selectList(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL)
                .orderByAsc(RelVocabLibraryWordDO::getCreatedAt));
        if (relations.isEmpty()) {
            return List.of();
        }
        Map<Long, RelUserWordProgressDO> progresses = relUserWordProgressMapper.selectList(new LambdaQueryWrapper<RelUserWordProgressDO>()
                        .eq(RelUserWordProgressDO::getUserId, userId)
                        .eq(RelUserWordProgressDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelUserWordProgressDO::getLibraryStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelUserWordProgressDO::getWordId, relations.stream().map(RelVocabLibraryWordDO::getWordId).toList()))
                .stream().collect(Collectors.toMap(RelUserWordProgressDO::getWordId, Function.identity(), (a, b) -> a));
        return relations.stream().map(relation -> toWordResp(relation, progresses.get(relation.getWordId()), library.getLanguageCode())).toList();
    }

    /**
     * 从指定词汇库中删除词条
     *
     * @param libraryId 词汇库ID
     * @param wordId 词条ID
     * @param languageCode 语言标识
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLibraryWord(String libraryId, Long wordId, String languageCode) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        String language = normalizeLanguage(languageCode);
        if (!library.getLanguageCode().equals(language)) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_LANGUAGE_MISMATCH);
        }
        RelVocabLibraryWordDO relation = relVocabLibraryWordMapper.selectOne(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getWordId, wordId)
                .eq(RelVocabLibraryWordDO::getLanguageCode, language)
                .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (relation == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_WORD_NOT_FOUND);
        }
        relation.setStatus(VocabConstant.STATUS_DELETED);
        relation.setDeletedAt(new Date());
        relVocabLibraryWordMapper.updateById(relation);
    }

    private void restoreProgress(String userId, Long wordId, String languageCode) {
        RelUserWordProgressDO progress = relUserWordProgressMapper.selectOne(new LambdaQueryWrapper<RelUserWordProgressDO>()
                .eq(RelUserWordProgressDO::getUserId, userId)
                .eq(RelUserWordProgressDO::getWordId, wordId)
                .eq(RelUserWordProgressDO::getLanguageCode, languageCode));
        if (progress == null) {
            relUserWordProgressMapper.insert(RelUserWordProgressDO.builder()
                    .userId(userId)
                    .wordId(wordId)
                    .languageCode(languageCode)
                    .status(VocabConstant.WORD_STATUS_NEW)
                    .libraryStatus(VocabConstant.STATUS_NORMAL)
                    .reviewCount(0)
                    .easinessFactor(VocabConstant.DEFAULT_EASINESS_FACTOR)
                    .intervalDays(0)
                    .build());
        // 恢复曾从词汇库移除的学习进度，避免重复创建记录。
        } else if (Integer.valueOf(VocabConstant.STATUS_DELETED).equals(progress.getLibraryStatus())) {
            progress.setLibraryStatus(VocabConstant.STATUS_NORMAL);
            progress.setDeletedAt(null);
            relUserWordProgressMapper.updateById(progress);
        }
    }

    private BizVocabLibraryDO getLibrary(String libraryId, String userId) {
        // 将用户条件并入查询，防止跨用户访问词汇库。
        BizVocabLibraryDO library = bizVocabLibraryMapper.selectOne(new LambdaQueryWrapper<BizVocabLibraryDO>()
                .eq(BizVocabLibraryDO::getId, libraryId)
                .eq(BizVocabLibraryDO::getUserId, userId)
                .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (library == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_NOT_FOUND);
        }
        return library;
    }

    private VocabLibraryRespDTO toLibraryResp(BizVocabLibraryDO library, long wordCount) {
        return VocabLibraryRespDTO.builder()
                .libraryId(library.getId())
                .name(library.getName())
                .languageCode(library.getLanguageCode())
                .description(library.getDescription())
                .wordCount(wordCount)
                .createdAt(library.getCreatedAt())
                .updatedAt(library.getUpdatedAt())
                .build();
    }

    private VocabLibraryWordRespDTO toWordResp(RelVocabLibraryWordDO relation, RelUserWordProgressDO progress, String languageCode) {
        VocabLibraryWordRespDTO.VocabLibraryWordRespDTOBuilder builder = VocabLibraryWordRespDTO.builder()
                .libraryWordId(relation.getId())
                .wordId(relation.getWordId())
                .languageCode(languageCode)
                .addedAt(relation.getCreatedAt());
        if ("ja".equals(languageCode)) {
            BizVocabJpDO word = bizVocabJpMapper.selectById(relation.getWordId());
            if (word != null) {
                builder.word(word.getWord())
                        .kana(word.getKana())
                        .translations(word.getTranslations());
            }
        } else {
            BizVocabEnDO word = bizVocabEnMapper.selectById(relation.getWordId());
            if (word != null) {
                builder.word(word.getWord())
                        .us(word.getUs())
                        .uk(word.getUk())
                        .translations(word.getTranslations())
                        .phrases(word.getPhrases());
            }
        }
        if (progress != null) {
            builder.learningStatus(progress.getStatus())
                    .reviewCount(progress.getReviewCount())
                    .nextReviewAt(progress.getNextReviewAt());
        }
        return builder.build();
    }

    private String normalizeLanguage(String languageCode) {
        String language = languageCode == null ? "" : languageCode.trim().toLowerCase(Locale.ROOT);
        if (!VocabConstant.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException(BaseErrorCode.VOCAB_LANGUAGE_NOT_SUPPORTED);
        }
        return language;
    }

    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }
}
