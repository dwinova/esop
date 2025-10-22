/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.redis;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableAutoConfiguration(exclude = RedisRepositoriesAutoConfiguration.class)
public class RedisConfig {
	
	@Bean
	public RedisConnectionFactory redisConnectionFactory(
			@Value("${spring.data.redis.url}") final String redisUrl,
			@Value("${spring.data.redis.username}") final String redisUsername,
			@Value("${spring.data.redis.password}") final String redisPassword,
			@Value("${spring.data.redis.database}") final int database) throws URISyntaxException {
		final URI uri = new URI(redisUrl);
		final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(uri.getHost());
		redisStandaloneConfiguration.setPort(uri.getPort());
		redisStandaloneConfiguration.setUsername(redisUsername);
		redisStandaloneConfiguration.setPassword(redisPassword);
		redisStandaloneConfiguration.setDatabase(database);
		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			final RedisConnectionFactory connectionFactory) {
		final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}
}
