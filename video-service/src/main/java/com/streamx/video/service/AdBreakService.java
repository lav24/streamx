package com.streamx.video.service;

import com.streamx.video.client.AdDecisioningClient;
import com.streamx.video.config.PlaybackProperties;
import com.streamx.video.config.S3Properties;
import com.streamx.video.dto.AdBreakCue;
import com.streamx.video.dto.AdBreakRequest;
import com.streamx.video.dto.AdBreakResponse;
import com.streamx.video.dto.AdDecisionRequest;
import com.streamx.video.dto.AdDecisionResult;
import com.streamx.video.entity.User;
import com.streamx.video.entity.Video;
import com.streamx.video.entity.VideoRendition;
import com.streamx.video.repository.UserRepository;
import com.streamx.video.repository.VideoRenditionRepository;
import com.streamx.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdBreakService {

    private static final int TAIL_GUARD_SECONDS = 60;
    private static final String PRE_ROLL = "PRE_ROLL";
    private static final String MID_ROLL = "MID_ROLL";

    private final VideoRepository videoRepository;
    private final VideoRenditionRepository videoRenditionRepository;
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final PlaybackProperties playbackProperties;
    private final UserRepository userRepository;
    private final AdDecisioningClient adDecisioningClient;

    @Transactional(readOnly = true)
    public List<AdBreakCue> getAdBreaks(UUID videoId) {
        VideoRendition rendition = videoRenditionRepository.findByVideoId(videoId).stream()
                .filter(r -> r.getStatus() == VideoRendition.Status.READY)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "No ready renditions for video: " + videoId));

        List<Integer> segmentBoundaries = segmentBoundariesFor(rendition.getHlsPlaylistKey());
        int totalDuration = segmentBoundaries.isEmpty() ? 0 : segmentBoundaries.get(segmentBoundaries.size() - 1);

        List<AdBreakCue> cues = new ArrayList<>();
        cues.add(new AdBreakCue(0, PRE_ROLL));

        int interval = playbackProperties.midRollIntervalSeconds();
        if (interval > 0) {
            for (int target = interval; target < totalDuration - TAIL_GUARD_SECONDS; target += interval) {
                cues.add(new AdBreakCue(nearestBoundary(segmentBoundaries, target), MID_ROLL));
            }
        }

        return cues;
    }

    public AdBreakResponse decideAdBreak(UUID videoId, AdBreakRequest request) {
        Video video = videoRepository.findByIdWithEpisodeAndTitle(videoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Video not found: " + videoId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + request.userId()));

        String[] genres = video.getEpisode().getTitle().getGenres();
        String genre = genres.length > 0 ? genres[0] : null;

        AdDecisionResult result = adDecisioningClient.decide(new AdDecisionRequest(
                request.userId(),
                user.getSubscriptionTier(),
                request.geo(),
                request.device(),
                request.ageBracket(),
                genre,
                request.placement()));

        if (!result.filled()) {
            return AdBreakResponse.noFill();
        }

        return new AdBreakResponse(
                true,
                result.campaignId(),
                result.creativeId(),
                result.durationSeconds(),
                "/playback/ad-manifest/" + result.creativeId());
    }

    public String getAdManifest(UUID creativeId) {
        return adDecisioningClient.fetchManifest(creativeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ad manifest not available for creative: " + creativeId));
    }

    private List<Integer> segmentBoundariesFor(String hlsPlaylistKey) {
        String playlist = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(s3Properties.bucket()).key(hlsPlaylistKey).build())
                .asUtf8String();

        List<Integer> boundaries = new ArrayList<>();
        double cumulative = 0;
        for (String line : playlist.split("\n")) {
            line = line.strip();
            if (line.startsWith("#EXTINF:")) {
                String durationPart = line.substring("#EXTINF:".length()).split(",")[0];
                cumulative += Double.parseDouble(durationPart);
                boundaries.add((int) Math.round(cumulative));
            }
        }
        return boundaries;
    }

    private int nearestBoundary(List<Integer> boundaries, int target) {
        int nearest = target;
        int bestDiff = Integer.MAX_VALUE;
        for (int boundary : boundaries) {
            int diff = Math.abs(boundary - target);
            if (diff < bestDiff) {
                bestDiff = diff;
                nearest = boundary;
            }
        }
        return nearest;
    }
}
