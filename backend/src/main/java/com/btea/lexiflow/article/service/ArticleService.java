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

}
