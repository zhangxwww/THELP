package com.example.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {
    public static String parse(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(s.getBytes());
            return new BigInteger(md.digest()).toString(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
