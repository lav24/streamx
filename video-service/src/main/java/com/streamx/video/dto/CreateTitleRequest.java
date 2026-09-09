package com.streamx.video.dto;

import com.streamx.video.entity.Title;

import java.util.List;

public record CreateTitleRequest(
        String name,
        String description,
        List<String> genres,
        Short releaseYear,
        Title.TitleType type) {
}
