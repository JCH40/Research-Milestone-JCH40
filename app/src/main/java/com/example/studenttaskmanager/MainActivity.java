package com.example.studenttaskmanager;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Paint;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewTreeLifecycleOwner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText taskInput;
    private TextView taskCounter;
    private LinearLayout taskContainer;
    private final ArrayList<CheckBox> taskChecks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        taskCounter = findViewById(R.id.taskCounter);
        taskContainer = findViewById(R.id.taskContainer);

        Button addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> addTask());
        updateCounter();
    }

    private void addTask() {
        String title = taskInput.getText()
                .toString().trim();
        if (title.isEmpty()) {
            taskInput.setError("Please enter a task");
            return;
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(4, 8, 4, 8);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(title);
        checkBox.setTextSize(16);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");

        checkBox.setOnCheckedChangeListener(
                (button, checked) -> {
                    if (checked) {
                        checkBox.setPaintFlags(
                                checkBox.getPaintFlags()
                                        | Paint.STRIKE_THRU_TEXT_FLAG
                        );
                    } else {
                        checkBox.setPaintFlags(
                                checkBox.getPaintFlags()
                                        & ~Paint.STRIKE_THRU_TEXT_FLAG
                        );
                    }

                    updateCounter();
                }
        );

        deleteButton.setOnClickListener(v -> {
            taskContainer.removeView(row);
            taskChecks.remove(checkBox);
            updateCounter();
        });

        row.addView(checkBox);
        row.addView(deleteButton);

        taskContainer.addView(row);
        taskChecks.add(checkBox);

        taskInput.setText("");
        updateCounter();

        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
    }

    private void updateCounter() {
        int completed = 0;

        for (CheckBox checkBox : taskChecks) {
            if (checkBox.isChecked()) {
                completed++;
            }
        }

        int total = taskChecks.size();
        int remaining = total - completed;

        taskCounter.setText("Task: " + total + " | Remaining: " + remaining);
    }
}