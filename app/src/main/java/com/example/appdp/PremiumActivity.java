package com.example.appdp;




import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.appdp.R;
import com.example.appdp.SessionManager;
import com.example.appdp.SubscriptionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PremiumActivity extends AppCompatActivity {

    private CardView cardMonthly, cardYearly;
    private RadioButton rbMonthly, rbYearly;
    private Button btnSubscribe;
    private TextView tvCurrentPlan, tvExpiryDate;
    private ImageView btnBack;

    private SubscriptionManager subscriptionManager;
    private SessionManager sessionManager;
    private String selectedPlan = "monthly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        subscriptionManager = new SubscriptionManager(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadCurrentSubscription();
        setupListeners();

        // Устанавливаем начальное состояние
        rbMonthly.setChecked(true);
        updateCardSelection();
    }

    private void initViews() {
        cardMonthly = findViewById(R.id.cardMonthly);
        cardYearly = findViewById(R.id.cardYearly);
        rbMonthly = findViewById(R.id.rbMonthly);
        rbYearly = findViewById(R.id.rbYearly);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        tvCurrentPlan = findViewById(R.id.tvCurrentPlan);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadCurrentSubscription() {
        if (subscriptionManager.isPremium()) {
            tvCurrentPlan.setText("Текущий план: " + subscriptionManager.getCurrentPlan());
            String expiryDate = subscriptionManager.getExpiryDate();
            if (expiryDate != null && !expiryDate.isEmpty()) {
                tvExpiryDate.setText("Действует до: " + expiryDate);
            } else {
                tvExpiryDate.setText("Действует до: Бессрочно");
            }
            btnSubscribe.setText("Продлить подписку");
        } else {
            tvCurrentPlan.setText("Текущий план: Бесплатный");
            tvExpiryDate.setText("Доступны базовые функции");
            btnSubscribe.setText("Оформить подписку");
        }
    }

    private void updateCardSelection() {
        if ("monthly".equals(selectedPlan)) {
            rbMonthly.setChecked(true);
            rbYearly.setChecked(false);
            cardMonthly.setCardBackgroundColor(getResources().getColor(R.color.primary_light));
            cardYearly.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        } else {
            rbMonthly.setChecked(false);
            rbYearly.setChecked(true);
            cardMonthly.setCardBackgroundColor(getResources().getColor(R.color.card_background));
            cardYearly.setCardBackgroundColor(getResources().getColor(R.color.primary_light));
        }
    }

    private void setupListeners() {
        // Обработка клика по карточке месячного плана
        cardMonthly.setOnClickListener(v -> {
            selectedPlan = "monthly";
            updateCardSelection();
        });

        // Обработка клика по карточке годового плана
        cardYearly.setOnClickListener(v -> {
            selectedPlan = "yearly";
            updateCardSelection();
        });

        btnSubscribe.setOnClickListener(v -> {
            if (selectedPlan.equals("monthly")) {
                subscribeMonthly();
            } else {
                subscribeYearly();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void subscribeMonthly() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        String expiryDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.getTime());

        subscriptionManager.setPremium(true);
        subscriptionManager.setCurrentPlan("Премиум (месячный)");
        subscriptionManager.setExpiryDate(expiryDate);

        Toast.makeText(this, "Подписка оформлена! Действует до: " + expiryDate, Toast.LENGTH_LONG).show();
        loadCurrentSubscription();

        setResult(RESULT_OK);
    }

    private void subscribeYearly() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        String expiryDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.getTime());

        subscriptionManager.setPremium(true);
        subscriptionManager.setCurrentPlan("Премиум (годовой)");
        subscriptionManager.setExpiryDate(expiryDate);

        Toast.makeText(this, "Подписка оформлена! Действует до: " + expiryDate, Toast.LENGTH_LONG).show();
        loadCurrentSubscription();

        setResult(RESULT_OK);
    }
}