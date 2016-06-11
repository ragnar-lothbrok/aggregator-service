package com.demo.aggregator.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.demo.account.model.Account;
import com.demo.aggregator.constants.AggregatorConstants;
import com.demo.aggregator.server.UserValidator;

/**
 * Client controller, fetches Order info from the microservice
 * 
 */
@RestController
public class AggregationController {

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	final static Logger logger = LoggerFactory.getLogger(AggregationController.class);

	String key = "X-API-TOKEN";

	@Value("${user.api.url}")
	private String userAPIURL;

	@Value("${httpclient.timeout}")
	private Integer timeout;

	@Autowired
	@LoadBalanced
	@Qualifier("loadBalancedRestTemplate")
	RestTemplate restTemplate;

	/**
	 * this method will be intercepted after successful login and set necessary
	 * details in static Map.
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@RequestMapping(
		value = "/",
		produces = { "application/json" })
	public @ResponseBody Map<String, Object> goHome(HttpServletRequest request, HttpServletResponse response)
			throws JsonGenerationException, JsonMappingException, IOException {
		logger.info("Inside /....");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> accountDetails = new HashMap<String, Object>();
		Map<String, String> validateUser = UserValidator.validateUser(request, response);
		Object details = auth.getDetails();
		if (null != details && details instanceof OAuth2AuthenticationDetails) {
			OAuth2AuthenticationDetails oauth = (OAuth2AuthenticationDetails) details;
			try {
				Account account = UserValidator.getUserDetailsFromCache(request, response, new HashMap<String, Object>(), userAPIURL,
						request.getHeader(AggregatorConstants.X_API_TOKEN), timeout, redisTemplate, validateUser, restTemplate);
				if (account != null) {
					accountDetails.put("userDetails", account);
					accountDetails.put("status", Boolean.TRUE);
				} else {
					accountDetails.put("status", Boolean.FALSE);
				}
				response.setHeader(key, oauth.getTokenValue());
			} catch (Exception exception) {
				logger.error("Exception occured : ", exception);
				accountDetails.put("status", Boolean.FALSE);
			}
		}
		return accountDetails;
	}

}
