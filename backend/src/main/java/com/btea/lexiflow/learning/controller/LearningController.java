package com.btea.lexiflow.learning.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.learning.dto.req.WordReviewReqDTO;
import com.btea.lexiflow.learning.dto.resp.DueWordRespDTO;
import com.btea.lexiflow.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 单词学习控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final LearningService learningService;

    /**
     * 获取当前用户待复习单词列表，可按词汇库限定范围
     *
     * @param libraryId 可选的词汇库ID
     * @return 待复习单词列表
     */
    @GetMapping("/due")
    public Result<List<DueWordRespDTO>> listDueWords(@RequestParam(required = false) String libraryId) {
        return Results.success(libraryId == null || libraryId.isBlank()
                ? learningService.listDueWords()
                : learningService.listDueWords(libraryId));
    }

    /**
     * 提交单词复习按钮结果并更新学习进度
     *
     * @param wordId 单词ID
     * @param reqDTO 复习按钮请求参数
     * @return 操作结果
     */
    @PostMapping("/words/{wordId}/review")
    public Result<Void> reviewWord(@PathVariable Long wordId,
                                   @RequestBody @Valid WordReviewReqDTO reqDTO) {
        learningService.reviewWord(wordId, reqDTO);
        return Results.success();
    }

}
