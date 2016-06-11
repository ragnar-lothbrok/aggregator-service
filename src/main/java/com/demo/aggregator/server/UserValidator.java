package com.demo.aggregator.server;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.client.RestTemplate;

import com.demo.account.model.Account;
import com.demo.aggregator.constants.AggregatorConstants;
import com.google.gson.Gson;

public class UserValidator {

	final static Logger logger = LoggerFactory.getLogger(UserValidator.class);

	public static final Map<String, Authentication> tokenAuthMap = new HashMap<String, Authentication>();
	public static final Map<String, String> userTokenMap = new HashMap<String, String>();

	public static Map<String, String> validateUser(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Inside validateUser....");
		Map<String, String> result = new HashMap<String, String>();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// If Token is not found in TokenAuthentication Map
		String visitorId = getValueFromHeader(request, AggregatorConstants.VISITOR_ID);
		if (auth == null || auth.getName().equals(AggregatorConstants.ANONYMOUS)) {
			if (StringUtils.isEmpty(visitorId)) {
				visitorId = UUID.randomUUID().toString();
				setInResponseHeader(response, AggregatorConstants.VISITOR_ID, visitorId);
				setInCookie(response, AggregatorConstants.VISITOR_ID, visitorId);
			}
			result.put(AggregatorConstants.ID, visitorId);
			result.put(AggregatorConstants.IS_GUEST, "true");
		} else {
			result.put(AggregatorConstants.ID, auth.getName());
			result.put(AggregatorConstants.IS_GUEST, "false");
			result.put(AggregatorConstants.ACCOUNT_ID, auth.getPrincipal().toString());
			Object details = auth.getDetails();
			if (null != details && details instanceof OAuth2AuthenticationDetails) {
				OAuth2AuthenticationDetails oauth = (OAuth2AuthenticationDetails) details;
				result.put(AggregatorConstants.X_API_TOKEN, oauth.getTokenValue());
			}
			if (StringUtils.isEmpty(visitorId)) {
				visitorId = UUID.randomUUID().toString();
			}
			setInCookie(response, AggregatorConstants.VISITOR_ID, visitorId);
			setInResponseHeader(response, AggregatorConstants.VISITOR_ID, visitorId);
		}
		logger.info("Result Map : " + result);
		return result;

	}

	/**
	 * This method will fetch tokenName value from Request Headers.
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static String getValueFromHeader(HttpServletRequest request, String key) {
		String token = request.getHeader(key);
		if (StringUtils.isEmpty(token)) {
			return getValueFromCookie(request, key);
		} else {
			return token;
		}
	}

	/**
	 * This method will fetch tokenName value from Cookie.
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static String getValueFromCookie(HttpServletRequest request, String key) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(key)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * This method will set key value in Cookie
	 * 
	 * @param response
	 * @param key
	 * @param value
	 */
	public static void setInCookie(HttpServletResponse response, String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setHttpOnly(false);
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
	}

	/**
	 * This method will set key value in Response header
	 * 
	 * @param response
	 * @param key
	 * @param value
	 */
	public static void setInResponseHeader(HttpServletResponse response, String key, String value) {
		response.setHeader(key, value);
	}

	public static Account getAccountFromRedis(String token, RedisTemplate<String, Object> redisTemplate, String accountId) {
		try {
			Object obj = redisTemplate.opsForValue().get(accountId);
			if (obj != null && obj instanceof Account) {
				return ((Account) obj);
			}
		} catch (Exception exception) {
			logger.error("Inside getAccountFromRedis...", exception);
		}
		return null;
	}

	public static void setAccountDetailInRedis(String token, RedisTemplate<String, Object> redisTemplate, Account account, Integer timeOut) {
		try {
			if (account != null)
				redisTemplate.opsForValue().set(account.getId().toString(), account, timeOut, TimeUnit.MINUTES);
		} catch (Exception exception) {
			logger.error("Inside getAccountFromRedis...", exception);
		}
	}

	public static Account getUserDetailsFromCache(HttpServletRequest request, HttpServletResponse response, Map<String, Object> responseMap,
			String userAPIURL, String token, Integer timeOut, RedisTemplate<String, Object> redisTemplate, Map<String, String> validateUser,
			RestTemplate restTemplate) {
		try {
			Account account = getAccountFromRedis(token, redisTemplate, validateUser.get(AggregatorConstants.ACCOUNT_ID));
			if (account != null) {
				return account;
			} else {
				getUserDetails(request, response, responseMap, userAPIURL, true, timeOut, redisTemplate, restTemplate);
				account = getAccountFromRedis(token, redisTemplate, validateUser.get(AggregatorConstants.ACCOUNT_ID));
				if (account != null) {
					return account;
				}
			}
		} catch (Exception exception) {
			logger.info("Inside updateUserDetails Exception occured..." + exception.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getUserDetails(HttpServletRequest request, HttpServletResponse response, Map<String, Object> responseMap,
			String userAPIURL, boolean flags, Integer timeOut, RedisTemplate<String, Object> redisTemplate, RestTemplate restTemplate)
			throws IOException, ClientProtocolException, Exception {
		Map<String, String> validateUser = UserValidator.validateUser(request, response);
		if (validateUser.get(AggregatorConstants.IS_GUEST) != null
				&& Boolean.FALSE.toString().equalsIgnoreCase(validateUser.get(AggregatorConstants.IS_GUEST))) {
			Account account = getAccountFromRedis(validateUser.get(AggregatorConstants.X_API_TOKEN), redisTemplate,
					validateUser.get(AggregatorConstants.ACCOUNT_ID));
			if (account != null) {
				if (!flags)
					responseMap.put(AggregatorConstants.USER_DETAILS,
							new Account(account.getId(), account.getEmailId(), account.getFirstName(), account.getLastName(), account.getGender(),
									account.getSsoProvider(), null, account.getDob(), account.getPhoneNumber(), account.getIsActive(),
									account.getCountry(), account.getCreateDate(), account.getProfileImage(), account.getImsId()));
				else
					responseMap.put(AggregatorConstants.USER_DETAILS, account);
				responseMap.put(AggregatorConstants.STATUS, Boolean.TRUE);
				return responseMap;
			}

			String getUserDetails = userAPIURL + "/uaa/userDetails?access_token=" + validateUser.get(AggregatorConstants.X_API_TOKEN);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
			ResponseEntity<String> serviceResponse = restTemplate
					.exchange(new RequestEntity<String>(headers, HttpMethod.GET, new URI(getUserDetails)), String.class);
			String line = serviceResponse.getBody();
			Gson gson = new Gson();
			if (line != null) {
				responseMap = gson.fromJson(line, responseMap.getClass());
				if (responseMap.get(AggregatorConstants.USER_DETAILS) != null) {
					String accountJson = gson.toJson(responseMap.get(AggregatorConstants.USER_DETAILS));
					account = gson.fromJson(accountJson, Account.class);
					UserValidator.setAccountDetailInRedis(validateUser.get(AggregatorConstants.X_API_TOKEN), redisTemplate, account, timeOut);
				}
			}
			if (!flags && account != null && account.getId() > 0) {
				responseMap.clear();
				responseMap.put("status", Boolean.TRUE);
				responseMap.put(AggregatorConstants.USER_DETAILS,
						new Account(account.getId(), account.getEmailId(), account.getFirstName(), account.getLastName(), account.getGender(),
								account.getSsoProvider(), null, account.getDob(), account.getPhoneNumber(), account.getIsActive(),
								account.getCountry(), account.getCreateDate(), account.getProfileImage(), account.getImsId()));
			}
		} else {
			throw new Exception("User is not logged in.");
		}
		return responseMap;
	}

	public static HttpClient getHttpClient(Integer timeout) {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CredentialsProvider provider = new BasicCredentialsProvider();
		HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).setDefaultRequestConfig(config).build();
		return httpClient;
	}

}
