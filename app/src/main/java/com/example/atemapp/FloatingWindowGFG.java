package com.example.atemapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.Nullable;

public class FloatingWindowGFG extends Service {

    private ViewGroup floatView;
    private static final String TAG = "FloatingWindow";
    private int LAYOUT_TYPE;
    private WindowManager.LayoutParams floatWindowLayoutParam;
    private WindowManager windowManager;
    private ImageView circleSelfieLens;
    private ImageView circlerearlens;
    private ImageView circledatamic;
    private BroadcastReceiver cameraStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isFrontCameraAvailable = intent.getBooleanExtra("isFrontCameraAvailable", true);
            boolean isBackCameraAvailable = intent.getBooleanExtra("isBackCameraAvailable", true);
            boolean isMicAvailable = intent.getBooleanExtra("isMicAvailable", true);
            Log.d(TAG, "Here is floating window: isMicAvailable=" + isMicAvailable);
            if (isFrontCameraAvailable) {
                circleSelfieLens.setBackgroundResource(R.drawable.circle_grey);
            } 
            if (!isFrontCameraAvailable) {
                circleSelfieLens.setBackgroundResource(R.drawable.circle_yellow);
            }
            if (isBackCameraAvailable) {
                circlerearlens.setBackgroundResource(R.drawable.circle_grey);
            } 
            if (!isBackCameraAvailable) {
                circlerearlens.setBackgroundResource(R.drawable.circle_blue);
            }
            if (isMicAvailable) {
                circledatamic.setBackgroundResource(R.drawable.circle_grey);
            } else {
                circledatamic.setBackgroundResource(R.drawable.circle_red);
            }

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        
        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);
        circleSelfieLens = floatView.findViewById(R.id.circle_selfie_lens);
        circlerearlens = floatView.findViewById(R.id.circle_rear_lens);
        circledatamic = floatView.findViewById(R.id.circle_data_mic);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        floatWindowLayoutParam.gravity = Gravity.CENTER;
        
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        windowManager.addView(floatView, floatWindowLayoutParam);

        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);

                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;
                }
                return false;
            }
        });

        IntentFilter filter = new IntentFilter("com.example.atemapp.CAMERA_STATUS");
        registerReceiver(cameraStatusReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cameraStatusReceiver);
        stopSelf();
        windowManager.removeView(floatView);
    }
}
