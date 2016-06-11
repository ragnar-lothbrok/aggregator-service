package com.demo.security;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@EnableOAuth2Sso
@EnableCaching
@EnableGlobalMethodSecurity(
	prePostEnabled = true)
public class LoginConfigurer extends WebSecurityConfigurerAdapter {

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Value("${aggregator.redis:true}")
	private boolean isRedisUse;

	@Override
	public void configure(HttpSecurity http) throws Exception {

		// http.logout().and().antMatcher("/**").authorizeRequests()
		// .antMatchers("/index.html", "/home.html", "/", "/login").permitAll()
		// .anyRequest().authenticated().and().csrf().
		// .csrfTokenRepository(csrfTokenRepository()).and()
		// .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);

		http.antMatcher("/**").authorizeRequests().anyRequest().hasAnyRole("AUTHENTICATED_USER", "ANONYMOUS").and()
				// .antMatcher("/cart/**").authorizeRequests().anyRequest()
				// .hasAnyRole("AUTHENTICATED_USER","ANONYMOUS").and()
				// .antMatcher("/cart/user/login/**").authorizeRequests().anyRequest().hasRole("AUTHENTICATED_USER").and()
				// .csrfTokenRepository(csrfTokenRepository()).and()
				// .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class)
				.csrf().disable()
				// .rememberMe().rememberMeCookieName("REMEMBER_ME_TOKEN")
				// .and()

				.sessionManagement().sessionFixation().migrateSession().and().securityContext()
				.securityContextRepository(ehcacheSecurityContextRepository());
		// .and().headers().addHeaderWriter(new TokenHeaderWriter());
		http.headers().frameOptions().sameOrigin().disable();
	}

	@SuppressWarnings("unused")
	private Filter csrfHeaderFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
					throws ServletException, IOException {
				CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
				if (csrf != null) {
					Cookie cookie = new Cookie("XSRF-TOKEN", csrf.getToken());
					cookie.setPath("/");
					response.addCookie(cookie);
				}
				filterChain.doFilter(request, response);
			}
		};
	}

	@SuppressWarnings("unused")
	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}

	@Bean
	public SecurityContextRepository ehcacheSecurityContextRepository() {
		return new EhcacheSecurityContextRepository(redisTemplate, new ConcurrentHashMap<String, SecurityContext>(), isRedisUse);
	}

	@Controller
	public static class LoginErrors {

	}

}
