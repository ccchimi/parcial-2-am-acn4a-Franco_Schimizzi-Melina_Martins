package com.app.tasteit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RecipeFormActivity extends AppCompatActivity {

    private EditText etTitle, etDesc, etImage, etTime;
    private Button btnSave;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_form);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etDesc  = findViewById(R.id.etDescription);
        etImage = findViewById(R.id.etImageUrl);
        etTime  = findViewById(R.id.etCookingTime);
        btnSave = findViewById(R.id.btnSaveRecipe);

        // Ver si estamos editando
        recipeId = getIntent().getStringExtra("recipeId");

        if (recipeId != null) {
            loadRecipe();
        }

        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void loadRecipe() {
        if (recipeId == null) return;

        db.collection("comunidad")
                .document(recipeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etTitle.setText(doc.getString("title"));
                    etDesc.setText(doc.getString("description"));
                    etImage.setText(doc.getString("imageUrl"));
                    etTime.setText(doc.getString("cookingTime"));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error al cargar la receta: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void saveRecipe() {

        String title = etTitle.getText().toString().trim();
        String desc  = etDesc.getText().toString().trim();
        String img   = etImage.getText().toString().trim();
        String time  = etTime.getText().toString().trim();

        if (title.isEmpty()) { etTitle.setError("Título requerido"); return; }
        if (desc.isEmpty())  { etDesc.setError("Descripción requerida"); return; }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debés iniciar sesión para publicar", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid   = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        Map<String,Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", desc);
        data.put("imageUrl", img);
        data.put("cookingTime", time);

        // Autor visible: username si lo tenemos, si no, email
        String visibleAuthor = LoginActivity.currentUsername;
        if (visibleAuthor == null || visibleAuthor.isEmpty()) {
            visibleAuthor = (email != null ? email : "usuario");
        }

        data.put("author", visibleAuthor);
        data.put("authorId", uid);
        data.put("authorEmail", email);

        if (recipeId == null) {
            // CREAR
            data.put("createdAt", System.currentTimeMillis());

            db.collection("comunidad")
                    .add(data)
                    .addOnSuccessListener(docRef -> {

                        String newId = docRef.getId();

                        // guardamos el ID dentro del documento
                        docRef.update("id", newId);

                        // guardar también en usuarios/{uid}/recetas
                        db.collection("usuarios")
                                .document(uid)
                                .collection("recetas")
                                .document(newId)
                                .set(data);

                        Toast.makeText(this, "Receta creada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error al crear receta: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );

        } else {
            // EDITAR – Solo si es dueño
            db.collection("comunidad")
                    .document(recipeId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            Toast.makeText(this, "La receta ya no existe", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String owner = doc.getString("authorId");

                        if (owner == null || !owner.equals(uid)) {
                            Toast.makeText(this,
                                    "No podés editar esta receta (no sos el dueño).",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // actualizar ambos lugares
                        db.collection("comunidad")
                                .document(recipeId)
                                .update(data);

                        db.collection("usuarios")
                                .document(uid)
                                .collection("recetas")
                                .document(recipeId)
                                .update(data);

                        Toast.makeText(this, "Receta actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error al verificar dueño: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        }
    }
}