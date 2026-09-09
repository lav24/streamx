package com.streamx.video.event;

import java.util.UUID;

public record VideoUploadCompletedEvent(UUID videoId, String s3RawKey) {
}
