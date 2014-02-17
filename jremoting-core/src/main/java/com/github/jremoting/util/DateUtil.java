package com.github.jremoting.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {
	 	private static final String                                     DEFAULT_DATE_TIME_FORMAT     = "yyyy-MM-dd HH:mm:ss";

	    private static final String                                     GENERATE_ID_DATE_TIME_FORMAT = "MMddHHmmss";

	    private static final ThreadLocal<Map<String, SimpleDateFormat>> formatHolder                 = new ThreadLocal<Map<String, SimpleDateFormat>>();

	    /**
	     * 获取当前时间, 格式:yyyy-MM-dd HH:mm:ss
	     * 
	     * @return String
	     */
	    public static String getCurrentTimeString() {
	        return parser(DEFAULT_DATE_TIME_FORMAT, new Date());
	    }

	    /**
	     * 获取当前时间
	     * 
	     * @return Date
	     */
	    public static Date getCurrentTime() {
	        return new Date();
	    }

	    public static String parser(String formatString, Date date) {
	        Map<String, SimpleDateFormat> dateFormatMap = formatHolder.get();
	        if (dateFormatMap == null) {
	            dateFormatMap = new HashMap<String, SimpleDateFormat>();
	            formatHolder.set(dateFormatMap);
	        }
	        SimpleDateFormat dateFormat = dateFormatMap.get(formatString);
	        if (dateFormat == null) {
	            dateFormat = new SimpleDateFormat(formatString);
	            dateFormatMap.put(formatString, dateFormat);
	        }
	        return dateFormat.format(date);
	    }

	    /**
	     * 获取当前时间(生成clientId), 格式:yyyyMMddHHmmss
	     * 
	     * @return String
	     */
	    public static String getGenerateIdTime() {
	        return parser(GENERATE_ID_DATE_TIME_FORMAT, new Date());
	    }

}
