package com.example.it_management_system;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Get the message data
        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        // Save the notification to Firebase Database for this specific user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userNotificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId);

        String notificationId = userNotificationsRef.push().getKey();
        NotificationModel notification = new NotificationModel(title, message, false); // False indicates unread
        userNotificationsRef.child(notificationId).setValue(notification);

        // Show a local notification to the user
        sendNotification(title, message);
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification_channel_id")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.notificationbell)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(new Random().nextInt(), builder.build());
    }
}

