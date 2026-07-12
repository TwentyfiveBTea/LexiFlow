package com.btea.lexiflow.learning.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.learning.dto.resp.DueWordRespDTO;
import com.btea.lexiflow.learning.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
     * 获取当前用户指定词汇库的待复习单词列表
     *
     * @param libraryId 词汇库ID
     * @return 待复习单词列表
     */
    @GetMapping("/due")
    public Result<List<DueWordRespDTO>> listDueWords(@RequestParam String libraryId) {
        return Results.success(learningService.listDueWords(libraryId));
    }
}
