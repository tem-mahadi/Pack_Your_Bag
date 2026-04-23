package com.temmahadi.packyourbag.BdApps_Backend;

import android.content.Context;
import android.content.SharedPreferences;

public class SubscriptionManager {
    private static final String PREF_NAME = "SubscriptionPrefs";
    private static final String KEY_IS_SUBSCRIBED = "isSubscribed";
    private static final String KEY_USER_MOBILE = "userMobile";
    private static final String KEY_FIRST_TIME_USER = "firstTimeUser";

    public static boolean isSubscribed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_SUBSCRIBED, false);
    }

    public static void setSubscribed(Context context, boolean isSubscribed) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_SUBSCRIBED, isSubscribed);
        if (isSubscribed) {
            editor.putBoolean(KEY_FIRST_TIME_USER, false);
        }
        editor.apply();
    }

    public static void setUserMobile(Context context, String mobileNumber) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_MOBILE, mobileNumber).apply();
    }

    public static String getUserMobile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_MOBILE, null);
    }

    public static boolean isFirstTimeUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_FIRST_TIME_USER, true);
    }

    public static void clearSubscriptionData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static void completeUserLogin(Context context, String mobileNumber) {
        setUserMobile(context, mobileNumber);
        setSubscribed(context, true);
    }
}
