package com.melodigm.post.util;

import com.melodigm.post.common.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
	public static boolean isNull(String strValue) {
		if (strValue != null && strValue.trim().length() > 0) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isNotNull(String strValue) {
		if (strValue != null && strValue.trim().length() > 0) {
			return true;
		} else {
			return false;
		}
	}

    public static boolean isRegexOnlyEng(String input) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) return true;
        else return false;
    }

    public static ArrayList<int[]> getSpans(String body, char prefix) {
        ArrayList<int[]> spans = new ArrayList<int[]>();

        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        // Check all occurrences
        while (matcher.find()) {
            int[] currentSpan = new int[2];
            currentSpan[0] = matcher.start();
            currentSpan[1] = matcher.end();
            spans.add(currentSpan);
        }

        return  spans;
    }
	
	public static Map<String, String> castQueryToMap(String query) {
	    String[] params = query.split("&");
	    Map<String, String> map = new HashMap<String, String>();
	    for (String param : params) {
	    	String name = param.split("=")[0];
	        String value = param.split("=")[1];
	        map.put(name, value);
	    }
	    return map;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static String castMapToString(Map<String, String> params) {
		StringBuffer strResponse = new StringBuffer();
		try {
			Iterator iterator = params.keySet().iterator();
			
			String keyAttribute = null;
			boolean isFirstState = true;
			while(iterator.hasNext()) {
				if (!isFirstState) {
					strResponse.append("&");
				}
				keyAttribute = (String) iterator.next();
				strResponse.append(URLEncoder.encode(keyAttribute, Constants.SERVICE_CHAR_SET));
				strResponse.append("=");
				strResponse.append(URLEncoder.encode(params.get(keyAttribute), Constants.SERVICE_CHAR_SET));
				
				isFirstState = false;
			}
		} catch (Exception e) {
			strResponse.append("");
		}
		return strResponse.toString();
	}
	
	public static String castCouponFormat(boolean isFMC, String couponNum) {
		String number = "";
		if(isFMC) {
			String cardNumber = couponNum;
			number = cardNumber;
			try { number = cardNumber.substring(0, cardNumber.length()-12) + "  " + cardNumber.substring(cardNumber.length()-12, cardNumber.length()-8) + "  " + cardNumber.substring(cardNumber.length()-8, cardNumber.length()-4) + "  " + cardNumber.substring(cardNumber.length()-4, cardNumber.length()); }
			catch(Exception ex) { number = cardNumber; }
		}
		else {
			String couponNumber = couponNum;
			number = couponNumber;
			if(couponNumber.length() == 18) {
				try { number = couponNumber.substring(0, 4) + "  " + couponNumber.substring(4, 10) + "  " + couponNumber.substring(10, 14) + "  " + couponNumber.substring(14, couponNumber.length()); }
				catch(Exception ex) { number = couponNumber; }
			}
			else if(couponNumber.length() == 20) {
				try { number = couponNumber.substring(0, 2) + " " + couponNumber.substring(2, 6) + " " + couponNumber.substring(6, 12) + " " + couponNumber.substring(12, 16) + " " + couponNumber.substring(16, couponNumber.length()); }
				catch(Exception ex) { number = couponNumber; }
			}
			else if(couponNumber.length() == 21) {
				try { number = couponNumber.substring(0, 3) + " " + couponNumber.substring(3, 7) + " " + couponNumber.substring(7, 13) + " " + couponNumber.substring(13, 17) + " " + couponNumber.substring(17, couponNumber.length()); }
				catch(Exception ex) { number = couponNumber; }
			}
			else {
				try { number = couponNumber.substring(0, 4) + "  " + couponNumber.substring(4, 8) + "  " + couponNumber.substring(8, 12) + "  " + couponNumber.substring(12, couponNumber.length()); }
				catch(Exception ex) { number = couponNumber; }
			}
		}
		return number;
	}

	public static byte[] getBytes(InputStream is) throws IOException {
		int len;
		int size = 1024;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}
}
