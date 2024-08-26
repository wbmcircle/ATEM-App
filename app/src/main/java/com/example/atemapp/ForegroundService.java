package com.example.atemapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = "ForegroundService";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraManager.AvailabilityCallback availabilityCallback = new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(String cameraId) {
                if (cameraId.equals("0")) {
                    Log.d(TAG, "Front Camera " + cameraId + " is now available.");
                    sendFrontCameraStatusBroadcast(true);
                } else {
                    Log.d(TAG, "Back Camera " + cameraId + " is now available.");
                    sendBackCameraStatusBroadcast(true);
                }
            }

            @Override
            public void onCameraUnavailable(String cameraId) {
                if (cameraId.equals("0")) {
                    Log.d(TAG, "Front Camera " + cameraId + " is now unavailable.");
                    sendFrontCameraStatusBroadcast(false);
                } else {
                    Log.d(TAG, "Back Camera " + cameraId + " is now unavailable.");
                    sendBackCameraStatusBroadcast(false);
                }
            }
        };

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    boolean isAvailable = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
                    sendFrontCameraStatusBroadcast(isAvailable);
                    sendBackCameraStatusBroadcast(isAvailable);
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error accessing camera: ", e);
        }

        cameraManager.registerAvailabilityCallback(availabilityCallback, null);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean isMicAvailable = audioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION;
        sendMicStatusBroadcast(audioManager.isMicrophoneMute());
    }

    private void sendFrontCameraStatusBroadcast(boolean isAvailable) {
        Log.d(TAG, "sendFrontCameraStatusBroadcast");
        Intent intent = new Intent("com.example.atemapp.CAMERA_STATUS");
        intent.putExtra("isFrontCameraAvailable", isAvailable);
        sendBroadcast(intent);
    }

    private void sendMicStatusBroadcast(boolean isAvailable) {
        Log.d(TAG, "sendMicStatusBroadcast="+isAvailable);
        Intent intent = new Intent("com.example.atemapp.CAMERA_STATUS");
        intent.putExtra("isMicAvailable", isAvailable);
        sendBroadcast(intent);
    }

    private void sendBackCameraStatusBroadcast(boolean isAvailable) {
        Log.d(TAG, "sendBackCameraStatusBroadcast");
        Intent intent = new Intent("com.example.atemapp.CAMERA_STATUS");
        intent.putExtra("isBackCameraAvailable", isAvailable);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        String input = "Default input text";
        if (intent != null && intent.getStringExtra("inputExtra") != null) {
            input = intent.getStringExtra("inputExtra");
        }

        boolean isCameraAvailable = intent.getBooleanExtra("isCameraAvailable", true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // Update the background of circle_data_mic based on camera status
        if (isCameraAvailable) {
            notificationLayout.setInt(R.id.circle_data_mic, "setBackgroundResource", R.drawable.circle_red);
        } else {
            notificationLayout.setInt(R.id.circle_data_mic, "setBackgroundResource", R.drawable.circle_grey);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        Log.d(TAG, "Notification started");

        // Perform any long-running tasks here

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        }
    }
}