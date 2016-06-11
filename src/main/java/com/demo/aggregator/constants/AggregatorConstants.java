package com.demo.aggregator.constants;

import java.text.SimpleDateFormat;

/**
 * This interface will keep all constants
 * 
 * @author raghunandangupta
 *
 */
public interface AggregatorConstants {

	public static final String X_API_TOKEN = "X-API-TOKEN";
	public static final String VISITOR_ID = "visitorId";
	public static String ID = "id";
	public static final String STATUS = "status";
	public static final String ERROR = "error";
	public static final String IS_GUEST = "isGuest";
	public static final String TRUE = "true";
	public static final String USER_DETAILS = "userDetails";
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public static SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final String REGEX = "\\d+";
	public static final String ANONYMOUS = "anonymousUser";
	public static final String ANONYMOUS_ROLE = "ROLE_ANONYMOUS";
	public static final String ACCOUNT_ID = "accountId";
}
