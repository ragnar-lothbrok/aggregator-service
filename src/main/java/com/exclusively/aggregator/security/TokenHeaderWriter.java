package com.exclusively.aggregator.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.header.HeaderWriter;

public class TokenHeaderWriter implements HeaderWriter {

	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			Object details = auth.getDetails();
			OAuth2AuthenticationDetails oauth = (OAuth2AuthenticationDetails) details;
			String token = oauth.getTokenValue();
			if (token != null) {
				response.setHeader("X-API-TOKEN", token);
				response.addCookie(new Cookie("X-API-TOKEN", token));
			}

		}

	}

}
