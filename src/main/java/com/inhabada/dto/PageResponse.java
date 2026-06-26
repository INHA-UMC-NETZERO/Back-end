package com.inhabada.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int page,
        int size,
        boolean hasNext
) {
    public static <E, T> PageResponse<T> from(Page<E> source, Function<E, T> mapper) {
        List<T> content = source.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
                content,
                source.getTotalElements(),
                source.getNumber(),
                source.getSize(),
                source.hasNext()
        );
    }
}
