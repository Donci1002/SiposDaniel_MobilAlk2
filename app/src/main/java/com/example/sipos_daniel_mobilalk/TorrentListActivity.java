package com.example.sipos_daniel_mobilalk;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TorrentListActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TorrentListActivity";
    private static final String CHANNEL_ID = "torrent_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int ALARM_REQUEST_CODE = 1001;
    private FirebaseFirestore db;
    private CollectionReference torrentsRef;
    private RecyclerView recyclerView;
    private TorrentAdapter adapter;
    private List<TorrentItem> torrentList = new ArrayList<>();
    private EventListener<QuerySnapshot> listener;
    private EditText titleInput, typeInput, sizeInput;
    private TextView lastAddedText;
    private Button addButton;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrent_list);

        db = FirebaseFirestore.getInstance();
        torrentsRef = db.collection("torrents");
        recyclerView = findViewById(R.id.recyclerView);
        titleInput = findViewById(R.id.titleInput);
        typeInput = findViewById(R.id.typeInput);
        sizeInput = findViewById(R.id.sizeInput);
        lastAddedText = findViewById(R.id.lastAddedText);
        addButton = findViewById(R.id.addButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TorrentAdapter(torrentList);
        recyclerView.setAdapter(adapter);

        // Permission kérés fájlíráshoz és kamerához
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Engedély szükséges!", Toast.LENGTH_SHORT).show();
                }
            }
        );

        // Vissza gomb
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String type = typeInput.getText().toString();
            int size = 0;
            try { size = Integer.parseInt(sizeInput.getText().toString()); } catch (Exception ignored) {}
            if (title.isEmpty() || type.isEmpty() || size <= 0) {
                Toast.makeText(this, "Minden mező kötelező!", Toast.LENGTH_SHORT).show();
                return;
            }
            TorrentItem item = new TorrentItem("", title, type, size);
            torrentsRef.add(item).addOnSuccessListener(documentReference -> {
                titleInput.setText("");
                typeInput.setText("");
                sizeInput.setText("");
                lastAddedText.setText("Hozzáadva: " + item.getName() + " (" + item.getDescription() + ", " + item.getRating() + " fájl)");
                Toast.makeText(this, "Sikeresen hozzáadva!", Toast.LENGTH_SHORT).show();
            });
        });

        // Valós idejű Firestore listener
        listener = (snapshots, e) -> {
            if (e != null) {
                Log.w(LOG_TAG, "Listen failed.", e);
                return;
            }
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                TorrentItem item = dc.getDocument().toObject(TorrentItem.class);
                item.setId(dc.getDocument().getId());
                switch (dc.getType()) {
                    case ADDED:
                        boolean exists = false;
                        for (TorrentItem t : torrentList) {
                            if (t.getId().equals(item.getId())) { exists = true; break; }
                        }
                        if (!exists) {
                            torrentList.add(item);
                            adapter.notifyItemInserted(torrentList.size() - 1);
                        }
                        break;
                    case REMOVED:
                        int idx = -1;
                        for (int i = 0; i < torrentList.size(); i++)
                            if (torrentList.get(i).getId().equals(item.getId())) idx = i;
                        if (idx != -1) {
                            torrentList.remove(idx);
                            adapter.notifyItemRemoved(idx);
                        }
                        break;
                    case MODIFIED:
                        int modIdx = -1;
                        for (int i = 0; i < torrentList.size(); i++)
                            if (torrentList.get(i).getId().equals(item.getId())) modIdx = i;
                        if (modIdx != -1) {
                            torrentList.set(modIdx, item);
                            adapter.notifyItemChanged(modIdx);
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        torrentsRef.addSnapshotListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Firestore listener automatikusan leáll, ha activity háttérbe kerül
    }

    // Notification létrehozása
    private void showNotification(String title, String text) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                Toast.makeText(this, "Értesítési engedély szükséges!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Torrent Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // AlarmManager példa
    private void setAlarm() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TorrentAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, alarmIntent); // 1 perc múlva
    }

    // Adapter a RecyclerView-hoz
    private class TorrentAdapter extends RecyclerView.Adapter<TorrentAdapter.ViewHolder> {
        private List<TorrentItem> items;
        public TorrentAdapter(List<TorrentItem> items) { this.items = items; }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.torrent_list_item, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TorrentItem item = items.get(position);
            holder.name.setText(item.getName());
            holder.desc.setText(item.getDescription());
            holder.rating.setText("Értékelés: " + item.getRating());
            holder.deleteButton.setOnClickListener(v -> {
                torrentsRef.document(item.getId()).delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(TorrentListActivity.this, "Sikeresen törölve!", Toast.LENGTH_SHORT).show();
                });
            });
        }
        @Override
        public int getItemCount() { return items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, desc, rating;
            ImageButton deleteButton;
            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.itemName);
                desc = itemView.findViewById(R.id.itemDesc);
                rating = itemView.findViewById(R.id.itemRating);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 