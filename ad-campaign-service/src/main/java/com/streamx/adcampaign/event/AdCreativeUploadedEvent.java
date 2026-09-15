package com.streamx.adcampaign.event;

import java.util.UUID;

public record AdCreativeUploadedEvent(UUID creativeId, String s3RawKey) {
}
