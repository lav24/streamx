package com.streamx.adcampaign.repository;

import com.streamx.adcampaign.entity.AdCreativeRendition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdCreativeRenditionRepository extends JpaRepository<AdCreativeRendition, UUID> {

    List<AdCreativeRendition> findByCreativeId(UUID creativeId);
}
