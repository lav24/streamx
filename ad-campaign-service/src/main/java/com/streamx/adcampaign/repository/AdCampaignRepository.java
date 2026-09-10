package com.streamx.adcampaign.repository;

import com.streamx.adcampaign.entity.AdCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdCampaignRepository extends JpaRepository<AdCampaign, UUID> {
}
