package com.example.appdp;



import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.User;
import com.example.appdp.SessionManager;
import com.example.appdp.SubscriptionManager;

public class ProfileFragment extends Fragment {

    private TextView tvAddictionType, tvStartDate, tvDaysCounter;
    private Button btnAddDay, btnLogout, btnPremium;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private SubscriptionManager subscriptionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(getActivity());
        dbHelper = DatabaseHelper.getInstance(getActivity());
        subscriptionManager = new SubscriptionManager(getActivity());

        initViews(view);
        loadUserData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvAddictionType = view.findViewById(R.id.tvAddictionType);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvDaysCounter = view.findViewById(R.id.tvDaysCounter);
        btnAddDay = view.findViewById(R.id.btnAddDay);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnPremium = view.findViewById(R.id.btnPremium);

        // Если кнопка премиум не найдена, создаем её программно
        if (btnPremium == null) {
            createPremiumButton(view);
        }
    }

    private void createPremiumButton(View view) {
        // Ищем родительский контейнер
        ViewGroup parent = (ViewGroup) view;

        // Создаем кнопку программно
        btnPremium = new Button(getActivity());
        btnPremium.setText("✨ Премиум подписка");
        btnPremium.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Применяем стили
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            btnPremium.setBackgroundTintList(getResources().getColorStateList(R.color.primary, getActivity().getTheme()));
        }
        btnPremium.setTextColor(getResources().getColor(android.R.color.white));

        // Добавляем отступы
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        btnPremium.setPadding(padding, padding, padding, padding);

        // Добавляем после кнопки выхода
        ViewGroup.LayoutParams params = btnPremium.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) params).topMargin = padding;
            ((ViewGroup.MarginLayoutParams) params).bottomMargin = padding;
        }

        parent.addView(btnPremium);
    }

    private void loadUserData() {
        try {
            String email = sessionManager.getUserEmail();

            if (email == null || email.isEmpty()) {
                tvAddictionType.setText("Ошибка: email не найден");
                tvStartDate.setText("Пожалуйста, выйдите и войдите снова");
                tvDaysCounter.setText("");
                btnAddDay.setEnabled(false);
                return;
            }

            User user = dbHelper.getUserByEmail(email);

            if (user == null) {
                tvAddictionType.setText("Ошибка: пользователь не найден");
                tvStartDate.setText("Пожалуйста, проверьте данные");
                tvDaysCounter.setText("");
                btnAddDay.setEnabled(false);
                return;
            }

            String addictionType = user.getAddictionType();
            if (addictionType == null || addictionType.isEmpty()) {
                tvAddictionType.setText("Зависимость: не выбрана");
                tvStartDate.setText("Дата начала: не указана");
                tvDaysCounter.setText("Дней без зависимости: 0");
                btnAddDay.setEnabled(false);
            } else {
                String displayType = getAddictionDisplay(addictionType);
                tvAddictionType.setText("Зависимость: " + displayType);

                String startDate = user.getStartDate();
                if (startDate == null || startDate.isEmpty()) {
                    tvStartDate.setText("Дата начала: не указана");
                } else {
                    tvStartDate.setText("Дата начала: " + startDate);
                }

                int daysCounter = user.getDaysCounter();
                tvDaysCounter.setText("Дней без зависимости: " + daysCounter);
                btnAddDay.setEnabled(true);
            }

            // Обновляем текст кнопки премиум в зависимости от статуса подписки
            if (subscriptionManager.isPremium()) {
                btnPremium.setText("👑 Премиум активна");
            } else {
                btnPremium.setText("✨ Премиум подписка");
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvAddictionType.setText("Ошибка: " + e.getMessage());
            tvStartDate.setText("Пожалуйста, перезапустите приложение");
            tvDaysCounter.setText("");
            btnAddDay.setEnabled(false);
        }
    }

    private String getAddictionDisplay(String type) {
        switch (type) {
            case "smoking": return "Курение";
            case "alcohol": return "Алкоголь";
            case "vaping": return "Электронные сигареты";
            case "energy": return "Энергетики";
            default: return type;
        }
    }

    private void setupListeners() {
        btnAddDay.setOnClickListener(v -> addDay());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        if (btnPremium != null) {
            btnPremium.setOnClickListener(v -> goToPremium());
        }
    }

    private void goToPremium() {
        try {
            Intent intent = new Intent(getActivity(), PremiumActivity.class);
            startActivityForResult(intent, 200);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Ошибка открытия премиум страницы: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            loadUserData(); // Обновляем данные после покупки подписки
        }
    }

    private void addDay() {
        try {
            String email = sessionManager.getUserEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(getActivity(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = dbHelper.getUserByEmail(email);
            if (user == null) {
                Toast.makeText(getActivity(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.incrementDayCounter(user.getId());

            if (success) {
                Toast.makeText(getActivity(), "День добавлен! +1", Toast.LENGTH_SHORT).show();
                loadUserData();
            } else {
                Toast.makeText(getActivity(), "Сегодня вы уже отмечали день!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> logout())
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(getActivity(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}