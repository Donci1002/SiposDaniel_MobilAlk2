package com.example.sipos_daniel_mobilalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    EditText felhasznalonevET;
    EditText jelszoET;
    Button bejelButton;
    Button regButton;
    Button vendegButton;
    Button googleButton;
    CardView loginCard;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private Button openTorrentListButton;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        felhasznalonevET = findViewById(R.id.ETfelh);
        jelszoET = findViewById(R.id.ETjelszo);
        bejelButton = findViewById(R.id.BTbejel);
        regButton = findViewById(R.id.BTbejelregisz);
        vendegButton = findViewById(R.id.BTvendeg);
        googleButton = findViewById(R.id.BTgoogle);
        loginCard = findViewById(R.id.loginCard);
        openTorrentListButton = findViewById(R.id.openTorrentListButton);
        openTorrentListButton.setVisibility(View.GONE);
        openTorrentListButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TorrentListActivity.class));
        });

        pref = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        loginCard.startAnimation(fadeIn);

        // Permission launcherek
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {}
        );
        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {}
        );
        notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {}
        );

        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void bejelentkezes(View view) {
        String felhasznalonev = felhasznalonevET.getText().toString();
        String jelszo = jelszoET.getText().toString();

        if (felhasznalonev.isEmpty() || jelszo.isEmpty()) {
            Toast.makeText(this, "Kérjük, töltsd ki mindkét mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(LOG_TAG, "Bejelentkezve: " + felhasznalonev + ", Jelszó: " + jelszo);
        torrentStart();
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        openTorrentListButton.setVisibility(View.VISIBLE);
    }

    public void reg(View view) {
        Intent intent = new Intent(this, Regisztracio.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void torrentStart() {
        Intent intent = new Intent(this, Torrent.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("felhasznalonev", felhasznalonevET.getText().toString());
        editor.putString("jelszo", jelszoET.getText().toString());
        editor.apply();

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

    public void bejelentkezesVendegkent(View view) {
        Toast.makeText(this, "Bejelentkezés vendégként", Toast.LENGTH_SHORT).show();
        torrentStart();
    }

    public void bejelentkezesGoogleal(View view) {
        Toast.makeText(this, "Google bejelentkezés", Toast.LENGTH_SHORT).show();
        torrentStart();
    }
}