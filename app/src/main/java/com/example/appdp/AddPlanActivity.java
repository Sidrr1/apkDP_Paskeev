package com.example.appdp;



import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdp.R;
import com.example.appdp.DatabaseHelper;
import com.example.appdp.SessionManager;
import java.util.Calendar;

public class AddPlanActivity extends AppCompatActivity {
    private EditText etTitle, etDescription;
    private TextView tvDuration;
    private Button btnSelectDuration, btnSave, btnCancel;
    private int selectedDuration = 7; // дней по умолчанию

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etPlanTitle);
        etDescription = findViewById(R.id.etPlanDescription);
        tvDuration = findViewById(R.id.tvDuration);
        btnSelectDuration = findViewById(R.id.btnSelectDuration);
        btnSave = findViewById(R.id.btnSavePlan);
        btnCancel = findViewById(R.id.btnCancelPlan);

        tvDuration.setText("Длительность: " + selectedDuration + " дней");
    }

    private void setupListeners() {
        btnSelectDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDurationPicker();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlan();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showDurationPicker() {
        // Простой диалог выбора длительности
        final String[] durations = {"7 дней", "14 дней", "21 день", "30 дней", "60 дней", "90 дней"};
        final int[] durationValues = {7, 14, 21, 30, 60, 90};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Выберите длительность")
                .setItems(durations, (dialog, which) -> {
                    selectedDuration = durationValues[which];
                    tvDuration.setText("Длительность: " + selectedDuration + " дней");
                })
                .show();
    }

    private void savePlan() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название плана", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Введите описание плана", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        String addictionType = sessionManager.getAddictionType();

        if (addictionType == null) {
            Toast.makeText(this, "Сначала выберите зависимость", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long planId = dbHelper.addCustomPlan(userId, addictionType, title, description, selectedDuration);

        if (planId != -1) {
            Toast.makeText(this, "План успешно создан!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Ошибка при создании плана", Toast.LENGTH_SHORT).show();
        }
    }
}