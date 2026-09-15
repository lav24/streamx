package com.streamx.video.dto;

import java.util.UUID;

public record AdBreakRequest(UUID userId, String geo, String device, String ageBracket, String placement) {
}
