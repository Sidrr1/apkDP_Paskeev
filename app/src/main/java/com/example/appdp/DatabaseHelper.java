package com.example.appdp;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.appdp.Article;
import com.example.appdp.Plan;
import com.example.appdp.User;
import com.example.appdp.UserPlan;
import com.example.appdp.ReminderService;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DependencyHelper.db";
    private static final int DATABASE_VERSION = 2; // Увеличиваем версию для обновления
    private static DatabaseHelper instance;
    private static final String TAG = "DatabaseHelper";
    private Context context;
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + DatabaseContract.UsersEntry.TABLE_NAME + " (" +
                DatabaseContract.UsersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.UsersEntry.COLUMN_EMAIL + " TEXT UNIQUE, " +
                DatabaseContract.UsersEntry.COLUMN_PASSWORD_HASH + " TEXT, " +
                DatabaseContract.UsersEntry.COLUMN_ADDICTION_TYPE + " TEXT, " +
                DatabaseContract.UsersEntry.COLUMN_START_DATE + " TEXT, " +
                DatabaseContract.UsersEntry.COLUMN_DAYS_COUNTER + " INTEGER DEFAULT 0, " +
                DatabaseContract.UsersEntry.COLUMN_LAST_CLICK_DATE + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы статей
        String CREATE_ARTICLES_TABLE = "CREATE TABLE " + DatabaseContract.ArticlesEntry.TABLE_NAME + " (" +
                DatabaseContract.ArticlesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.ArticlesEntry.COLUMN_ADDICTION_TYPE + " TEXT, " +
                DatabaseContract.ArticlesEntry.COLUMN_TITLE + " TEXT, " +
                DatabaseContract.ArticlesEntry.COLUMN_DESCRIPTION + " TEXT, " +
                DatabaseContract.ArticlesEntry.COLUMN_CONTENT + " TEXT)";
        db.execSQL(CREATE_ARTICLES_TABLE);

        // Создание таблицы планов (обновленная)
        String CREATE_PLANS_TABLE = "CREATE TABLE " + DatabaseContract.PlansEntry.TABLE_NAME + " (" +
                DatabaseContract.PlansEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.PlansEntry.COLUMN_ADDICTION_TYPE + " TEXT, " +
                DatabaseContract.PlansEntry.COLUMN_TITLE + " TEXT, " +
                DatabaseContract.PlansEntry.COLUMN_DESCRIPTION + " TEXT, " +
                DatabaseContract.PlansEntry.COLUMN_DURATION_DAYS + " INTEGER, " +
                DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM + " INTEGER DEFAULT 0, " +
                DatabaseContract.PlansEntry.COLUMN_USER_ID + " INTEGER DEFAULT -1)";
        db.execSQL(CREATE_PLANS_TABLE);

        // Создание таблицы пользовательских планов
        String CREATE_USER_PLANS_TABLE = "CREATE TABLE " + DatabaseContract.UserPlansEntry.TABLE_NAME + " (" +
                DatabaseContract.UserPlansEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.UserPlansEntry.COLUMN_USER_ID + " INTEGER, " +
                DatabaseContract.UserPlansEntry.COLUMN_PLAN_ID + " INTEGER, " +
                DatabaseContract.UserPlansEntry.COLUMN_STATUS + " TEXT DEFAULT 'active', " +
                DatabaseContract.UserPlansEntry.COLUMN_START_DATE + " TEXT, " +
                DatabaseContract.UserPlansEntry.COLUMN_COMPLETED_DATE + " TEXT, " +
                "FOREIGN KEY(" + DatabaseContract.UserPlansEntry.COLUMN_USER_ID + ") REFERENCES " +
                DatabaseContract.UsersEntry.TABLE_NAME + "(" + DatabaseContract.UsersEntry._ID + "), " +
                "FOREIGN KEY(" + DatabaseContract.UserPlansEntry.COLUMN_PLAN_ID + ") REFERENCES " +
                DatabaseContract.PlansEntry.TABLE_NAME + "(" + DatabaseContract.PlansEntry._ID + "))";
        db.execSQL(CREATE_USER_PLANS_TABLE);

        // Создание таблицы напоминаний
        String CREATE_REMINDERS_TABLE = "CREATE TABLE " + DatabaseContract.RemindersEntry.TABLE_NAME + " (" +
                DatabaseContract.RemindersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.RemindersEntry.COLUMN_USER_ID + " INTEGER, " +
                DatabaseContract.RemindersEntry.COLUMN_PLAN_ID + " INTEGER, " +
                DatabaseContract.RemindersEntry.COLUMN_TITLE + " TEXT, " +
                DatabaseContract.RemindersEntry.COLUMN_DESCRIPTION + " TEXT, " +
                DatabaseContract.RemindersEntry.COLUMN_REMINDER_TIME + " TEXT, " +
                DatabaseContract.RemindersEntry.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                DatabaseContract.RemindersEntry.COLUMN_REPEAT_DAILY + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + DatabaseContract.RemindersEntry.COLUMN_USER_ID + ") REFERENCES " +
                DatabaseContract.UsersEntry.TABLE_NAME + "(" + DatabaseContract.UsersEntry._ID + "), " +
                "FOREIGN KEY(" + DatabaseContract.RemindersEntry.COLUMN_PLAN_ID + ") REFERENCES " +
                DatabaseContract.PlansEntry.TABLE_NAME + "(" + DatabaseContract.PlansEntry._ID + "))";
        db.execSQL(CREATE_REMINDERS_TABLE);

        // Заполнение начальными данными
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.RemindersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.UserPlansEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.PlansEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.UsersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ArticlesEntry.TABLE_NAME);
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        try {
            // Статьи для курения
            insertArticle(db, "smoking", "Вред курения для организма",
                    "Как курение влияет на здоровье",
                    "Курение вызывает рак легких, сердечно-сосудистые заболевания, преждевременное старение кожи и множество других проблем со здоровьем. Никотин вызывает сильную зависимость, а смолы оседают в легких, вызывая хронические заболевания.");

            insertArticle(db, "smoking", "Как бросить курить",
                    "Эффективные способы отказа от курения",
                    "Существует несколько методов бросить курить: постепенное снижение количества сигарет, использование никотинзаместительной терапии, психологическая поддержка. Главное - найти свою мотивацию и не сдаваться при срывах.");

            // Статьи для алкоголя
            insertArticle(db, "alcohol", "Влияние алкоголя на организм",
                    "Как алкоголь разрушает здоровье",
                    "Алкоголь негативно влияет на печень, мозг и сердечно-сосудистую систему. Регулярное употребление приводит к циррозу печени, деменции, сердечной недостаточности и другим серьезным заболеваниям.");

            insertArticle(db, "alcohol", "Путь к трезвости",
                    "Как отказаться от алкоголя",
                    "Отказ от алкоголя требует силы воли и поддержки близких. Начните с малого: сократите количество выпиваемого, найдите альтернативные способы расслабления, обратитесь к специалисту при необходимости.");

            // Статьи для электронных сигарет
            insertArticle(db, "vaping", "Правда об электронных сигаретах",
                    "Мифы и реальность вейпинга",
                    "Электронные сигареты содержат никотин и другие вредные вещества. Они не являются безопасной альтернативой курению и могут вызывать серьезные заболевания легких, известные как EVALI.");

            // Статьи для энергетиков
            insertArticle(db, "energy", "Опасность энергетических напитков",
                    "Влияние энергетиков на сердце",
                    "Энергетические напитки вызывают тахикардию, повышают артериальное давление, нарушают сон и могут привести к серьезным сердечным заболеваниям. Особенно опасны при употреблении с алкоголем.");

            // Планы для курения
            insertPlan(db, "smoking", "Снижение на 50% за 2 недели",
                    "Уменьшите количество выкуриваемых сигарет вдвое за 14 дней", 14, 0, -1);
            insertPlan(db, "smoking", "Полный отказ за месяц",
                    "Постепенный отказ от курения в течение 30 дней", 30, 0, -1);

            // Планы для алкоголя
            insertPlan(db, "alcohol", "Снижение потребления",
                    "Уменьшите количество алкоголя на 30% за 3 недели", 21, 0, -1);
            insertPlan(db, "alcohol", "Трезвый месяц",
                    "Полный отказ от алкоголя на 30 дней", 30, 0, -1);

            // Планы для вейпинга
            insertPlan(db, "vaping", "Отказ от никотина",
                    "Постепенное снижение крепости жидкости до нуля", 28, 0, -1);
            insertPlan(db, "vaping", "Полный отказ от вейпа",
                    "Полный отказ от использования электронных сигарет за 14 дней", 14, 0, -1);

            // Планы для энергетиков
            insertPlan(db, "energy", "Замена на воду",
                    "Заменяйте каждый второй энергетик водой в течение 2 недель", 14, 0, -1);
            insertPlan(db, "energy", "Полный отказ",
                    "Полный отказ от энергетических напитков за 30 дней", 30, 0, -1);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting initial data", e);
        }
    }

    private void insertArticle(SQLiteDatabase db, String type, String title, String desc, String content) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ArticlesEntry.COLUMN_ADDICTION_TYPE, type);
        values.put(DatabaseContract.ArticlesEntry.COLUMN_TITLE, title);
        values.put(DatabaseContract.ArticlesEntry.COLUMN_DESCRIPTION, desc);
        values.put(DatabaseContract.ArticlesEntry.COLUMN_CONTENT, content);
        db.insert(DatabaseContract.ArticlesEntry.TABLE_NAME, null, values);
    }

    private void insertPlan(SQLiteDatabase db, String type, String title, String desc, int duration, int isCustom, int userId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PlansEntry.COLUMN_ADDICTION_TYPE, type);
        values.put(DatabaseContract.PlansEntry.COLUMN_TITLE, title);
        values.put(DatabaseContract.PlansEntry.COLUMN_DESCRIPTION, desc);
        values.put(DatabaseContract.PlansEntry.COLUMN_DURATION_DAYS, duration);
        values.put(DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM, isCustom);
        values.put(DatabaseContract.PlansEntry.COLUMN_USER_ID, userId);
        db.insert(DatabaseContract.PlansEntry.TABLE_NAME, null, values);
    }

    // Методы для работы с планами
    public List<Plan> getPlansByAddictionType(String addictionType) {
        List<Plan> plans = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.PlansEntry.TABLE_NAME, null,
                    DatabaseContract.PlansEntry.COLUMN_ADDICTION_TYPE + "=?",
                    new String[]{addictionType}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Plan plan = new Plan();
                    plan.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry._ID)));
                    plan.setAddictionType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_ADDICTION_TYPE)));
                    plan.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_TITLE)));
                    plan.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_DESCRIPTION)));
                    plan.setDurationDays(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_DURATION_DAYS)));
                    plan.setCustom(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM)) == 1);
                    plan.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_USER_ID)));
                    plans.add(plan);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting plans", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return plans;
    }

    // Добавление пользовательского плана
    public long addCustomPlan(int userId, String addictionType, String title, String description, int durationDays) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PlansEntry.COLUMN_ADDICTION_TYPE, addictionType);
        values.put(DatabaseContract.PlansEntry.COLUMN_TITLE, title);
        values.put(DatabaseContract.PlansEntry.COLUMN_DESCRIPTION, description);
        values.put(DatabaseContract.PlansEntry.COLUMN_DURATION_DAYS, durationDays);
        values.put(DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM, 1);
        values.put(DatabaseContract.PlansEntry.COLUMN_USER_ID, userId);
        return db.insert(DatabaseContract.PlansEntry.TABLE_NAME, null, values);
    }

    // Удаление пользовательского плана
    public boolean deleteCustomPlan(int planId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(DatabaseContract.PlansEntry.TABLE_NAME,
                DatabaseContract.PlansEntry._ID + "=? AND " +
                        DatabaseContract.PlansEntry.COLUMN_USER_ID + "=? AND " +
                        DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM + "=1",
                new String[]{String.valueOf(planId), String.valueOf(userId)});
        return rows > 0;
    }

    // Методы для работы с пользовательскими планами
    public boolean assignPlanToUser(int userId, int planId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserPlansEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.UserPlansEntry.COLUMN_PLAN_ID, planId);
        values.put(DatabaseContract.UserPlansEntry.COLUMN_STATUS, "active");
        values.put(DatabaseContract.UserPlansEntry.COLUMN_START_DATE, getCurrentDate());

        long result = db.insert(DatabaseContract.UserPlansEntry.TABLE_NAME, null, values);
        return result != -1;
    }

    public List<UserPlan> getUserPlans(int userId) {
        List<UserPlan> userPlans = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT up.*, p." + DatabaseContract.PlansEntry.COLUMN_TITLE +
                    ", p." + DatabaseContract.PlansEntry.COLUMN_DESCRIPTION +
                    ", p." + DatabaseContract.PlansEntry.COLUMN_DURATION_DAYS +
                    ", p." + DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM +
                    " FROM " + DatabaseContract.UserPlansEntry.TABLE_NAME + " up" +
                    " JOIN " + DatabaseContract.PlansEntry.TABLE_NAME + " p ON up." +
                    DatabaseContract.UserPlansEntry.COLUMN_PLAN_ID + " = p." + DatabaseContract.PlansEntry._ID +
                    " WHERE up." + DatabaseContract.UserPlansEntry.COLUMN_USER_ID + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                do {
                    UserPlan userPlan = new UserPlan();
                    userPlan.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.UserPlansEntry._ID)));
                    userPlan.setUserId(userId);
                    userPlan.setPlanId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.UserPlansEntry.COLUMN_PLAN_ID)));
                    userPlan.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserPlansEntry.COLUMN_STATUS)));
                    userPlan.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserPlansEntry.COLUMN_START_DATE)));
                    userPlan.setCompletedDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserPlansEntry.COLUMN_COMPLETED_DATE)));

                    Plan plan = new Plan();
                    plan.setId(userPlan.getPlanId());
                    plan.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_TITLE)));
                    plan.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_DESCRIPTION)));
                    plan.setDurationDays(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_DURATION_DAYS)));
                    plan.setCustom(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PlansEntry.COLUMN_IS_CUSTOM)) == 1);
                    userPlan.setPlan(plan);

                    userPlans.add(userPlan);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user plans", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return userPlans;
    }

    public boolean completeUserPlan(int userPlanId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserPlansEntry.COLUMN_STATUS, "completed");
        values.put(DatabaseContract.UserPlansEntry.COLUMN_COMPLETED_DATE, getCurrentDate());

        int rows = db.update(DatabaseContract.UserPlansEntry.TABLE_NAME, values,
                DatabaseContract.UserPlansEntry._ID + "=?",
                new String[]{String.valueOf(userPlanId)});
        return rows > 0;
    }

    // Методы для работы с напоминаниями

    // Добавьте эти методы в DatabaseHelper.java

    public long addReminder(int userId, int planId, String title, String description, String reminderTime, boolean repeatDaily) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RemindersEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.RemindersEntry.COLUMN_PLAN_ID, planId);
        values.put(DatabaseContract.RemindersEntry.COLUMN_TITLE, title);
        values.put(DatabaseContract.RemindersEntry.COLUMN_DESCRIPTION, description);
        values.put(DatabaseContract.RemindersEntry.COLUMN_REMINDER_TIME, reminderTime);
        values.put(DatabaseContract.RemindersEntry.COLUMN_IS_ACTIVE, 1);
        values.put(DatabaseContract.RemindersEntry.COLUMN_REPEAT_DAILY, repeatDaily ? 1 : 0);
        long id = db.insert(DatabaseContract.RemindersEntry.TABLE_NAME, null, values);

        // Если напоминание добавлено успешно, запускаем его
        if (id != -1) {
            Reminder reminder = getReminderById((int) id);
            if (reminder != null) {
                try {
                    ReminderService.scheduleReminder(context, reminder);
                    Log.d(TAG, "Напоминание запланировано при добавлении");
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при планировании напоминания", e);
                }
            }
        }

        return id;
    }

    public boolean deleteReminder(int reminderId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Отменяем напоминание перед удалением
            ReminderService.cancelReminder(context, reminderId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отмене напоминания", e);
        }

        int rows = db.delete(DatabaseContract.RemindersEntry.TABLE_NAME,
                DatabaseContract.RemindersEntry._ID + "=?",
                new String[]{String.valueOf(reminderId)});

        if (rows > 0) {
            Log.d(TAG, "Напоминание удалено. ID: " + reminderId);
        }

        return rows > 0;
    }

    public Reminder getReminderById(int reminderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.RemindersEntry.TABLE_NAME, null,
                    DatabaseContract.RemindersEntry._ID + "=?",
                    new String[]{String.valueOf(reminderId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                Reminder reminder = new Reminder();
                reminder.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry._ID)));
                reminder.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_USER_ID)));
                reminder.setPlanId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_PLAN_ID)));
                reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_TITLE)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_DESCRIPTION)));
                reminder.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_REMINDER_TIME)));
                reminder.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_IS_ACTIVE)) == 1);
                reminder.setRepeatDaily(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_REPEAT_DAILY)) == 1);
                return reminder;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting reminder by id", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }



    public boolean toggleReminderActive(int reminderId, boolean active) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RemindersEntry.COLUMN_IS_ACTIVE, active ? 1 : 0);
        int rows = db.update(DatabaseContract.RemindersEntry.TABLE_NAME, values,
                DatabaseContract.RemindersEntry._ID + "=?",
                new String[]{String.valueOf(reminderId)});

        // Если напоминание деактивировано, отменяем его
        if (!active) {
            ReminderService.cancelReminder(context, reminderId);
        } else {
            // Если активировано, перезапускаем
            Reminder reminder = getReminderById(reminderId);
            if (reminder != null) {
                ReminderService.scheduleReminder(context, reminder);
            }
        }

        return rows > 0;
    }

    public List<Reminder> getRemindersByUser(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.RemindersEntry.TABLE_NAME, null,
                    DatabaseContract.RemindersEntry.COLUMN_USER_ID + "=? AND " +
                            DatabaseContract.RemindersEntry.COLUMN_IS_ACTIVE + "=1",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Reminder reminder = new Reminder();
                    reminder.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry._ID)));
                    reminder.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_USER_ID)));
                    reminder.setPlanId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_PLAN_ID)));
                    reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_TITLE)));
                    reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_DESCRIPTION)));
                    reminder.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_REMINDER_TIME)));
                    reminder.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_IS_ACTIVE)) == 1);
                    reminder.setRepeatDaily(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RemindersEntry.COLUMN_REPEAT_DAILY)) == 1);
                    reminders.add(reminder);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting reminders", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return reminders;
    }

    // Методы для работы с пользователями
    public long registerUser(String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UsersEntry.COLUMN_EMAIL, email);
        values.put(DatabaseContract.UsersEntry.COLUMN_PASSWORD_HASH, passwordHash);
        return db.insert(DatabaseContract.UsersEntry.TABLE_NAME, null, values);
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseContract.UsersEntry.TABLE_NAME, null,
                    DatabaseContract.UsersEntry.COLUMN_EMAIL + "=?",
                    new String[]{email}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                User user = new User();

                // Получаем ID
                int idIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry._ID);
                if (idIndex != -1) {
                    user.setId(cursor.getInt(idIndex));
                }

                // Получаем Email
                int emailIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_EMAIL);
                if (emailIndex != -1) {
                    user.setEmail(cursor.getString(emailIndex));
                }

                // Получаем пароль
                int passwordIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_PASSWORD_HASH);
                if (passwordIndex != -1) {
                    user.setPasswordHash(cursor.getString(passwordIndex));
                }

                // Получаем тип зависимости
                int addictionIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_ADDICTION_TYPE);
                if (addictionIndex != -1) {
                    user.setAddictionType(cursor.getString(addictionIndex));
                    android.util.Log.d("DatabaseHelper", "Addiction type from DB: " + cursor.getString(addictionIndex));
                }

                // Получаем дату начала
                int startDateIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_START_DATE);
                if (startDateIndex != -1) {
                    user.setStartDate(cursor.getString(startDateIndex));
                    android.util.Log.d("DatabaseHelper", "Start date from DB: " + cursor.getString(startDateIndex));
                }

                // Получаем счетчик дней
                int daysIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_DAYS_COUNTER);
                if (daysIndex != -1) {
                    user.setDaysCounter(cursor.getInt(daysIndex));
                    android.util.Log.d("DatabaseHelper", "Days counter from DB: " + cursor.getInt(daysIndex));
                }

                // Получаем дату последнего клика
                int lastClickIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_LAST_CLICK_DATE);
                if (lastClickIndex != -1) {
                    user.setLastClickDate(cursor.getString(lastClickIndex));
                }

                cursor.close();
                return user;
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error getting user by email", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public boolean updateUserAddiction(int userId, String addictionType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UsersEntry.COLUMN_ADDICTION_TYPE, addictionType);
        values.put(DatabaseContract.UsersEntry.COLUMN_START_DATE, getCurrentDate());
        values.put(DatabaseContract.UsersEntry.COLUMN_DAYS_COUNTER, 0);

        int rows = db.update(DatabaseContract.UsersEntry.TABLE_NAME, values,
                DatabaseContract.UsersEntry._ID + "=?",
                new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean incrementDayCounter(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.UsersEntry.TABLE_NAME,
                    new String[]{DatabaseContract.UsersEntry.COLUMN_LAST_CLICK_DATE,
                            DatabaseContract.UsersEntry.COLUMN_DAYS_COUNTER},
                    DatabaseContract.UsersEntry._ID + "=?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int lastClickIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_LAST_CLICK_DATE);
                int daysCounterIndex = cursor.getColumnIndex(DatabaseContract.UsersEntry.COLUMN_DAYS_COUNTER);

                String lastClick = lastClickIndex != -1 ? cursor.getString(lastClickIndex) : null;
                int currentDays = daysCounterIndex != -1 ? cursor.getInt(daysCounterIndex) : 0;
                cursor.close();

                String today = getCurrentDate();
                if (today.equals(lastClick)) {
                    return false;
                }

                ContentValues values = new ContentValues();
                values.put(DatabaseContract.UsersEntry.COLUMN_DAYS_COUNTER, currentDays + 1);
                values.put(DatabaseContract.UsersEntry.COLUMN_LAST_CLICK_DATE, today);

                db.update(DatabaseContract.UsersEntry.TABLE_NAME, values,
                        DatabaseContract.UsersEntry._ID + "=?",
                        new String[]{String.valueOf(userId)});
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error incrementing day counter", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    // Методы для работы со статьями
    public List<Article> getArticlesByAddictionType(String addictionType) {
        List<Article> articles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.ArticlesEntry.TABLE_NAME, null,
                    DatabaseContract.ArticlesEntry.COLUMN_ADDICTION_TYPE + "=?",
                    new String[]{addictionType}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Article article = new Article();
                    article.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry._ID)));
                    article.setAddictionType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_ADDICTION_TYPE)));
                    article.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_TITLE)));
                    article.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_DESCRIPTION)));
                    article.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_CONTENT)));
                    articles.add(article);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting articles", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return articles;
    }

    public Article getArticleById(int articleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.ArticlesEntry.TABLE_NAME, null,
                    DatabaseContract.ArticlesEntry._ID + "=?",
                    new String[]{String.valueOf(articleId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                Article article = new Article();
                article.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry._ID)));
                article.setAddictionType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_ADDICTION_TYPE)));
                article.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_TITLE)));
                article.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_DESCRIPTION)));
                article.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ArticlesEntry.COLUMN_CONTENT)));
                return article;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting article by id", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }
}