package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.util.Map;

/**
 * 分页请求DTO
 */
public class PageRequest {
    
    private Integer page = 1; // 页码，从1开始
    private Integer size = 10; // 每页大小
    private String sortBy; // 排序字段
    private String sortDirection = "ASC"; // 排序方向：ASC, DESC
    private Map<String, Object> filters; // 过滤条件
    private String keyword; // 关键词搜索
    
    public PageRequest() {}
    
    public PageRequest(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }
    
    public PageRequest(Integer page, Integer size, String sortBy, String sortDirection) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
    
    // 计算偏移量
    public Integer getOffset() {
        return (page - 1) * size;
    }
    
    // 验证分页参数
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100; // 限制最大页面大小
        }
        if (sortDirection == null || (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection))) {
            sortDirection = "ASC";
        }
    }
    
    // Getters and Setters
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    public Map<String, Object> getFilters() {
        return filters;
    }
    
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    @Override
    public String toString() {
        return "PageRequest{" +
                "page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", filters=" + filters +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}