package com.streamx.video.dto;

import com.streamx.video.entity.Episode;

import java.util.UUID;

public record EpisodeResponse(
        UUID id,
        UUID titleId,
        Short season,
        Short episodeNumber,
        Integer durationSeconds) {

    public static EpisodeResponse from(Episode episode) {
        return new EpisodeResponse(
                episode.getId(),
                episode.getTitle().getId(),
                episode.getSeason(),
                episode.getEpisodeNumber(),
                episode.getDurationSeconds());
    }
}
