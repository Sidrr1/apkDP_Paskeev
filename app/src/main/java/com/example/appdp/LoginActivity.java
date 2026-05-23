package com.example.appdp;



import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.User;
import com.example.appdp.SessionManager;


public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        // Проверяем, не залогинен ли уже пользователь
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.getUserByEmail(email);
        if (user == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        // Простое хэширование пароля
        String hashedPassword = String.valueOf(password.hashCode());
        if (!hashedPassword.equals(user.getPasswordHash())) {
            Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Логируем данные пользователя перед сохранением
        android.util.Log.d("LoginActivity", "User found: " + user.getEmail());
        android.util.Log.d("LoginActivity", "Addiction type: " + user.getAddictionType());
        android.util.Log.d("LoginActivity", "Start date: " + user.getStartDate());
        android.util.Log.d("LoginActivity", "Days counter: " + user.getDaysCounter());

        sessionManager.createLoginSession(user);

        // Проверяем, что данные сохранились
        String savedEmail = sessionManager.getUserEmail();
        android.util.Log.d("LoginActivity", "Saved email in session: " + savedEmail);

        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent;
        if (sessionManager.getAddictionType() == null) {
            intent = new Intent(LoginActivity.this, AddictionChoiceActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}