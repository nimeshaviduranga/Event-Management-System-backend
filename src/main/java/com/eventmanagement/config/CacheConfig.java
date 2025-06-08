package com.eventmanagement.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Default cache configuration - 10 minutes
        cacheConfigurations.put("events", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Upcoming events - 2 minutes (frequently updated)
        cacheConfigurations.put("upcomingEvents", defaultConfig.entryTtl(Duration.ofMinutes(2)));

        //User events - 5 minutes
        cacheConfigurations.put("userEvents", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Event details - 5 minutes
        cacheConfigurations.put("attendeeCounts", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        // User details - 10 minutes
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}