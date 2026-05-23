package com.example.appdp;


import android.content.Context;
import android.content.SharedPreferences;

public class SubscriptionManager {
    private static final String PREF_NAME = "Subscription";
    private static final String KEY_IS_PREMIUM = "is_premium";
    private static final String KEY_CURRENT_PLAN = "current_plan";
    private static final String KEY_EXPIRY_DATE = "expiry_date";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SubscriptionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public boolean isPremium() {
        return pref.getBoolean(KEY_IS_PREMIUM, false);
    }

    public void setPremium(boolean isPremium) {
        editor.putBoolean(KEY_IS_PREMIUM, isPremium);
        editor.apply();
    }

    public String getCurrentPlan() {
        return pref.getString(KEY_CURRENT_PLAN, "Бесплатный");
    }

    public void setCurrentPlan(String plan) {
        editor.putString(KEY_CURRENT_PLAN, plan);
        editor.apply();
    }

    public String getExpiryDate() {
        return pref.getString(KEY_EXPIRY_DATE, "");
    }

    public void setExpiryDate(String date) {
        editor.putString(KEY_EXPIRY_DATE, date);
        editor.apply();
    }

    public boolean hasAccessToFeature(String feature) {
        if (isPremium()) {
            return true;
        }
        // Бесплатные функции
        switch (feature) {
            case "basic_articles":
            case "basic_profile":
            case "day_counter":
                return true;
            default:
                return false;
        }
    }
}