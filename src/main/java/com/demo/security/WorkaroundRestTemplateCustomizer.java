package com.demo.security;

import java.util.ArrayList;

import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkaroundRestTemplateCustomizer implements UserInfoRestTemplateCustomizer {

	public void customize(OAuth2RestTemplate template) {
		template.setInterceptors(new ArrayList<ClientHttpRequestInterceptor>(template.getInterceptors()));
	}

}
