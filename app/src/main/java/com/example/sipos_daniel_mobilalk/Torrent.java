package com.example.sipos_daniel_mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;
import android.widget.Button;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

public class Torrent extends AppCompatActivity {
    private static final String LOG_TAG = Torrent.class.getName();
    private FirebaseUser user;
    private ImageView torrentImage;
    private TextView torrentText;
    private CardView imageCard;
    private RatingBar ratingBar;
    private Button downloadButton;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrent);

        torrentImage = findViewById(R.id.torrentImage);
        torrentText = findViewById(R.id.torrentTitle);
        imageCard = findViewById(R.id.imageCard);
        ratingBar = findViewById(R.id.ratingBar);
        downloadButton = findViewById(R.id.downloadButton);

        Button listButton = findViewById(R.id.listButton);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Torrent.this, TorrentListActivity.class));
            }
        });

        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        imageCard.startAnimation(scaleUp);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Torrent.this, "Letöltés elkezdődött!", Toast.LENGTH_SHORT).show();
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Toast.makeText(Torrent.this, "Értékelés: " + rating, Toast.LENGTH_SHORT).show();
            }
        });

        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                Log.d(LOG_TAG, "Helyes felhasználó");
                torrentText.setText("Üdvözöljük " + (user.getDisplayName() != null ? user.getDisplayName() : "a Torrent alkalmazásban!"));
            } else {
                Log.d(LOG_TAG, "Helytelen felhasználó");
                torrentText.setText("Üdvözöljük a Torrent alkalmazásban!");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Firebase hiba: " + e.getMessage());
            torrentText.setText("Üdvözöljük a Torrent alkalmazásban!");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Kamera engedélyezve!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Kamera engedély megtagadva!", Toast.LENGTH_SHORT).show();
                }
            }
        );
        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Helymeghatározás engedélyezve!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Helymeghatározás engedély megtagadva!", Toast.LENGTH_SHORT).show();
                }
            }
        );

        Button cameraButton = findViewById(R.id.cameraButton);
        Button locationButton = findViewById(R.id.locationButton);
        cameraButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                Toast.makeText(this, "Kamera engedély már megadva!", Toast.LENGTH_SHORT).show();
            }
        });
        locationButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                Toast.makeText(this, "Helymeghatározás engedély már megadva!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

// Modell osztály a Firestore-hoz
class TorrentItem {
    private String id;
    private String name;
    private String description;
    private int rating;

    public TorrentItem() {}
    public TorrentItem(String id, String name, String description, int rating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rating = rating;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}