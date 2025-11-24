package com.app.tasteit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etUsername;
    private RecyclerView rvMyRecipes;

    private Gson gson = new Gson();

    private List<CommunityRecipe> myCommunityRecipes = new ArrayList<>();
    private CommunityRecipeAdapter myAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final String COMMUNITY_PREFS = "CommunityPrefs";
    private static final String COMMUNITY_KEY = "community_recipes";

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ---- TOOLBAR + DRAWER ----
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_recetas) {
                startActivity(new Intent(this, RecipesActivity.class));
            } else if (id == R.id.nav_comunidad) {
                startActivity(new Intent(this, CommunityActivity.class));
            } else if (id == R.id.nav_favoritos) {
                Intent i = new Intent(this, RecipesActivity.class);
                i.putExtra("showFavorites", true);
                startActivity(i);
            } else if (id == R.id.nav_logout) {
                LoginActivity.logout(this);
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // ---- POPUP MENU (MI PERFIL / LOGOUT) ----
        ImageView ivAccount = findViewById(R.id.ivAccount);
        AccountMenuHelper.setup(this, ivAccount);

        // ---- CAMPOS DE PERFIL ----
        etFirstName = findViewById(R.id.etProfileFirstName);
        etLastName = findViewById(R.id.etProfileLastName);
        etEmail = findViewById(R.id.etProfileEmail);
        etUsername = findViewById(R.id.etProfileUsername);

        rvMyRecipes = findViewById(R.id.rvMyCommunityRecipes);
        rvMyRecipes.setLayoutManager(new LinearLayoutManager(this));

        Button btnSave = findViewById(R.id.btnSaveProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        String username = LoginActivity.currentUser;

        if (username == null) {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etUsername.setText(username);
        etUsername.setEnabled(false); // Username fijo

        // ---- Cargar datos desde Firestore ----
        loadProfileData(username);

        // ---- Cargar recetas propias (hasta migrarlas) ----
        loadMyCommunityRecipes(username);

        myAdapter = new CommunityRecipeAdapter(this, myCommunityRecipes);
        rvMyRecipes.setAdapter(myAdapter);

        btnSave.setOnClickListener(v -> saveProfileData(username));
        btnLogout.setOnClickListener(v -> LoginActivity.logout(this));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    //   FIRESTORE – Cargar DATA
    private void loadProfileData(String username) {

        db.collection("users")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Error: No existe el usuario en Firestore", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    etFirstName.setText(doc.getString("firstName"));
                    etLastName.setText(doc.getString("lastName"));
                    etEmail.setText(doc.getString("email"));

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    //   FIRESTORE – Guardar DATA
    private void saveProfileData(String username) {

        String newFirst = etFirstName.getText().toString().trim();
        String newLast = etLastName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        db.collection("users")
                .document(username)
                .update(
                        "firstName", newFirst,
                        "lastName", newLast,
                        "email", newEmail
                )
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ======================================================================
    //   (TEMPORAL) Cargar recetas del usuario desde SharedPrefs
    //   Esto se migrara a Firestore despues.
    // ======================================================================
    private void loadMyCommunityRecipes(String username) {

        SharedPreferences prefs = getSharedPreferences(COMMUNITY_PREFS, MODE_PRIVATE);
        String json = prefs.getString(COMMUNITY_KEY, null);

        if (json == null) {
            myCommunityRecipes = new ArrayList<>();
            return;
        }

        Type type = new TypeToken<List<CommunityRecipe>>(){}.getType();
        List<CommunityRecipe> all = gson.fromJson(json, type);

        if (all == null) {
            myCommunityRecipes = new ArrayList<>();
            return;
        }

        myCommunityRecipes = new ArrayList<>();

        for (CommunityRecipe r : all) {
            if (r.getAuthor() != null && r.getAuthor().equals(username)) {
                myCommunityRecipes.add(r);
            }
        }
    }
}