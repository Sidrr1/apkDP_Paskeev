package com.example.appdp;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdp.R;
import com.example.appdp.SessionManager;

public class MainActivity extends AppCompatActivity {
    private Button btnStartJourney;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Проверяем, залогинен ли пользователь
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        btnStartJourney = findViewById(R.id.btnStartJourney);

        // Проверяем, выбрана ли зависимость
        if (sessionManager.getAddictionType() == null) {
            btnStartJourney.setText("Выбрать зависимость");
        } else {
            btnStartJourney.setText("Мой путь");
        }

        btnStartJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionManager.getAddictionType() == null) {
                    // Если зависимость не выбрана, переходим на экран выбора
                    startActivity(new Intent(MainActivity.this, AddictionChoiceActivity.class));
                } else {
                    // Если зависимость выбрана, открываем главный экран с табами
                    startActivity(new Intent(MainActivity.this, MainTabActivity.class));
                }
            }
        });
    }
}