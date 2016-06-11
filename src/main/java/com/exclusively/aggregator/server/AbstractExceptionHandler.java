package com.exclusively.aggregator.server;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * This class will catch the AccountValidationExcepiton and send that in Json
 * format.
 * 
 * @author raghunandangupta
 *
 */
@ControllerAdvice
public class AbstractExceptionHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionHandler.class);

	@ExceptionHandler(JedisConnectionException.class)
	@ResponseBody
	public ResponseEntity<Map<String, String>> processJedisConnectionException(JedisConnectionException ex) {
		LOGGER.error("Exception occured : " , ex);
		Map<String, String> errorMap = new HashMap<String, String>();
		errorMap.put("error", "Connection could not established with redis.");
		errorMap.put("status", "false");
		return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(SocketTimeoutException.class)
	@ResponseBody
	public ResponseEntity<Map<String, String>> processSocketTimeoutException(SocketTimeoutException ex) {
		LOGGER.error("Exception occured : " , ex);
		Map<String, String> errorMap = new HashMap<String, String>();
		errorMap.put("error", "Connection could not established with redis.");
		errorMap.put("status", "false");
		return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.BAD_REQUEST);
	}

}
