package edu.br.resource.resourcesystem.dto.response;

import java.util.List;
import java.util.TreeSet;

public record PageResponse<T>(
        List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }

    public List<Integer> visiblePages() {
        if (totalPages == 0) return List.of();
        var pages = new TreeSet<Integer>();
        pages.add(1);
        pages.add(totalPages);
        int current = Math.max(1, Math.min(page, totalPages));
        for (int offset = -1; offset <= 1; offset++) {
            long number = (long) current + offset;
            if (number >= 1 && number <= totalPages) pages.add((int) number);
        }
        return List.copyOf(pages);
    }
}
