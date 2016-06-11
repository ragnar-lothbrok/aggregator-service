package com.demo.security;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import com.demo.constants.AggregatorConstants;

public class EhcacheSecurityContextRepository implements SecurityContextRepository {

	RedisTemplate<String, Object> redisTemplate;
	private ConcurrentHashMap<String, SecurityContext> mapCache;
	private boolean isRedisUse;

	public EhcacheSecurityContextRepository(RedisTemplate<String, Object> redisTemplate, ConcurrentHashMap<String, SecurityContext> mapCache,
			boolean isRedisUse) {
		super();
		this.redisTemplate = redisTemplate;
		this.mapCache = mapCache;
		this.isRedisUse = isRedisUse;
	}

	public boolean containsContext(HttpServletRequest request) {
		String token = this.getToken(request);
		if (token != null) {
			if (isRedisUse)
				return (redisTemplate.opsForValue().get(token) != null ? true : false);
			else
				return mapCache.containsKey(token);
		} else {
			return false;
		}
	}

	// return this.contains("SSSC", getToken(request));

	private String getToken(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String token = request.getHeader("X-API-TOKEN");
		if (StringUtils.isEmpty(token)) {
			return checkCookie(request);
		} else {
			return token;
		}
		// return token;
	}

	private static String checkCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals("JSESSIONID")) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	public SecurityContext loadContext(HttpRequestResponseHolder holder) {
		if (this.containsContext(holder.getRequest())) {
			String token = this.getToken(holder.getRequest());
			if (isRedisUse) {
				return (SecurityContext) redisTemplate.opsForValue().get(token);
			} else {
				return (SecurityContext) mapCache.get(token);
			}
		} else
			return SecurityContextHolder.getContext();
	}

	public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = context.getAuthentication();
		if (auth != null) {
			Object details = auth.getDetails();

			if (details instanceof OAuth2AuthenticationDetails) {
				OAuth2AuthenticationDetails oauth = (OAuth2AuthenticationDetails) details;
				String token = oauth.getTokenValue();
				if (token != null) {
					if (isRedisUse) {
						redisTemplate.opsForValue().set(token, context);
					} else {
						mapCache.put(token, context);
					}
					response.setHeader("X-API-TOKEN", token);
					response.addCookie(new Cookie("X-API-TOKEN", token));
				}
				String jsessionId = checkCookieFromResponse(response);
				if (jsessionId != null) {
					if (isRedisUse) {
						redisTemplate.opsForValue().set(jsessionId, context);
					} else {
						mapCache.put(jsessionId, context);
					}
				}
			}
		}
		String visitorId = UserValidator.getValueFromHeader(request, AggregatorConstants.VISITOR_ID);
		if (visitorId != null) {
			UserValidator.setInResponseHeader(response, AggregatorConstants.VISITOR_ID, visitorId);
			UserValidator.setInCookie(response, AggregatorConstants.VISITOR_ID, visitorId);
		}
	}

	private String checkCookieFromResponse(HttpServletResponse response) {
		if (response.getHeader("SET-COOKIE") != null) {
			String header = new String(response.getHeader("SET-COOKIE").getBytes());
			return header.substring(header.indexOf("=") + 1, (header.indexOf(";") != -1 ? header.indexOf(";") : header.length()));
		}

		return null;
	}

}
