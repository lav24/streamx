package com.streamx.video.repository;

import com.streamx.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    @Query("SELECT v FROM Video v JOIN FETCH v.episode e JOIN FETCH e.title WHERE v.id = :id")
    Optional<Video> findByIdWithEpisodeAndTitle(@Param("id") UUID id);
}
