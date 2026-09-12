package com.streamx.budgetpacing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<String> checkAndSpendScript() {
        return RedisScript.of(new ClassPathResource("scripts/check_and_spend.lua"), String.class);
    }
}
