package com.btea.lexiflow.common.convention.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/19
 * @Description: 通用分页响应参数
 */
@Data
@Builder
public class PageRespDTO<T> {

    /**
     * 当前页记录
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long totalPages;

    public static <T> PageRespDTO<T> of(List<T> records, long total, int page, int pageSize) {
        long totalPages = total / pageSize + (total % pageSize == 0 ? 0 : 1);
        return PageRespDTO.<T>builder()
                .records(records)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }
}
