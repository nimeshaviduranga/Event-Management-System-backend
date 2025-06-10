package com.eventmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Configuration
    @EnableCaching
    @Profile("!test")
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    static class ProductionCacheConfig {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

            Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
            cacheConfigurations.put("events", defaultConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("upcomingEvents", defaultConfig.entryTtl(Duration.ofMinutes(2)));
            cacheConfigurations.put("userEvents", defaultConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("attendeeCounts", defaultConfig.entryTtl(Duration.ofMinutes(5)));

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .build();
        }
    }

    // Test Cache Manager (for test profile)
    @Configuration
    @Profile("test")
    static class TestCacheConfig {

        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }
}