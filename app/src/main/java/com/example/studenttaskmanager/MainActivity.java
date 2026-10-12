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
import android.app.DatePickerDialog;

import java.util.ArrayList;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText taskInput;
    private TextView taskCounter;
    private LinearLayout taskContainer;
    private TextView selectedDueDate;
    private final ArrayList<CheckBox> taskChecks = new ArrayList<>();
    private Date selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        taskCounter = findViewById(R.id.taskCounter);
        selectedDueDate = findViewById(R.id.selectedDueDate);
        taskContainer = findViewById(R.id.taskContainer);

        Button addTaskButton = findViewById(R.id.addTaskButton);
        Button dueDateButton = findViewById(R.id.dueDateButton);

        addTaskButton.setOnClickListener(v -> addTask());
        dueDateButton.setOnClickListener(v -> chooseDueDate());
        updateCounter();
    }

    private void chooseDueDate() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar chosenDate = Calendar.getInstance();
                    chosenDate.set(year, month, dayOfMonth, 12, 0, 0);
                    chosenDate.set(Calendar.MILLISECOND, 0);

                    selectedDate = chosenDate.getTime();

                    DateFormat dateFormat =
                            DateFormat.getDateInstance(
                                    DateFormat.MEDIUM, Locale.getDefault());

                    selectedDueDate.setText(
                            "Due date: " + dateFormat.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void addTask() {
        String title = taskInput.getText().toString().trim();

        if (title.isEmpty()) {
            taskInput.setError("Please enter a task");
            return;
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(4, 8, 4, 8);

        LinearLayout taskDetails = new LinearLayout(this);
        taskDetails.setOrientation(LinearLayout.VERTICAL);
        taskDetails.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(title);
        checkBox.setTextSize(16);

        taskDetails.addView(checkBox);

        if (selectedDate != null) {
            TextView dueDateText = new TextView(this);

            DateFormat dateFormat =
                    DateFormat.getDateInstance(
                            DateFormat.MEDIUM, Locale.getDefault());

            dueDateText.setText("Due: " + dateFormat.format(selectedDate));
            dueDateText.setTextSize(13);
            dueDateText.setPadding(48, 0, 0, 0);

            taskDetails.addView(dueDateText);
        }

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");

        checkBox.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                checkBox.setPaintFlags(
                        checkBox.getPaintFlags()
                                | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                checkBox.setPaintFlags(
                        checkBox.getPaintFlags()
                                & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            updateCounter();
        });

        deleteButton.setOnClickListener(v -> {
            taskContainer.removeView(row);
            taskChecks.remove(checkBox);
            updateCounter();
        });

        row.addView(taskDetails);
        row.addView(deleteButton);

        taskContainer.addView(row);
        taskChecks.add(checkBox);

        taskInput.setText("");
        selectedDate = null;
        selectedDueDate.setText("No due date selected");

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