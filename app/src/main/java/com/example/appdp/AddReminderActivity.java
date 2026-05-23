package com.example.appdp;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.Reminder;
import com.example.appdp.ReminderService;
import com.example.appdp.SessionManager;

import java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity {
    private EditText etTitle, etDescription;
    private TextView tvTime;
    private CheckBox cbRepeatDaily;
    private RadioGroup rgTimeType;
    private RadioButton rbSpecificTime, rbMinutesLater;
    private Button btnSelectTime, btnSave, btnCancel;

    private String selectedTime = "09:00";
    private int selectedPlanId = -1;
    private String selectedPlanTitle = "";
    private boolean isRelativeTime = false; // false - конкретное время, true - через N минут
    private int minutesLater = 1; // через сколько минут

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        requestNotificationPermission();
        checkAlarmPermission();

        selectedPlanId = getIntent().getIntExtra("plan_id", -1);
        selectedPlanTitle = getIntent().getStringExtra("plan_title");

        initViews();
        setupListeners();

        if (selectedPlanId != -1 && selectedPlanTitle != null) {
            etTitle.setText("Напоминание: " + selectedPlanTitle);
            etTitle.setEnabled(false);
        }
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }
    }


    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Разрешение на точные будильники")
                        .setMessage("Для работы напоминаний нужно разрешить приложению устанавливать точные будильники")
                        .setPositiveButton("Настройки", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etReminderTitle);
        etDescription = findViewById(R.id.etReminderDescription);
        tvTime = findViewById(R.id.tvReminderTime);
        cbRepeatDaily = findViewById(R.id.cbRepeatDaily);
        rgTimeType = findViewById(R.id.rgTimeType);
        rbSpecificTime = findViewById(R.id.rbSpecificTime);
        rbMinutesLater = findViewById(R.id.rbMinutesLater);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSave = findViewById(R.id.btnSaveReminder);
        btnCancel = findViewById(R.id.btnCancelReminder);

        tvTime.setText("Время: " + selectedTime);

        // По умолчанию выбрано конкретное время
        rbSpecificTime.setChecked(true);
        btnSelectTime.setEnabled(true);
    }

    private void setupListeners() {
        rgTimeType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSpecificTime) {
                isRelativeTime = false;
                btnSelectTime.setEnabled(true);
                cbRepeatDaily.setEnabled(true);
                updateTimeDisplay();
            } else {
                isRelativeTime = true;
                btnSelectTime.setEnabled(true);
                cbRepeatDaily.setEnabled(false);
                cbRepeatDaily.setChecked(false);
                showMinutesPicker();
            }
        });

        btnSelectTime.setOnClickListener(v -> {
            if (isRelativeTime) {
                showMinutesPicker();
            } else {
                showTimePicker();
            }
        });

        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showMinutesPicker() {
        final String[] minutes = {"1 минута", "5 минут", "10 минут", "15 минут", "30 минут", "1 час", "2 часа", "3 часа"};
        final int[] minutesValues = {1, 5, 10, 15, 30, 60, 120, 180};

        new AlertDialog.Builder(this)
                .setTitle("Через сколько напомнить?")
                .setItems(minutes, (dialog, which) -> {
                    minutesLater = minutesValues[which];
                    updateTimeDisplay();
                })
                .show();
    }

    private void showTimePicker() {
        String[] timeParts = selectedTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    updateTimeDisplay();
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        if (isRelativeTime) {
            String text;
            if (minutesLater < 60) {
                text = "Через " + minutesLater + " " + getMinutesText(minutesLater);
            } else {
                int hours = minutesLater / 60;
                text = "Через " + hours + " " + getHoursText(hours);
            }
            tvTime.setText(text);
        } else {
            tvTime.setText("Время: " + selectedTime);
        }
    }

    private String getMinutesText(int minutes) {
        if (minutes == 1) return "минуту";
        if (minutes >= 2 && minutes <= 4) return "минуты";
        return "минут";
    }

    private String getHoursText(int hours) {
        if (hours == 1) return "час";
        if (hours >= 2 && hours <= 4) return "часа";
        return "часов";
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите заголовок напоминания", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        boolean repeatDaily = !isRelativeTime && cbRepeatDaily.isChecked();

        long reminderId;

        if (isRelativeTime) {
            // Относительное время - через N минут
            reminderId = addRelativeReminder(userId, title, description, minutesLater);
        } else {
            // Абсолютное время - в определенный час
            reminderId = dbHelper.addReminder(userId, selectedPlanId, title, description, selectedTime, repeatDaily);
        }

        if (reminderId != -1) {
            if (isRelativeTime) {
                Toast.makeText(this, "Напоминание установлено через " + minutesLater + " " + getMinutesText(minutesLater), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Напоминание установлено на " + selectedTime, Toast.LENGTH_SHORT).show();
            }
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Ошибка при установке напоминания", Toast.LENGTH_SHORT).show();
        }
    }

    private long addRelativeReminder(int userId, String title, String description, int minutesLater) {
        // Вычисляем время через minutesLater минут
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutesLater);

        String timeString = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        // Сохраняем в БД как обычное напоминание (не повторяющееся)
        long id = dbHelper.addReminder(userId, selectedPlanId, title, description, timeString, false);

        if (id != -1) {
            // Получаем созданное напоминание и запускаем его вручную
            Reminder reminder = dbHelper.getReminderById((int) id);
            if (reminder != null) {
                // Устанавливаем точное время срабатывания
                scheduleExactReminder(reminder, calendar.getTimeInMillis());
            }
        }

        return id;
    }

    private void scheduleExactReminder(Reminder reminder, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderService.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_title", reminder.getTitle());
        intent.putExtra("reminder_description", reminder.getDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminder.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }

        android.util.Log.d("AddReminder", "Напоминание запланировано на " + new java.util.Date(triggerTime));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Уведомления разрешены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Для работы напоминаний нужно разрешить уведомления", Toast.LENGTH_LONG).show();
            }
        }
    }

}