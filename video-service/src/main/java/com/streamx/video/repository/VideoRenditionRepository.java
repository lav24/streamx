package com.streamx.video.repository;

import com.streamx.video.entity.VideoRendition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoRenditionRepository extends JpaRepository<VideoRendition, UUID> {

    List<VideoRendition> findByVideoId(UUID videoId);
}
