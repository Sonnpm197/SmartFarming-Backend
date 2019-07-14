package com.son.CapstoneProject.common;

public class StringUtils {

    public static boolean isNullOrEmpty(String s) {
        if (s == null || s.trim().length() == 0) {
            return true;
        }
        return false;
    }

}
