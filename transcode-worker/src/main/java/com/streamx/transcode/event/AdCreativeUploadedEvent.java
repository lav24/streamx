package com.streamx.transcode.event;

import java.util.UUID;

public record AdCreativeUploadedEvent(UUID creativeId, String s3RawKey) {
}
