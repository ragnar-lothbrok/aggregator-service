package com.demo.aggregator.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.demo.account.model.Account;
import com.demo.aggregator.constants.AggregatorConstants;
import com.exclusively.aggregator.server.UserValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RestController
@RequestMapping("/uaa")
public class UserServiceAggregationController {

	final static Logger logger = LoggerFactory.getLogger(UserServiceAggregationController.class);

	@Value("${user.api.url}")
	private String userAPIURL;

	@Value("${user.login.uri}")
	private String userLoginURI;

	@Value("${url.getUserInformation.headername}")
	private String headerName;

	@Value("${url.social.token.name}")
	private String socialTokenName;

	@Value("${url.social.token.type}")
	private String socialTokenType;

	@Value("${httpclient.timeout}")
	private Integer timeout;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	@Qualifier("restTemplateForUserDetails")
	RestTemplate restTemplate;

	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	@RequestMapping(
		value = "/userDetails",
		method = RequestMethod.GET,
		produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> getUserDetails(@RequestParam(
		required = false) Boolean floorFlag, HttpServletRequest request, HttpServletResponse response) {
		logger.info("Inside getUserDetails...");
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		try {
			responseMap = UserValidator.getUserDetails(request, response, responseMap, userAPIURL, floorFlag == null ? false : floorFlag, timeout,
					redisTemplate, restTemplate);
		} catch (Exception exception) {
			logger.error("Inside getUserDetails Exception occured...", exception);
			responseMap.put(AggregatorConstants.STATUS, Boolean.FALSE);
			responseMap.put(AggregatorConstants.ERROR, exception.getMessage());
		}
		logger.info("Response Map : " + response);
		return responseMap;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(
		value = "/login",
		method = RequestMethod.GET,
		produces = { MediaType.APPLICATION_JSON_VALUE })
	public String userLogin(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Inside login...");
		Map<String, Object> responseMap = new HashMap<String, Object>();
		HttpResponse httpResponse = null;
		HttpClient httpClient = null;
		try {
			String token = null;
			httpClient = UserValidator.getHttpClient(timeout);
			HttpGet getRequest = new HttpGet(userLoginURI);
			if (request.getHeader("Authorization") != null) {
				getRequest.addHeader("Authorization", request.getHeader("Authorization"));
			}
			if (request.getHeader(headerName) != null) {
				getRequest.addHeader(headerName, request.getHeader(headerName));
			}
			if (request.getHeader(socialTokenName) != null) {
				getRequest.addHeader(socialTokenName, request.getHeader(socialTokenName));
			}
			if (request.getHeader(socialTokenType) != null) {
				getRequest.addHeader(socialTokenType, request.getHeader(socialTokenType));
			}
			if (request.getHeader(AggregatorConstants.VISITOR_ID) != null) {
				getRequest.addHeader(AggregatorConstants.VISITOR_ID, request.getHeader(AggregatorConstants.VISITOR_ID));
			}
			if (request.getHeader("GENDER") != null) {
				getRequest.addHeader("GENDER", request.getHeader("GENDER"));
			}
			httpResponse = httpClient.execute(getRequest);
			String line = null;
			Header[] tokens = httpResponse.getHeaders(AggregatorConstants.X_API_TOKEN);
			if (tokens.length != 0) {
				token = tokens[0].getValue();
			}
			InputStream inputStream = httpResponse.getEntity().getContent();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = bufferedReader.readLine()) != null) {
				break;
			}
			logger.info("Response received after login : " + line + " Token : " + token);
			response.setHeader(AggregatorConstants.X_API_TOKEN, token);
			if (line != null) {
				try {
					responseMap = gson.fromJson(line, responseMap.getClass());
					String accountJson = gson.toJson(responseMap.get(AggregatorConstants.USER_DETAILS));
					Account account = gson.fromJson(accountJson, Account.class);
					UserValidator.setAccountDetailInRedis(token, redisTemplate, account, timeout);
				} catch (Exception exception) {
					logger.error("Exception occured while saving into cache: ", exception);
				}
			}
			return line;
		} catch (Exception exception) {
			logger.error("Exception occured userLogin : ", exception);
			responseMap.put(AggregatorConstants.STATUS, Boolean.FALSE);
			responseMap.put(AggregatorConstants.ERROR, exception.getMessage());
		}
		logger.info("Response Map : " + responseMap);
		return gson.toJson(responseMap);
	}

	@RequestMapping(
		value = "/logout",
		produces = { "application/json" },
		method = RequestMethod.POST)
	public @ResponseBody String logout(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			if (request.getHeader(AggregatorConstants.X_API_TOKEN) != null) {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority(AggregatorConstants.ANONYMOUS_ROLE))) {
					new SecurityContextLogoutHandler().logout(request, response, auth);
					redisTemplate.delete(request.getHeader(AggregatorConstants.X_API_TOKEN));
					responseMap.put("status", Boolean.TRUE);
					return gson.toJson(responseMap);
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured logout : ", exception);
		}
		responseMap.put("status", Boolean.TRUE);
		return gson.toJson(responseMap);
	}
}
