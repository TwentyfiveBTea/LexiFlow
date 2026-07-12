package com.btea.lexiflow.vocab.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryCreateReqDTO;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryWordAddReqDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryWordRespDTO;
import com.btea.lexiflow.vocab.service.VocabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * @Description: 词汇库控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vocab")
public class VocabController {

    private final VocabService vocabService;

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
     * 获取当前用户的词汇库列表
     *
     * @return 词汇库列表
     */
    @GetMapping("/libraries")
    public Result<List<VocabLibraryRespDTO>> listLibraries() {
        return Results.success(vocabService.listLibraries());
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

}
