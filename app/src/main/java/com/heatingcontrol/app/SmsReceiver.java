package com.heatingcontrol.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");
        if (pdus == null) return;

        StringBuilder fullMessage = new StringBuilder();
        String sender = null;

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }
            if (smsMessage != null) {
                if (sender == null) sender = smsMessage.getOriginatingAddress();
                fullMessage.append(smsMessage.getMessageBody());
            }
        }

        String messageText = fullMessage.toString().trim();

        // Check if the message matches the expected heating format
        // Expected: "Main unit: ON/OFF XXC\n"Hab1": ON/OFF YYC"
        if (looksLikeHeatingResponse(messageText)) {
            // Broadcast to MainActivity
            Intent updateIntent = new Intent(MainActivity.SMS_RECEIVED_ACTION);
            updateIntent.putExtra("sender", sender);
            updateIntent.putExtra("message", formatHeatingMessage(messageText));
            context.sendBroadcast(updateIntent);
        }
    }

    /**
     * Checks whether the SMS matches the expected heating controller format.
     * Accepts messages containing "Main unit:" or "Hab1"
     */
    private boolean looksLikeHeatingResponse(String message) {
        String lower = message.toLowerCase();
        return lower.contains("main unit") || lower.contains("hab1");
    }

    /**
     * Cleans and formats the heating response for display.
     */
    private String formatHeatingMessage(String raw) {
        // Normalize line endings and clean up
        return raw.replace("\\n", "\n").trim();
    }
}
