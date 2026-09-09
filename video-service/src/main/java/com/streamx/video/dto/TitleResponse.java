package com.streamx.video.dto;

import com.streamx.video.entity.Title;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TitleResponse(
        UUID id,
        String name,
        String description,
        List<String> genres,
        Short releaseYear,
        String type,
        Instant createdAt) {

    public static TitleResponse from(Title title) {
        return new TitleResponse(
                title.getId(),
                title.getName(),
                title.getDescription(),
                List.of(title.getGenres()),
                title.getReleaseYear(),
                title.getType().name(),
                title.getCreatedAt());
    }
}
