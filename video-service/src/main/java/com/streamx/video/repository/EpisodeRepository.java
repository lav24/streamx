package com.streamx.video.repository;

import com.streamx.video.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EpisodeRepository extends JpaRepository<Episode, UUID> {

    @Query("SELECT e FROM Episode e JOIN FETCH e.title WHERE e.title.id = :titleId ORDER BY e.season, e.episodeNumber")
    List<Episode> findByTitleIdWithTitle(@Param("titleId") UUID titleId);
}
