package com.streamx.video.repository;

import com.streamx.video.entity.WatchHistory;
import com.streamx.video.entity.WatchHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, WatchHistoryId> {

    @Query("SELECT wh FROM WatchHistory wh JOIN FETCH wh.video "
            + "WHERE wh.id.userId = :userId AND wh.completed = false "
            + "ORDER BY wh.lastWatchedAt DESC")
    List<WatchHistory> findContinueWatching(@Param("userId") UUID userId);
}
