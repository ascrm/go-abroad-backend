package com.goAbroad.core.search.controller;

import com.goAbroad.common.result.R;
import com.goAbroad.core.search.dto.SearchResultDTO;
import com.goAbroad.core.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 搜索全部（文章、规划、问答、用户）
     */
    @GetMapping
    public R<SearchResultDTO> search(@RequestParam String q) {
        SearchResultDTO result = searchService.searchAll(q);
        return R.ok(result);
    }
}