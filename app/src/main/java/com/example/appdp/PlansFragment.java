package com.example.appdp;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.Plan;
import com.example.appdp.UserPlan;
import com.example.appdp.ReminderService;
import com.example.appdp.SessionManager;

import java.util.List;

public class PlansFragment extends Fragment {
    private ListView listViewPlans;
    private TextView tvNoPlans;
    private Button btnAddCustomPlan;
    private Button btnTestReminder;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Plan> availablePlans;
    private List<UserPlan> userPlans;
    private PlansAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plans, container, false);

        dbHelper = DatabaseHelper.getInstance(getActivity());
        sessionManager = new SessionManager(getActivity());

        initViews(view);
        loadData();
        checkPermissions();

        return view;
    }

    private void initViews(View view) {
        listViewPlans = view.findViewById(R.id.listViewPlans);
        tvNoPlans = view.findViewById(R.id.tvNoPlans);
        btnAddCustomPlan = view.findViewById(R.id.btnAddCustomPlan);

        // Создаем кнопку для тестирования
        LinearLayout parentLayout = (LinearLayout) view;
        btnTestReminder = new Button(getActivity());
        btnTestReminder.setText("🔔 ТЕСТ: Напоминание через 10 секунд");
        btnTestReminder.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        btnTestReminder.setTextColor(getResources().getColor(android.R.color.white));
        btnTestReminder.setPadding(16, 12, 16, 12);
        btnTestReminder.setOnClickListener(v -> testReminder());

        // Добавляем кнопку в начало
        parentLayout.addView(btnTestReminder, 0);

        btnAddCustomPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPlanActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void loadData() {
        try {
            int userId = sessionManager.getUserId();
            String addictionType = sessionManager.getAddictionType();

            if (addictionType == null || addictionType.isEmpty()) {
                tvNoPlans.setText("Сначала выберите зависимость");
                tvNoPlans.setVisibility(View.VISIBLE);
                listViewPlans.setVisibility(View.GONE);
                return;
            }

            availablePlans = dbHelper.getPlansByAddictionType(addictionType);
            userPlans = dbHelper.getUserPlans(userId);

            if (availablePlans == null || availablePlans.isEmpty()) {
                tvNoPlans.setText("Нет планов. Создайте свой!");
                tvNoPlans.setVisibility(View.VISIBLE);
                listViewPlans.setVisibility(View.GONE);
            } else {
                tvNoPlans.setVisibility(View.GONE);
                listViewPlans.setVisibility(View.VISIBLE);
                adapter = new PlansAdapter();
                listViewPlans.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvNoPlans.setText("Ошибка загрузки: " + e.getMessage());
            tvNoPlans.setVisibility(View.VISIBLE);
        }
    }

    private void checkPermissions() {
        // Для Android 13+ запрашиваем разрешение на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // Для Android 12+ запрашиваем разрешение на точные будильники
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(getActivity())
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

    private void testReminder() {
        try {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(getActivity(), ReminderService.class);
            intent.putExtra("reminder_title", "🔔 ТЕСТОВОЕ НАПОМИНАНИЕ");
            intent.putExtra("reminder_description", "Это тестовое напоминание! Вибрация и звук работают!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 999, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Через 10 секунд
            long triggerTime = System.currentTimeMillis() + 10000;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

            Toast.makeText(getActivity(), "⏰ Тестовое напоминание через 10 секунд!", Toast.LENGTH_LONG).show();
            android.util.Log.d("TestReminder", "Напоминание установлено на " + new java.util.Date(triggerTime));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Уведомления разрешены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Для работы напоминаний нужно разрешить уведомления", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            loadData();
        }
    }

    private class PlansAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return availablePlans != null ? availablePlans.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return availablePlans != null ? availablePlans.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return availablePlans != null ? availablePlans.get(position).getId() : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_plan_improved, parent, false);
            }

            Plan plan = availablePlans.get(position);

            TextView tvTitle = convertView.findViewById(R.id.tvPlanTitle);
            TextView tvDescription = convertView.findViewById(R.id.tvPlanDescription);
            TextView tvDuration = convertView.findViewById(R.id.tvPlanDuration);
            Button btnComplete = convertView.findViewById(R.id.btnComplete);
            Button btnStart = convertView.findViewById(R.id.btnStart);
            ImageButton btnReminder = convertView.findViewById(R.id.btnReminder);
            ImageButton btnDelete = convertView.findViewById(R.id.btnDelete);
            TextView tvStatus = convertView.findViewById(R.id.tvPlanStatus);

            tvTitle.setText(plan.getTitle());
            tvDescription.setText(plan.getDescription());
            tvDuration.setText("Длительность: " + plan.getDurationDays() + " дней");

            // Показываем кнопку удаления только для пользовательских планов
            if (plan.isCustom()) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> showDeleteConfirmation(plan));
            } else {
                btnDelete.setVisibility(View.GONE);
            }

            // Кнопка напоминания для всех планов
            btnReminder.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddReminderActivity.class);
                intent.putExtra("plan_id", plan.getId());
                intent.putExtra("plan_title", plan.getTitle());
                startActivity(intent);
            });

            UserPlan userPlan = getUserPlanForPlan(plan.getId());

            if (userPlan != null) {
                btnStart.setVisibility(View.GONE);

                if ("completed".equals(userPlan.getStatus())) {
                    tvStatus.setText("✓ Выполнен");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnComplete.setVisibility(View.GONE);
                    convertView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    tvStatus.setText("В процессе");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    btnComplete.setVisibility(View.VISIBLE);
                    btnComplete.setText("Отметить выполненным");
                    btnComplete.setTag(userPlan.getId());
                    btnComplete.setOnClickListener(v -> {
                        int userPlanId = (int) v.getTag();
                        completePlan(userPlanId);
                    });
                }
            } else {
                tvStatus.setText("Не начат");
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                btnComplete.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setOnClickListener(v -> startPlan(plan.getId()));
            }

            return convertView;
        }

        private UserPlan getUserPlanForPlan(int planId) {
            if (userPlans != null) {
                for (UserPlan up : userPlans) {
                    if (up.getPlanId() == planId) {
                        return up;
                    }
                }
            }
            return null;
        }
    }

    private void showDeleteConfirmation(Plan plan) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Удалить план")
                .setMessage("Вы уверены, что хотите удалить план \"" + plan.getTitle() + "\"?")
                .setPositiveButton("Да", (dialog, which) -> deletePlan(plan))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deletePlan(Plan plan) {
        int userId = sessionManager.getUserId();
        boolean success = dbHelper.deleteCustomPlan(plan.getId(), userId);

        if (success) {
            Toast.makeText(getActivity(), "План удален", Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            Toast.makeText(getActivity(), "Ошибка при удалении", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlan(int planId) {
        int userId = sessionManager.getUserId();
        boolean success = dbHelper.assignPlanToUser(userId, planId);

        if (success) {
            Toast.makeText(getActivity(), "План начат! Удачи!", Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            Toast.makeText(getActivity(), "Ошибка при начале плана", Toast.LENGTH_SHORT).show();
        }
    }

    private void completePlan(int userPlanId) {
        boolean success = dbHelper.completeUserPlan(userPlanId);

        if (success) {
            Toast.makeText(getActivity(), "План выполнен! Поздравляем!", Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            Toast.makeText(getActivity(), "Ошибка при выполнении плана", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}