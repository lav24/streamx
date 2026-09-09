package com.streamx.transcode.event;

import java.util.UUID;

public record VideoUploadCompletedEvent(UUID videoId, String s3RawKey) {
}
