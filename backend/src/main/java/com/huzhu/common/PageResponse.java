package com.huzhu.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> records;
    private Long total;
    private Long pageNum;
    private Long pageSize;

    public static <T> PageResponse<T> of(List<T> records, Long total, Long pageNum, Long pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        return response;
    }
}
