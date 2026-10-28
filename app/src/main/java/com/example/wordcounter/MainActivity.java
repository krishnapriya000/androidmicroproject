
package com.example.wordcounter;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView wordCount, characterCount;
    Button saveButton, updateButton, deleteButton, clearButton;
    ListView historyList;

    DBHelper dbHelper;
    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> entries = new ArrayList<>();
    ArrayAdapter<String> adapter;

    int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        editText = findViewById(R.id.editText);
        wordCount = findViewById(R.id.wordCount);
        characterCount = findViewById(R.id.characterCount);
        saveButton = findViewById(R.id.saveButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        clearButton = findViewById(R.id.clearButton);
        historyList = findViewById(R.id.historyList);

        dbHelper = new DBHelper(this);

        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, entries);
        historyList.setAdapter(adapter);

        // Live word and character count
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                updateCounter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // CREATE: Save text
        saveButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();

            if (text.isEmpty()) {
                showMessage("Please enter some text");
                return;
            }

            int words = countWords(text);
            int characters = text.length();

            long result = dbHelper.insertText(
                    text, words, characters);

            if (result != -1) {
                showMessage("Text saved successfully");
                selectedId = -1;
                loadHistory();
            } else {
                showMessage("Unable to save text");
            }
        });

        // READ: Tap a saved entry to select it
        historyList.setOnItemClickListener((parent, view, position, id) -> {
            selectedId = ids.get(position);

            Cursor cursor = dbHelper.getAllTexts();

            try {
                while (cursor.moveToNext()) {
                    int recordId = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id"));

                    if (recordId == selectedId) {
                        String savedText = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "text_content"));

                        editText.setText(savedText);
                        editText.setSelection(savedText.length());
                        break;
                    }
                }
            } finally {
                cursor.close();
            }

            showMessage("Text selected. You can edit it now.");
        });

        // UPDATE: Update selected entry
        updateButton.setOnClickListener(v -> {
            if (selectedId == -1) {
                showMessage("Select a saved entry first");
                return;
            }

            String text = editText.getText().toString().trim();

            if (text.isEmpty()) {
                showMessage("Please enter some text");
                return;
            }

            int result = dbHelper.updateText(
                    selectedId, text,
                    countWords(text), text.length());

            if (result > 0) {
                showMessage("Text updated successfully");
                selectedId = -1;
                loadHistory();
            } else {
                showMessage("Unable to update text");
            }
        });

        // DELETE: Delete selected entry
        deleteButton.setOnClickListener(v -> {
            if (selectedId == -1) {
                showMessage("Select a saved entry first");
                return;
            }

            int result = dbHelper.deleteText(selectedId);

            if (result > 0) {
                showMessage("Text deleted successfully");
                selectedId = -1;
                editText.setText("");
                loadHistory();
            } else {
                showMessage("Unable to delete text");
            }
        });

        // Clear the input box
        clearButton.setOnClickListener(v -> {
            editText.setText("");
            selectedId = -1;
        });

        loadHistory();
        updateCounter("");
    }

    private int countWords(String text) {
        String trimmed = text.trim();

        if (trimmed.isEmpty()) {
            return 0;
        }

        return trimmed.split("\\s+").length;
    }

    private void updateCounter(String text) {
        wordCount.setText("Words: " + countWords(text));
        characterCount.setText("Characters: " + text.length());
    }

    // READ: Load all saved entries from SQLite
    private void loadHistory() {
        ids.clear();
        entries.clear();

        Cursor cursor = dbHelper.getAllTexts();

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String text = cursor.getString(
                        cursor.getColumnIndexOrThrow("text_content"));

                int words = cursor.getInt(
                        cursor.getColumnIndexOrThrow("word_count"));

                int characters = cursor.getInt(
                        cursor.getColumnIndexOrThrow("character_count"));

                ids.add(id);
                entries.add("ID: " + id + "\n"
                        + text + "\n"
                        + "Words: " + words
                        + " | Characters: " + characters);
            }
        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
