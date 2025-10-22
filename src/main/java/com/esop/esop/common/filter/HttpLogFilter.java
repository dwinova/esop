/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(FilterOrder.HTTP_LOG_FILTER)
@Slf4j
public class HttpLogFilter implements Filter {
	
	@Override
	public void doFilter(
			final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		final ContentCachingRequestWrapper cachedRequest =
				new ContentCachingRequestWrapper((HttpServletRequest) request);
		final ContentCachingResponseWrapper cachedResponse =
				new ContentCachingResponseWrapper((HttpServletResponse) response);
		chain.doFilter(cachedRequest, cachedResponse);
		HttpLogFilter.logRequest(cachedRequest);
		HttpLogFilter.logResponse(cachedRequest, cachedResponse);
	}
	
	static void logRequest(final ContentCachingRequestWrapper request) {
		final String method = request.getMethod();
		final String requestURI = request.getRequestURI();
		final String queryString = request.getQueryString();
		final String formattedQueryString = StringUtils.hasText(queryString) ? "?" + queryString : "";
		String requestBody = HttpLogFilter.getRequestBody(request);
		if (requestURI.contains("api-docs") || requestURI.contains("swagger")) {
			return;
		}
		if (requestBody.isEmpty()) {
			log.info("RCV | {} {}{}", method, requestURI, formattedQueryString);
		} else {
			requestBody = requestBody.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"*****\"");
			requestBody =
					requestBody.replaceAll("(?i)\"access_token\"\\s*:\\s*\"[^\"]*\"", "\"access_token\":\"*****\"");
			requestBody =
					requestBody.replaceAll("(?i)\"refresh_token\"\\s*:\\s*\"[^\"]*\"", "\"refresh_token\":\"*****\"");
			log.info("RCV | {} {}{} | body = {}", method, requestURI, formattedQueryString, requestBody);
		}
	}
	
	static void logResponse(
			final ContentCachingRequestWrapper request, final ContentCachingResponseWrapper response) {
		final String method = request.getMethod();
		final String requestURI = request.getRequestURI();
		final String queryString = request.getQueryString();
		final String formattedQueryString = StringUtils.hasText(queryString) ? "?" + queryString : "";
		final int status = response.getStatus();
		String responseBody = HttpLogFilter.getResponseBody(response);
		if (requestURI.contains("api-docs") || requestURI.contains("swagger")) {
			return;
		}
		if (status < 500) {
			if (responseBody.isEmpty()) {
				log.info("SNT | {} {}{} | {}", method, requestURI, formattedQueryString, status);
			} else {
				responseBody = responseBody.replaceAll("(?i)\"access_token\"\\s*:\\s*\"[^\"]*\"",
						"\"access_token\":\"*****\"");
				responseBody = responseBody.replaceAll("(?i)\"refresh_token\"\\s*:\\s*\"[^\"]*\"",
						"\"refresh_token\":\"*****\"");
				log.info(
						"SNT | {} {}{} | {} | body = {}",
						method,
						requestURI,
						formattedQueryString,
						status,
						responseBody);
			}
		} else {
			if (responseBody.isEmpty()) {
				log.error("SNT | {} {}{} | {}", method, requestURI, formattedQueryString, status);
			} else {
				responseBody = responseBody.replaceAll("(?i)\"access_token\"\\s*:\\s*\"[^\"]*\"",
						"\"access_token\":\"*****\"");
				responseBody = responseBody.replaceAll("(?i)\"refresh_token\"\\s*:\\s*\"[^\"]*\"",
						"\"refresh_token\":\"*****\"");
				log.error(
						"SNT | {} {}{} | {} | body = {}",
						method,
						requestURI,
						formattedQueryString,
						status,
						responseBody);
			}
		}
	}
	
	@NonNull
	static String getRequestBody(final ContentCachingRequestWrapper request) {
		try (final InputStream inputStream =
				new ByteArrayInputStream(request.getContentAsByteArray())) {
			return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
		return "";
	}
	
	@NonNull
	static String getResponseBody(final ContentCachingResponseWrapper response) {
		try (final InputStream inputStream = response.getContentInputStream()) {
			final String responseBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			response.copyBodyToResponse();
			return responseBody;
		} catch (IOException ignored) {
		}
		return "";
	}
}
