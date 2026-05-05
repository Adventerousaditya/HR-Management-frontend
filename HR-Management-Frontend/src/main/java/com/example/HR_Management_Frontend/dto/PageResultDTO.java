package com.example.HR_Management_Frontend.dto;

import java.util.List;

/**
 * Generic wrapper returned by the service for paginated list responses.
 *
 * @param <T> DTO type (e.g. DepartmentDTO)
 */
public class PageResultDTO<T> {

    private List<T> content;
    private int     currentPage;
    private int     totalPages;
    private long    totalElements;
    private int     pageSize;

    public PageResultDTO() {}

    public PageResultDTO(List<T> content, int currentPage,
                         int totalPages, long totalElements, int pageSize) {
        this.content       = content;
        this.currentPage   = currentPage;
        this.totalPages    = totalPages;
        this.totalElements = totalElements;
        this.pageSize      = pageSize;
    }

    // ── Getters & Setters ─────────────────────────────────
    public List<T> getContent()         { return content; }
    public void    setContent(List<T> content) { this.content = content; }

    public int  getCurrentPage()        { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int  getTotalPages()         { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements()      { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int  getPageSize()           { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}