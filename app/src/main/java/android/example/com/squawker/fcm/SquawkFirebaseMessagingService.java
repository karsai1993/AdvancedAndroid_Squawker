package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class SquawkFirebaseMessagingService extends FirebaseMessagingService {

    private final int NOTIFICATION_MAX_CHARACTER = 30;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> remoteMap = remoteMessage.getData();
        if (remoteMap.size() == 0) return;
        String author = remoteMap.get(SquawkContract.COLUMN_AUTHOR);
        String authorKey = remoteMap.get(SquawkContract.COLUMN_AUTHOR_KEY);
        String message = remoteMap.get(SquawkContract.COLUMN_MESSAGE);
        String date = remoteMap.get(SquawkContract.COLUMN_DATE);
        insertIntoDatabase(new String[]{author, authorKey, message, date});
        sendNotification(author, message);
    }

    // TODO (1) Make a new Service in the fcm package that extends from FirebaseMessagingService.
    // TODO (2) As part of the new Service - Override onMessageReceived. This method will
    // be triggered whenever a squawk is received. You can get the data from the squawk
    // message using getData(). When you send a test message, this data will include the
    // following key/value pairs:
    // test: true
    // author: Ex. "TestAccount"
    // authorKey: Ex. "key_test"
    // message: Ex. "Hello world"
    // date: Ex. 1484358455343
    // TODO (3) As part of the new Service - If there is message data, get the data using
    // the keys and do two things with it :
    // 1. Display a notification with the first 30 character of the message
    // 2. Use the content provider to insert a new message into the local database
    // Hint: You shouldn't be doing content provider operations on the main thread.
    // If you don't know how to make notifications or interact with a content provider
    // look at the notes in the classroom for help.
    private void sendNotification(String author, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        if (message.length() > NOTIFICATION_MAX_CHARACTER) {
            message = message.substring(0, NOTIFICATION_MAX_CHARACTER);
        }
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setContentText(message)
                .setContentTitle(author)
                .setSound(defaultRingtoneUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_duck);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification.build());
    }
    private void insertIntoDatabase(String [] inputs) {
        new AsyncTask<String, Void, Uri>() {
            @Override
            protected Uri doInBackground(String... inputArray) {
                if (inputArray.length == 0) return null;
                ContentValues contentValues = new ContentValues();
                contentValues.put(SquawkContract.COLUMN_AUTHOR, inputArray[0]);
                contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY, inputArray[1]);
                contentValues.put(SquawkContract.COLUMN_MESSAGE, inputArray[2]);
                contentValues.put(SquawkContract.COLUMN_DATE, inputArray[3]);
                return getContentResolver().insert(
                        SquawkProvider.SquawkMessages.CONTENT_URI,
                        contentValues
                );
            }
        }.execute(inputs);
    }
}
