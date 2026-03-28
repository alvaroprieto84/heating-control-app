package com.heatingcontrol.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST = 101;
    private static final String PREFS_NAME = "HeatingControlPrefs";
    private static final String PREF_PHONE = "saved_phone";
    public static final String SMS_RECEIVED_ACTION = "com.heatingcontrol.SMS_RECEIVED";

    private EditText editPhoneNumber;
    private TextView textStatus;
    private Button btnOn, btnOff, btnStatus;

    // BroadcastReceiver to receive parsed SMS from SmsReceiver
    private final BroadcastReceiver smsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender = intent.getStringExtra("sender");
            String message = intent.getStringExtra("message");

            String savedPhone = editPhoneNumber.getText().toString().trim();

            // Only show message if it comes from the configured number
            if (sender != null && normalizePhone(sender).contains(normalizePhone(savedPhone))
                    || (savedPhone != null && normalizePhone(savedPhone).contains(normalizePhone(sender != null ? sender : "")))) {
                displayStatusMessage(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textStatus = findViewById(R.id.textStatus);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        btnStatus = findViewById(R.id.btnStatus);

        // Load saved phone number
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedPhone = prefs.getString(PREF_PHONE, "");
        editPhoneNumber.setText(savedPhone);

        // Save phone number whenever it changes
        editPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) savePhoneNumber();
        });

        btnOn.setOnClickListener(v -> {
            savePhoneNumber();
            sendSms("#01#");
        });

        btnOff.setOnClickListener(v -> {
            savePhoneNumber();
            sendSms("#02#");
        });

        btnStatus.setOnClickListener(v -> {
            savePhoneNumber();
            sendSms("#07#");
        });

        // Request SMS permissions
        checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register receiver for incoming SMS updates
        IntentFilter filter = new IntentFilter(SMS_RECEIVED_ACTION);
        ContextCompat.registerReceiver(this, smsUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsUpdateReceiver);
        savePhoneNumber();
    }

    private void savePhoneNumber() {
        String phone = editPhoneNumber.getText().toString().trim();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PREF_PHONE, phone).apply();
    }

    private void sendSms(String message) {
        String phoneNumber = editPhoneNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasSmsSendPermission()) {
            Toast.makeText(this, "SMS permission required", Toast.LENGTH_SHORT).show();
            checkAndRequestPermissions();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Sent: " + message, Toast.LENGTH_SHORT).show();
            textStatus.setText("Waiting for response...");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void displayStatusMessage(String message) {
        runOnUiThread(() -> textStatus.setText(message));
    }

    private String normalizePhone(String phone) {
        // Strip spaces, dashes, parentheses for comparison
        return phone.replaceAll("[\\s\\-().]", "");
    }

    // ---- Permissions ----

    private boolean hasSmsSendPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, SMS_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this,
                        "SMS permissions are required for this app to work.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
