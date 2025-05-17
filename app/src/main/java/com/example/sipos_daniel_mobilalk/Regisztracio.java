package com.example.sipos_daniel_mobilalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class Regisztracio extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = Regisztracio.class.getName();
    private static final String PREF_KEY = Regisztracio.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    EditText ETfelhnev;
    EditText ETemailcim;
    EditText ETpw;
    EditText ETpwujra;
    EditText ETtel;
    Spinner spinner;
    CardView regCard;
    CardView contactCard;
    Button registerButton;
    Button cancelButton;

    private SharedPreferences pref;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regisztracio);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }

        ETfelhnev = findViewById(R.id.ETfelh);
        ETemailcim = findViewById(R.id.ETemail);
        ETpw = findViewById(R.id.ETjelszo);
        ETpwujra = findViewById(R.id.ETjelszoUjra);
        ETtel = findViewById(R.id.ETtelefon);
        spinner = findViewById(R.id.SPtele);
        registerButton = findViewById(R.id.registerButton);
        cancelButton = findViewById(R.id.cancelButton);

        try {
            regCard = findViewById(R.id.regCard);
            contactCard = findViewById(R.id.contactCard);

            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            regCard.startAnimation(slideIn);

            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            contactCard.startAnimation(fadeIn);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Animáció hiba: " + e.getMessage());
        }

        pref = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String felhasznalonev = pref.getString("felhasznalonev", "");
        String jelszo = pref.getString("jelszo", "");

        ETfelhnev.setText(felhasznalonev);
        ETpw.setText(jelszo);
        ETpwujra.setText(jelszo);

        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.telefonok, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        try {
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Firebase inicializálási hiba: " + e.getMessage());
        }

        Log.i(LOG_TAG, "onCreate");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void register(View view) {
        String felhasznalonev = ETfelhnev.getText().toString();
        String email = ETemailcim.getText().toString();
        String jelszo = ETpw.getText().toString();
        String jelszoUjra = ETpwujra.getText().toString();

        if(felhasznalonev.isEmpty() || email.isEmpty() || jelszo.isEmpty() || jelszoUjra.isEmpty()) {
            Toast.makeText(this, "Kérjük, tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!jelszo.equals(jelszoUjra)) {
            Toast.makeText(this, "A két jelszó nem egyezik!", Toast.LENGTH_SHORT).show();
            return;
        }

        String telefonszam = ETtel.getText().toString();
        String telefonTipus = spinner.getSelectedItem().toString();

        Log.i(LOG_TAG, "Regisztált: " + felhasznalonev + ", E-mail: " + email);

        try {
            auth.createUserWithEmailAndPassword(email, jelszo).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "A felhasználó sikeresen létrehozva");
                    Toast.makeText(Regisztracio.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    torrentStart();
                } else {
                    Log.d(LOG_TAG, "A felhasználót nem sikerült létrehozni");
                    Toast.makeText(Regisztracio.this, "Sikertelen regisztráció: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Firebase hiba: " + e.getMessage());
            torrentStart();
        }
    }

    public void megse(View view) {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void torrentStart() {
        Intent intent = new Intent(this, Torrent.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();
        Log.i(LOG_TAG, selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nincs teendő
    }
}