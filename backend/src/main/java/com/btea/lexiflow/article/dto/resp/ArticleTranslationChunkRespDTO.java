package com.btea.lexiflow.article.dto.resp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:04
 * @Description: 文章分块翻译结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslationChunkRespDTO {

    /**
     * 当前块段落翻译结果
     */
    private List<ArticleTranslatedParagraphDTO> paragraphs;

    /**
     * 当前块摘要
     */
    @JsonAlias("chunk_summary")
    private String chunkSummary;

    /**
     * 新增术语
     */
    @JsonAlias("new_terms")
    private List<ArticleTranslationTermDTO> newTerms;

    /**
     * 尚未闭合的指代或上下文
     */
    @JsonAlias("open_references")
    private List<String> openReferences;
}
