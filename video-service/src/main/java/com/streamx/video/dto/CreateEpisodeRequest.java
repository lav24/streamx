package com.streamx.video.dto;

public record CreateEpisodeRequest(Short season, Short episodeNumber, Integer durationSeconds) {
}
