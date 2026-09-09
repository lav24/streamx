package com.streamx.video.service;

import com.streamx.video.config.S3Properties;
import com.streamx.video.entity.Video;
import com.streamx.video.entity.VideoRendition;
import com.streamx.video.repository.VideoRenditionRepository;
import com.streamx.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaybackService {

    private final VideoRepository videoRepository;
    private final VideoRenditionRepository videoRenditionRepository;
    private final S3Properties s3Properties;

    @Transactional(readOnly = true)
    public String getMasterPlaylist(UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Video not found: " + videoId));

        if (video.getStatus() != Video.Status.READY) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Video is not ready for playback: " + video.getStatus());
        }

        List<VideoRendition> renditions = videoRenditionRepository.findByVideoId(videoId).stream()
                .filter(r -> r.getStatus() == VideoRendition.Status.READY)
                .toList();

        if (renditions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No ready renditions for video: " + videoId);
        }

        StringBuilder playlist = new StringBuilder();
        playlist.append("#EXTM3U\n");
        playlist.append("#EXT-X-VERSION:3\n");

        for (VideoRendition r : renditions) {
            int bandwidthBps = r.getBitrateKbps() * 1000;
            playlist.append("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d\n"
                    .formatted(bandwidthBps, r.getWidth(), r.getHeight()));
            playlist.append(publicUrlFor(r.getHlsPlaylistKey())).append('\n');
        }

        return playlist.toString();
    }

    private String publicUrlFor(String key) {
        return "%s/%s/%s".formatted(s3Properties.publicEndpoint(), s3Properties.bucket(), key);
    }
}
