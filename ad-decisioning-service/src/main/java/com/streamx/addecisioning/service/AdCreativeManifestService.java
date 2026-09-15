package com.streamx.addecisioning.service;

import com.streamx.addecisioning.cache.CampaignCache;
import com.streamx.addecisioning.config.S3Properties;
import com.streamx.addecisioning.event.CampaignUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdCreativeManifestService {

    private final CampaignCache campaignCache;
    private final S3Properties s3Properties;

    public String getMasterPlaylist(UUID creativeId) {
        CampaignUpdatedEvent.Creative creative = campaignCache.all().stream()
                .flatMap(c -> c.creatives().stream())
                .filter(c -> c.creativeId().equals(creativeId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ad creative not found: " + creativeId));

        List<CampaignUpdatedEvent.Rendition> renditions = creative.renditions();
        if (renditions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No ready renditions for ad creative: " + creativeId);
        }

        StringBuilder playlist = new StringBuilder();
        playlist.append("#EXTM3U\n");
        playlist.append("#EXT-X-VERSION:3\n");

        for (CampaignUpdatedEvent.Rendition r : renditions) {
            int bandwidthBps = r.bitrateKbps() * 1000;
            playlist.append("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d\n"
                    .formatted(bandwidthBps, r.width(), r.height()));
            playlist.append(publicUrlFor(r.hlsPlaylistKey())).append('\n');
        }

        return playlist.toString();
    }

    private String publicUrlFor(String key) {
        return "%s/%s/%s".formatted(s3Properties.publicEndpoint(), s3Properties.bucket(), key);
    }
}
