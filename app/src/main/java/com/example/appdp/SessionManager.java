package com.example.appdp;



import android.content.Context;
import android.content.SharedPreferences;
import com.example.appdp.User;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_ADDICTION_TYPE = "addictionType";
    private static final String KEY_START_DATE = "startDate";
    private static final String KEY_DAYS_COUNTER = "daysCounter";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());

        if (user.getAddictionType() != null) {
            editor.putString(KEY_ADDICTION_TYPE, user.getAddictionType());
        }

        if (user.getStartDate() != null) {
            editor.putString(KEY_START_DATE, user.getStartDate());
        }

        editor.putInt(KEY_DAYS_COUNTER, user.getDaysCounter());
        editor.apply();

        // Логируем для отладки
        android.util.Log.d("SessionManager", "User saved: " + user.getEmail());
        android.util.Log.d("SessionManager", "Addiction type: " + user.getAddictionType());
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        String email = pref.getString(KEY_USER_EMAIL, null);
        android.util.Log.d("SessionManager", "Getting email: " + email);
        return email;
    }

    public String getAddictionType() {
        return pref.getString(KEY_ADDICTION_TYPE, null);
    }

    public String getStartDate() {
        return pref.getString(KEY_START_DATE, null);
    }

    public int getDaysCounter() {
        return pref.getInt(KEY_DAYS_COUNTER, 0);
    }

    public void setAddictionType(String addictionType) {
        editor.putString(KEY_ADDICTION_TYPE, addictionType);
        editor.apply();
    }

    public void setStartDate(String startDate) {
        editor.putString(KEY_START_DATE, startDate);
        editor.apply();
    }

    public void setDaysCounter(int daysCounter) {
        editor.putInt(KEY_DAYS_COUNTER, daysCounter);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
        android.util.Log.d("SessionManager", "User logged out, session cleared");
    }
}