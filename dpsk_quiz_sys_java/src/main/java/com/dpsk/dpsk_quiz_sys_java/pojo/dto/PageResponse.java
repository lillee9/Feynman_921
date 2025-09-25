package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import java.util.List;

/**
 * 分页响应DTO
 * @param <T> 数据类型
 */
public class PageResponse<T> {
    
    private List<T> content; // 当前页数据
    private Integer page; // 当前页码
    private Integer size; // 每页大小
    private Long totalElements; // 总记录数
    private Integer totalPages; // 总页数
    private Boolean first; // 是否第一页
    private Boolean last; // 是否最后一页
    private Boolean hasNext; // 是否有下一页
    private Boolean hasPrevious; // 是否有上一页
    
    public PageResponse() {}
    
    public PageResponse(List<T> content, Integer page, Integer size, Long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 1;
        this.last = page.equals(totalPages);
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
    }
    
    // 静态工厂方法
    public static <T> PageResponse<T> of(List<T> content, Integer page, Integer size, Long totalElements) {
        return new PageResponse<>(content, page, size, totalElements);
    }
    
    // 空页面
    public static <T> PageResponse<T> empty(Integer page, Integer size) {
        return new PageResponse<>(List.of(), page, size, 0L);
    }
    
    // Getters and Setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
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
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Boolean getFirst() {
        return first;
    }
    
    public void setFirst(Boolean first) {
        this.first = first;
    }
    
    public Boolean getLast() {
        return last;
    }
    
    public void setLast(Boolean last) {
        this.last = last;
    }
    
    public Boolean getHasNext() {
        return hasNext;
    }
    
    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public Boolean getHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    @Override
    public String toString() {
        return "PageResponse{" +
                "content=" + content +
                ", page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}