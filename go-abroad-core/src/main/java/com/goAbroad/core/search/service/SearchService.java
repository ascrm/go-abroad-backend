package com.goAbroad.core.search.service;

import com.goAbroad.core.search.dto.*;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 搜索全部（文章、规划、问答、用户）
     */
    SearchResultDTO searchAll(String keyword);
}