package com.streamx.adcampaign.repository;

import com.streamx.adcampaign.entity.AdCreative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdCreativeRepository extends JpaRepository<AdCreative, UUID> {

    List<AdCreative> findByCampaignId(UUID campaignId);
}
