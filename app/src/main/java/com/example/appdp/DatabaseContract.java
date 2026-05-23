package com.example.appdp;


import android.provider.BaseColumns;


import android.provider.BaseColumns;

public final class DatabaseContract {
    private DatabaseContract() {}

    public static class UsersEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_ADDICTION_TYPE = "addiction_type";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_DAYS_COUNTER = "days_counter";
        public static final String COLUMN_LAST_CLICK_DATE = "last_click_date";
    }

    public static class ArticlesEntry implements BaseColumns {
        public static final String TABLE_NAME = "articles";
        public static final String COLUMN_ADDICTION_TYPE = "addiction_type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CONTENT = "content";
    }

    public static class PlansEntry implements BaseColumns {
        public static final String TABLE_NAME = "plans";
        public static final String COLUMN_ADDICTION_TYPE = "addiction_type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DURATION_DAYS = "duration_days";
        public static final String COLUMN_IS_CUSTOM = "is_custom"; // 0 - стандартный, 1 - пользовательский
        public static final String COLUMN_USER_ID = "user_id"; // Для пользовательских планов
    }

    public static class UserPlansEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_plans";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PLAN_ID = "plan_id";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_COMPLETED_DATE = "completed_date";
    }

    // Новая таблица для напоминаний
    public static class RemindersEntry implements BaseColumns {
        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PLAN_ID = "plan_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_REPEAT_DAILY = "repeat_daily";
    }
}