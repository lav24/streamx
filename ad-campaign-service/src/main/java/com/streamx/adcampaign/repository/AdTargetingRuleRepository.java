package com.streamx.adcampaign.repository;

import com.streamx.adcampaign.entity.AdTargetingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdTargetingRuleRepository extends JpaRepository<AdTargetingRule, UUID> {

    List<AdTargetingRule> findByCampaignId(UUID campaignId);
}
