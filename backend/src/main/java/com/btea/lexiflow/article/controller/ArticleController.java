package com.btea.lexiflow.article.controller;

import com.btea.lexiflow.article.dto.req.ArticleAnalyzeReqDTO;
import com.btea.lexiflow.article.dto.resp.ArticleAnalyzeRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleDetailRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleListRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleProcessingDetailRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleUploadRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleVocabOccurrenceRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleVocabRespDTO;
import com.btea.lexiflow.article.service.ArticleService;
import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:45
 * @Description: 文章控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/article")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 上传文章
     *
     * @param file 文章文件
     * @return 响应结果
     */
    @PostMapping("/upload")
    public Result<ArticleUploadRespDTO> uploadArticle(@RequestParam("file") MultipartFile file) {
        return Results.success(articleService.uploadArticle(file));
    }

    /**
     * 分析文章词汇
     *
     * @param articleId 文章ID
     * @param reqDTO 分析请求参数
     * @return 响应结果
     */
    @PostMapping("/{articleId}/analyze")
    public Result<ArticleAnalyzeRespDTO> analyzeArticle(@PathVariable String articleId,
                                                        @RequestBody @Valid ArticleAnalyzeReqDTO reqDTO) {
        return Results.success(articleService.analyzeArticle(articleId, reqDTO));
    }

    /**
     * 查询文章列表
     *
     * @param keyword 文章标题关键词
     * @param languageCode 语言标识：en/ja
     * @return 响应结果
     */
    @GetMapping("/list")
    public Result<List<ArticleListRespDTO>> listArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String languageCode) {
        return Results.success(articleService.listArticles(keyword, languageCode));
    }

    /**
     * 获取文章处理详情
     *
     * @param articleId 文章ID
     * @return 文章处理详情
     */
    @GetMapping("/{articleId}/processing-detail")
    public Result<ArticleProcessingDetailRespDTO> getArticleProcessingDetail(@PathVariable String articleId) {
        return Results.success(articleService.getArticleProcessingDetail(articleId));
    }

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @return 响应结果
     */
    @GetMapping("/{articleId}")
    public Result<ArticleDetailRespDTO> getArticleDetail(@PathVariable String articleId) {
        return Results.success(articleService.getArticleDetail(articleId));
    }

    /**
     * 获取文章命中词汇列表
     *
     * @param articleId 文章ID
     * @param analysisLevel 词汇分析等级
     * @return 响应结果
     */
    @GetMapping("/{articleId}/vocabs")
    public Result<List<ArticleVocabRespDTO>> listArticleVocabs(@PathVariable String articleId,
                                                               @RequestParam("analysisLevel") String analysisLevel) {
        return Results.success(articleService.listArticleVocabs(articleId, analysisLevel));
    }

    /**
     * 获取文章已解析的词汇等级
     *
     * @param articleId 文章ID
     * @return 已解析的词汇等级列表
     */
    @GetMapping("/{articleId}/vocab-levels")
    public Result<List<String>> listArticleVocabLevels(@PathVariable String articleId) {
        return Results.success(articleService.listArticleVocabLevels(articleId));
    }

    /**
     * 获取词汇出现位置列表
     *
     * @param articleId 文章ID
     * @param articleVocabId 文章命中词汇汇总ID
     * @return 响应结果
     */
    @GetMapping("/{articleId}/vocabs/{articleVocabId}/occurrences")
    public Result<List<ArticleVocabOccurrenceRespDTO>> listArticleVocabOccurrences(@PathVariable String articleId,
                                                                                   @PathVariable String articleVocabId) {
        return Results.success(articleService.listArticleVocabOccurrences(articleId, articleVocabId));
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @return 响应结果
     */
    @DeleteMapping("/{articleId}")
    public Result<Void> deleteArticle(@PathVariable String articleId) {
        articleService.deleteArticle(articleId);
        return Results.success();
    }
}
