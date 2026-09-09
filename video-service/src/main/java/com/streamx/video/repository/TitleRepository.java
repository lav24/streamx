package com.streamx.video.repository;

import com.streamx.video.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TitleRepository extends JpaRepository<Title, UUID> {
}
