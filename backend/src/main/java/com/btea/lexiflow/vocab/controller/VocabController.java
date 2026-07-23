package com.btea.lexiflow.vocab.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.learning.service.LearningService;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryCreateReqDTO;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryWordAddReqDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryStatisticsRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryWordRespDTO;
import com.btea.lexiflow.vocab.service.VocabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vocab")
public class VocabController {

    private final VocabService vocabService;
    private final LearningService learningService;

    /**
     * 创建词汇库
     *
     * @param reqDTO 创建词汇库请求参数
     * @return 词汇库信息
     */
    @PostMapping("/libraries")
    public Result<VocabLibraryRespDTO> createLibrary(@RequestBody @Valid VocabLibraryCreateReqDTO reqDTO) {
        return Results.success(vocabService.createLibrary(reqDTO));
    }

    /**
     * 查询当前用户的词汇库列表
     *
     * @param keyword 词汇库名称关键词
     * @param languageCode 语言标识：en/ja
     * @return 词汇库列表
     */
    @GetMapping("/libraries")
    public Result<List<VocabLibraryRespDTO>> listLibraries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String languageCode) {
        return Results.success(vocabService.listLibraries(keyword, languageCode));
    }

    /**
     * 删除词汇库
     *
     * @param libraryId 词汇库ID
     * @return 响应结果
     */
    @DeleteMapping("/libraries/{libraryId}")
    public Result<Void> deleteLibrary(@PathVariable String libraryId) {
        vocabService.deleteLibrary(libraryId);
        return Results.success();
    }

    /**
     * 将文章命中词汇加入指定词汇库
     *
     * @param libraryId 词汇库ID
     * @param reqDTO 词汇加入请求参数
     * @return 响应结果
     */
    @PostMapping("/libraries/{libraryId}/words")
    public Result<Void> addArticleVocab(@PathVariable String libraryId,
                                        @RequestBody @Valid VocabLibraryWordAddReqDTO reqDTO) {
        vocabService.addArticleVocab(libraryId, reqDTO);
        return Results.success();
    }

    /**
     * 获取指定词汇库中的词条列表
     *
     * @param libraryId 词汇库ID
     * @param keyword 单词关键词
     * @param level 词汇等级
     * @return 词汇库词条列表
     */
    @GetMapping("/libraries/{libraryId}/words")
    public Result<List<VocabLibraryWordRespDTO>> listLibraryWords(
            @PathVariable String libraryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level) {
        return Results.success(vocabService.listLibraryWords(libraryId, keyword, level));
    }

    /**
     * 获取指定词汇库的学习统计
     *
     * @param libraryId 词汇库ID
     * @return 词汇库学习统计
     */
    @GetMapping("/libraries/{libraryId}/statistics")
    public Result<VocabLibraryStatisticsRespDTO> getLibraryStatistics(@PathVariable String libraryId) {
        return Results.success(vocabService.getLibraryStatistics(libraryId));
    }

    /**
     * 从指定词汇库中删除词条
     *
     * @param libraryId 词汇库ID
     * @param wordId 词条ID
     * @param languageCode 语言标识
     * @return 响应结果
     */
    @DeleteMapping("/libraries/{libraryId}/words/{wordId}")
    public Result<Void> deleteLibraryWord(@PathVariable String libraryId,
                                          @PathVariable Long wordId,
                                          @RequestParam String languageCode) {
        vocabService.deleteLibraryWord(libraryId, wordId, languageCode);
        return Results.success();
    }

    /**
     * 查询当前用户当天剩余待复习单词数量
     *
     * @return 当天剩余待复习单词数量
     */
    @GetMapping("/due")
    public Result<Integer> countTodayDueWords() {
        return Results.success(learningService.listDueWords().size());
    }
}
