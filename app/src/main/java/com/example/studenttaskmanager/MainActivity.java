package com.example.studenttaskmanager;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText taskInput;
    private TextView taskCounter;
    private LinearLayout taskContainer;
    private TextView selectedDueDate;

    private final ArrayList<Task> tasks = new ArrayList<>();
    private Date selectedDate = null;
    private SharedPreferences preferences;

    private static class Task {
        String title;
        Long dueDateMillis;
        boolean completed;

        Task(String title, Long dueDateMillis, boolean completed) {
            this.title = title;
            this.dueDateMillis = dueDateMillis;
            this.completed = completed;
        }
    }

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

        preferences = getSharedPreferences("student_tasks", MODE_PRIVATE);

        addTaskButton.setOnClickListener(v -> addTask());
        dueDateButton.setOnClickListener(v -> chooseDueDate());

        loadTasks();
        renderTasks();
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
                    selectedDueDate.setText(
                            "Due date: " + formatDate(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private String formatDate(Date date) {
        DateFormat dateFormat =
                DateFormat.getDateInstance(
                        DateFormat.MEDIUM, Locale.getDefault());
        return dateFormat.format(date);
    }

    private void addTask() {
        String title = taskInput.getText().toString().trim();

        if (title.isEmpty()) {
            taskInput.setError("Please enter a task");
            return;
        }

        Long dueDateMillis =
                selectedDate == null ? null : selectedDate.getTime();

        tasks.add(new Task(title, dueDateMillis, false));
        saveTasks();
        renderTasks();

        taskInput.setText("");
        selectedDate = null;
        selectedDueDate.setText("No due date selected");

        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
    }

    private void renderTasks() {
        taskContainer.removeAllViews();

        for (int i = 0; i < tasks.size(); i++) {
            final int taskIndex = i;
            Task task = tasks.get(i);

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
            checkBox.setText(task.title);
            checkBox.setTextSize(16);
            checkBox.setChecked(task.completed);
            applyStrikeThrough(checkBox, task.completed);
            taskDetails.addView(checkBox);

            if (task.dueDateMillis != null) {
                TextView dueDateText = new TextView(this);
                dueDateText.setText(
                        "Due: " + formatDate(
                                new Date(task.dueDateMillis)));
                dueDateText.setTextSize(13);
                dueDateText.setPadding(48, 0, 0, 0);
                taskDetails.addView(dueDateText);
            }

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");

            checkBox.setOnCheckedChangeListener((button, checked) -> {
                tasks.get(taskIndex).completed = checked;
                applyStrikeThrough(checkBox, checked);
                saveTasks();
                updateCounter();
            });

            deleteButton.setOnClickListener(v -> {
                tasks.remove(taskIndex);
                saveTasks();
                renderTasks();
            });

            row.addView(taskDetails);
            row.addView(deleteButton);
            taskContainer.addView(row);
        }

        updateCounter();
    }

    private void applyStrikeThrough(
            CheckBox checkBox, boolean completed) {
        if (completed) {
            checkBox.setPaintFlags(
                    checkBox.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            checkBox.setPaintFlags(
                    checkBox.getPaintFlags()
                            & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void saveTasks() {
        JSONArray jsonArray = new JSONArray();

        try {
            for (Task task : tasks) {
                JSONObject object = new JSONObject();
                object.put("title", task.title);
                object.put("completed", task.completed);

                if (task.dueDateMillis != null) {
                    object.put("dueDateMillis", task.dueDateMillis);
                } else {
                    object.put("dueDateMillis", JSONObject.NULL);
                }

                jsonArray.put(object);
            }

            preferences.edit()
                    .putString("tasks", jsonArray.toString())
                    .apply();

        } catch (JSONException e) {
            Toast.makeText(
                    this, "Could not save tasks.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTasks() {
        String savedTasks = preferences.getString("tasks", "[]");

        try {
            JSONArray jsonArray = new JSONArray(savedTasks);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                String title = object.getString("title");
                boolean completed = object.getBoolean("completed");

                Long dueDateMillis = null;
                if (!object.isNull("dueDateMillis")) {
                    dueDateMillis = object.getLong("dueDateMillis");
                }

                tasks.add(
                        new Task(title, dueDateMillis, completed));
            }

        } catch (JSONException e) {
            tasks.clear();
            Toast.makeText(
                    this, "Could not load saved tasks.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCounter() {
        int completed = 0;

        for (Task task : tasks) {
            if (task.completed) {
                completed++;
            }
        }

        int total = tasks.size();
        int remaining = total - completed;

        taskCounter.setText(
                "Task: " + total + " | Remaining: " + remaining);
    }
}
