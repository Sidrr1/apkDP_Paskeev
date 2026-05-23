package com.example.appdp;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.User;
import com.example.appdp.SessionManager;

public class AddictionChoiceActivity extends AppCompatActivity {
    private RadioGroup radioGroupAddiction;
    private RadioButton rbSmoking, rbAlcohol, rbVaping, rbEnergy;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addiction_choice);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        // Проверяем, залогинен ли пользователь
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        radioGroupAddiction = findViewById(R.id.radioGroupAddiction);
        rbSmoking = findViewById(R.id.rbSmoking);
        rbAlcohol = findViewById(R.id.rbAlcohol);
        rbVaping = findViewById(R.id.rbVaping);
        rbEnergy = findViewById(R.id.rbEnergy);
        btnSave = findViewById(R.id.btnSave);

        // Добавляем обработчики кликов на сами RadioButton
        setupRadioButtonListeners();
    }

    private void setupRadioButtonListeners() {
        rbSmoking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroupAddiction.check(R.id.rbSmoking);
                updateCardBackground(R.id.rbSmoking);
            }
        });

        rbAlcohol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroupAddiction.check(R.id.rbAlcohol);
                updateCardBackground(R.id.rbAlcohol);
            }
        });

        rbVaping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroupAddiction.check(R.id.rbVaping);
                updateCardBackground(R.id.rbVaping);
            }
        });

        rbEnergy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroupAddiction.check(R.id.rbEnergy);
                updateCardBackground(R.id.rbEnergy);
            }
        });
    }

    private void updateCardBackground(int selectedId) {
        // Сброс фона всех карточек
        resetCardBackgrounds();

        // Подсветка выбранной карточки
        if (selectedId == R.id.rbSmoking) {
            findViewById(R.id.cardSmoking).setBackgroundResource(R.drawable.card_selected_smoking);
        } else if (selectedId == R.id.rbAlcohol) {
            findViewById(R.id.cardAlcohol).setBackgroundResource(R.drawable.card_selected_alcohol);
        } else if (selectedId == R.id.rbVaping) {
            findViewById(R.id.cardVaping).setBackgroundResource(R.drawable.card_selected_vaping);
        } else if (selectedId == R.id.rbEnergy) {
            findViewById(R.id.cardEnergy).setBackgroundResource(R.drawable.card_selected_energy);
        }
    }

    private void resetCardBackgrounds() {
        findViewById(R.id.cardSmoking).setBackgroundResource(R.drawable.card_default);
        findViewById(R.id.cardAlcohol).setBackgroundResource(R.drawable.card_default);
        findViewById(R.id.cardVaping).setBackgroundResource(R.drawable.card_default);
        findViewById(R.id.cardEnergy).setBackgroundResource(R.drawable.card_default);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddictionChoice();
            }
        });
    }

    private void saveAddictionChoice() {
        int selectedId = radioGroupAddiction.getCheckedRadioButtonId();
        String addictionType = null;

        if (selectedId == R.id.rbSmoking) {
            addictionType = "smoking";
        } else if (selectedId == R.id.rbAlcohol) {
            addictionType = "alcohol";
        } else if (selectedId == R.id.rbVaping) {
            addictionType = "vaping";
        } else if (selectedId == R.id.rbEnergy) {
            addictionType = "energy";
        }

        if (addictionType == null) {
            Toast.makeText(this, "Выберите тип зависимости", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        boolean success = dbHelper.updateUserAddiction(userId, addictionType);

        if (success) {
            sessionManager.setAddictionType(addictionType);

            // Получаем обновленного пользователя и сохраняем в сессию
            User user = dbHelper.getUserByEmail(sessionManager.getUserEmail());
            if (user != null) {
                sessionManager.createLoginSession(user);
            }

            Toast.makeText(this, "Зависимость выбрана! Начинаем путь!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(AddictionChoiceActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Ошибка сохранения. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
        }
    }
}