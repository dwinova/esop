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

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public abstract class AbstractRedisRepository<K, V> implements RedisRepository<K, V> {
	
	protected final RedisTemplate<K, V> redisTemplate;
	
	
	@Override
	public V get(final K key) {
		return this.redisTemplate.opsForValue().get(key);
	}
	
	@Override
	public Long getRecordTtl(final K key) {
		return this.redisTemplate.opsForValue().getOperations().getExpire(key);
	}
	
	@Override
	public void set(final K key, final V value) {
		this.redisTemplate.opsForValue().set(key, value);
	}
	
	@Override
	public void set(final K key, final V value, final long timeout) {
		this.redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
	}
	
	@Override
	public void setIfAbsent(final K key, final V value) {
		this.redisTemplate.opsForValue().setIfAbsent(key, value);
	}
	
	@Override
	public void setIfAbsent(final K key, final V value, final long timeout) {
		this.redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
	}
	
	@Override
	public void delete(final K key) {
		this.redisTemplate.delete(key);
	}
}
