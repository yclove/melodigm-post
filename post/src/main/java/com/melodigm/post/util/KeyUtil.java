package com.melodigm.post.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class KeyUtil {
    public static String getTokenKey(String securityKey, String accessKey, String currentTimeMillis){
        String accessToken = "";
        String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        SecretKeySpec signingKey = new SecretKeySpec(securityKey.getBytes(), HMAC_SHA256_ALGORITHM);

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal((accessKey + currentTimeMillis).getBytes());
            accessToken = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return accessToken;
    }
}
