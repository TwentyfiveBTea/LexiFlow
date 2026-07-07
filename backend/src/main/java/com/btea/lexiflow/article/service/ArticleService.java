package com.btea.lexiflow.article.service;

import com.btea.lexiflow.article.dto.req.ArticleAnalyzeReqDTO;
import com.btea.lexiflow.article.dto.resp.ArticleAnalyzeRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleDetailRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleListRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleUploadRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleVocabOccurrenceRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleVocabRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:44
 * @Description: 文章服务接口
 */
public interface ArticleService {

    /**
     * 上传文章
     *
     * @param file 文章文件
     * @return 上传响应参数
     */
    ArticleUploadRespDTO uploadArticle(MultipartFile file);

    /**
     * 分析文章词汇
     *
     * @param articleId 文章ID
     * @param reqDTO 分析请求参数
     * @return 分析响应参数
     */
    ArticleAnalyzeRespDTO analyzeArticle(String articleId, ArticleAnalyzeReqDTO reqDTO);

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @return 文章详情
     */
    ArticleDetailRespDTO getArticleDetail(String articleId);

    /**
     * 获取文章列表
     *
     * @return 文章列表
     */
    List<ArticleListRespDTO> listArticles();

    /**
     * 获取文章命中词汇列表
     *
     * @param articleId 文章ID
     * @param analysisLevel 词汇分析等级
     * @return 命中词汇列表
     */
    List<ArticleVocabRespDTO> listArticleVocabs(String articleId, String analysisLevel);

}
