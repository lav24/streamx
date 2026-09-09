package com.streamx.video.dto;

import java.util.UUID;

public record UploadInitRequest(UUID episodeId, String fileExtension) {
}
