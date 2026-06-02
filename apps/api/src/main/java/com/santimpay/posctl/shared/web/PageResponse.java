package com.santimpay.posctl.shared.web;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Standard list envelope (matches docs/04). Cursor pagination is layered on top per-endpoint; this
 * offset-based form is the default for admin grids that need a total.
 */
public record PageResponse<T>(List<T> data, PageMeta page) {

    public record PageMeta(int limit, int number, long totalElements, int totalPages) {}

    public static <S, T> PageResponse<T> from(Page<S> page, List<T> mapped) {
        return new PageResponse<>(mapped, new PageMeta(
                page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages()));
    }
}
