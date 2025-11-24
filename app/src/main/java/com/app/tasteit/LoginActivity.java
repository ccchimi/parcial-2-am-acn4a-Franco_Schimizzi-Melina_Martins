package com.app.tasteit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnCreateUser;
    private TextView tvForgot;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public static String currentUser = null; // ahora guardamos USERNAME, no email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        tvForgot = findViewById(R.id.tvForgot);

        // Si ya estaba logueado lo mando al Home
        if (auth.getCurrentUser() != null) {
            // Recupero username guardado
            currentUser = auth.getCurrentUser().getDisplayName();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> loginUser());
        btnCreateUser.setOnClickListener(v -> goToRegister());
        tvForgot.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Ingrese un nombre de usuario");
            return;
        }
        if (pass.isEmpty()) {
            etPassword.setError("Ingrese la contraseña");
            return;
        }

        // 1️⃣ Buscar email REAL de ese username
        db.collection("users")
                .document(username)   // username = ID único en Firestore
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String email = doc.getString("email");

                    if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "El usuario no tiene un email válido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2️⃣ Autenticar con FirebaseAuth usando email real
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {

                                // 3️⃣ Guardamos currentUser como username, NO como email
                                currentUser = username;

                                Toast.makeText(this, "Bienvenido " + username, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al buscar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void resetPassword() {
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Ingresá un usuario para recuperar contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String email = doc.getString("email");

                    auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(v ->
                                    Toast.makeText(this, "Enviamos un email para restablecer la contraseña", Toast.LENGTH_LONG).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                });
    }

    private void goToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    // Usado por ProfileActivity
    public static void logout(AppCompatActivity activity) {
        FirebaseAuth.getInstance().signOut();
        currentUser = null;

        Toast.makeText(activity, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }
}