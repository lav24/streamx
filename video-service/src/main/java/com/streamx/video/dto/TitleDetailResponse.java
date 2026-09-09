package com.streamx.video.dto;

import java.util.List;

public record TitleDetailResponse(TitleResponse title, List<EpisodeResponse> episodes) {
}
