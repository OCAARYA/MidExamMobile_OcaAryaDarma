package com.example.exammobile_oca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "settings";
    private static final String KEY_DARK = "darkMode";

    TextInputLayout layoutName, layoutStudentId;
    TextInputEditText editName, editStudentId;
    CheckBox checkEnrolled, checkDark;
    Button btnAdd, btnView;
    DBHelper dbHelper;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layoutName = findViewById(R.id.layoutName);
        layoutStudentId = findViewById(R.id.layoutStudentId);
        editName = findViewById(R.id.editName);
        editStudentId = findViewById(R.id.editStudentId);
        checkEnrolled = findViewById(R.id.checkEnrolled);
        checkDark = findViewById(R.id.checkDark);
        btnAdd = findViewById(R.id.btnAdd);
        btnView = findViewById(R.id.btnView);
        dbHelper = new DBHelper(this);

        checkDark.setChecked(isDark);
        checkDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        btnAdd.setOnClickListener(view -> {
            layoutName.setError(null);
            layoutStudentId.setError(null);

            String name = String.valueOf(editName.getText()).trim();
            String studentId = String.valueOf(editStudentId.getText()).trim();
            boolean enrolled = checkEnrolled.isChecked();

            boolean valid = true;
            if (name.isEmpty()) {
                layoutName.setError("Student name is required");
                valid = false;
            }
            if (studentId.isEmpty()) {
                layoutStudentId.setError("Student ID is required");
                valid = false;
            }
            if (!valid) return;

            if (dbHelper.addStudent(new StudentModel(name, studentId, enrolled))) {
                Toast.makeText(this, "Add Successful", Toast.LENGTH_SHORT).show();
                editName.setText("");
                editStudentId.setText("");
                checkEnrolled.setChecked(false);
                editName.requestFocus();
            } else {
                layoutStudentId.setError("Student ID already exists");
            }
        });

        btnView.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ViewStudent.class)));
    }
}