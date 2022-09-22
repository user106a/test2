package com.pflm.common.utils;

import org.apache.commons.codec.binary.Hex;

import java.util.Base64;

public class Transform
{

    public static String trans(String is)
    {

        char c = NUtils.getRandomKey("1").toUpperCase().charAt(0);
        String os = "";
        int len = is.length();
        for (int i = 0; i < len; i++)
        {
            os = os + Character.toString((char) (is.charAt(i) ^ c));
        }

        return os;
    }
}