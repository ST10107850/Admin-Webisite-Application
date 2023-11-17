package com.example.part_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.part_2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText full_name, email, password, con_pass;
    Button  submit;
    TextView txtLog;
    // Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        full_name = findViewById(R.id.fullNames);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        con_pass = findViewById(R.id.con_pass);
        submit = findViewById(R.id.reg_btn);

        txtLog = findViewById(R.id.log_txt);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Admin");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        txtLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser() {
        String fullNameValue = full_name.getText().toString().trim();
        String emailValue = email.getText().toString().trim();
        String passwordValue = password.getText().toString().trim();
        String confirmPasswordValue = con_pass.getText().toString().trim();

        // Validate fields
        if (fullNameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty() || confirmPasswordValue.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format

       /* if (!emailValue.endsWith("@starbuck.co.za")) {
            Toast.makeText(this, "Email must end with @starbuck.co.za", Toast.LENGTH_SHORT).show();
            return;
        }*/

        if (!emailValue.endsWith("@starbuck.co.za") || emailValue.length() <= "@starbuck.co.za".length()) {
        Toast.makeText(this, "Invalid email format. Email must end with @starbuck.co.za and contain characters.", Toast.LENGTH_SHORT).show();
        return;
    }
        // Validate password strength (you can customize this based on your criteria)
        if (passwordValue.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }


        // Validate password and confirm password match
        if (!passwordValue.equals(confirmPasswordValue)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a User object
        User user = new User();
        user.setFullName(fullNameValue);
        user.setEmail(emailValue);
        user.setPassword(passwordValue);

        // Use Firebase Authentication to create a new user
        mAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save user to Realtime Database only if authentication is successful
                        databaseReference.push().setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Registered successfully...", Toast.LENGTH_SHORT).show();
                                    // Proceed to the next activity or perform the desired action
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish(); // Close the current activity
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace(); // Log the error for debugging
                                });
                    } else {
                        // If registration fails, display a message to the user.
                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}