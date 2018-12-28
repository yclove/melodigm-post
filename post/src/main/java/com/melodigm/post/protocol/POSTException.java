package com.melodigm.post.protocol;

public class POSTException extends Exception {
    public static final String INVALID_DATA   = "1";
    public static final String SW_UPDATE_NEEDED = "0100";
    public static final String SW_UPDATE_SUPPORT = "0101";
    public static final String NOT_FOUND_USER = "0111";
    public static final String ROOTING_DETECTED  = "rooting detected";

    String code;
    String message;

    public POSTException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        if(code == SW_UPDATE_NEEDED) {
            return ""+SW_UPDATE_NEEDED;
        }
        if(message.equalsIgnoreCase(SW_UPDATE_NEEDED)) {
            return this.message + ".";
        }
        return this.message;
    }

    public String getCode() {
        return code;
    }
}
