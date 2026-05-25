package com.tamdao.restful_api_design.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CommonPageResponse<T> {
    private List<T> data;
    private PagingMetadata paging;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PagingMetadata {
        private int limit;
        private Long nextCursor;
        private Boolean hasMore;
    }
}
