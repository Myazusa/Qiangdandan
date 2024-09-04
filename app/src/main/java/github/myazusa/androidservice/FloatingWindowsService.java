package github.myazusa.androidservice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;

import github.myazusa.config.ApplicationConfig;
import github.myazusa.enums.ToggleStateEnum;
import github.myazusa.io.LogsFileIO;
import github.myazusa.qiangdandan.R;
import github.myazusa.service.ImageRecognition;
import github.myazusa.view.ToggleButton;

public class FloatingWindowsService extends Service {
    private static final String TAG = FloatingWindowsService.class.getName();
    private static WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    ToggleButton qiangdanButton;
    ToggleButton accessibilityQiangdanButton;
    private View floatingView;
    private static boolean isFloatingWindowAdded = false;
    public boolean isQiangdanStart = false;
    private Boolean clickBlock = ApplicationConfig.getInstance()
            .getPreferences().getBoolean("clickBlock",true);
    private Boolean imageBlock = ApplicationConfig.getInstance()
            .getPreferences().getBoolean("imageBlock",true);
    private Integer recognizeLateDelayMillis = null;
    private Bitmap cacheBitmap = null;
    private org.opencv.core.Point cachePoint = null;
    CaptureService captureService = null;
    private final IBinder floatingWindowsServiceBinder = new FloatingWindowsService.FloatingWindowsServiceBinder();
    public class FloatingWindowsServiceBinder extends Binder {
        FloatingWindowsService getService() {
            return FloatingWindowsService.this;
        }
    }

    public void receiveData(Bundle bundle) {
        Bitmap bitmap = bundle.getParcelable("bitmap_key");
        Optional.ofNullable(bitmap).ifPresent(i->{
            if(isQiangdanStart){
                // 调用opencv来识别
                Log.d(TAG, "收到截屏服务的消息");
                if(cacheBitmap == null){
                    cacheBitmap = bitmap;
                }

                if(!bitmap.equals(cacheBitmap) && imageBlock){
                    Log.i(TAG, "图像已变化，开始识别");
                    cacheBitmap = bitmap;
                    org.opencv.core.Point point = ImageRecognition.recognizeButton(QAccessibilityService.getInstance().getApplicationContext(), bitmap, R.drawable.jiedan_button);
                    if(point != null){
                        if(cachePoint == null){
                            cachePoint = point;
                            tryClick(point);
                        }
                        if (!cachePoint.equals(point) && clickBlock) {
                            Log.i(TAG, "坐标已变化，开始点击");
                            tryClick(point);
                        }
                    }
                }
            }
        });
    }
    private void tryClick(org.opencv.core.Point point){
        QAccessibilityService.getInstance().performClick((float) point.x, (float) point.y);
            if(recognizeLateDelayMillis == null){
                recognizeLateDelayMillis =  Integer.parseInt(ApplicationConfig.getInstance()
                        .getPreferences().getString("recognizeLateDelayMillis","0"));
            }
            if(recognizeLateDelayMillis != 0){
                try {
                    Thread.sleep(recognizeLateDelayMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    private void sendData(){
        Bundle bundle = new Bundle();
        bundle.putString("stop_key", "stop");
        if(captureService != null){
            captureService.receiveData(bundle);
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CaptureService.CaptureServiceBinder binder = (CaptureService.CaptureServiceBinder) service;
            captureService = binder.getService();
            Log.d(TAG,"FloatingWindowsService绑定截屏服务成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            captureService = null;
            Log.d(TAG,"FloatingWindowsService解绑截屏服务成功");
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isFloatingWindowAdded){
            initFloatingView();
            createFloatingButton();
            createCloseSwitch();
            createQiangdanSwitch();
            createAccessibilityQiangdanSwitch();
            // 如果再次创建会更新无障碍服务的值
            QAccessibilityService.updateLockingLevelToggleState();
            // 绑定两个service
            bindService(new Intent(this, CaptureService.class), connection, Context.BIND_AUTO_CREATE);
            return START_STICKY;
        }else {
            return START_NOT_STICKY;
        }
    }

    /**
     * 初始化悬浮窗本体
     */
    private void initFloatingView() {
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        Display display = windowManager.getDefaultDisplay();
        display.getSize(new Point());
        layoutParams = getLayoutParams();

        // 使用ContextThemeWrapper将Activity的主题应用到悬浮窗视图
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.Theme_Qiangdandan);
        // 通过ContextThemeWrapper获取LayoutInflater
        LayoutInflater inflater = (LayoutInflater) contextThemeWrapper.getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.view_floating_windows, null);
        if (floatingView != null) {
            windowManager.addView(floatingView, layoutParams);
        }
        isFloatingWindowAdded = true;
    }

    /**
     * 创建抢单按钮，用于识图抢单
     */
    private void createQiangdanSwitch(){
        qiangdanButton = floatingView.findViewById(R.id.qiangdanButton);
        qiangdanButton.setOnClickListener(l->{
            if(qiangdanButton.isButtonState() == ToggleStateEnum.Default){
                qiangdanButton.setButtonToTriggered();
                qiangdanButton.setIconResource(R.drawable.button_pause_2__streamline_core);
                captureService.startCapture();
                captureService.resumeTask();
                isQiangdanStart = true;
            } else if (qiangdanButton.isButtonState() == ToggleStateEnum.Triggered) {
                qiangdanButton.setButtonToDefault();
                qiangdanButton.setIconResource(R.drawable.button_play__streamline_core);
                captureService.interruptTask();
                captureService.stopCapture();
                isQiangdanStart = false;
            }
        });
    }
    /**
     *
     */
    private void createAccessibilityQiangdanSwitch(){
        accessibilityQiangdanButton = floatingView.findViewById(R.id.accessibilityQiangdanButton);
        accessibilityQiangdanButton.setOnClickListener(l->{
            if(accessibilityQiangdanButton.isButtonState() == ToggleStateEnum.Default){
                accessibilityQiangdanButton.setButtonToTriggered();
                QAccessibilityService.setIsAccessibilityEventEnable(true);
            } else if (accessibilityQiangdanButton.isButtonState() == ToggleStateEnum.Triggered) {
                accessibilityQiangdanButton.setButtonToDefault();
                QAccessibilityService.setIsAccessibilityEventEnable(false);
            }
        });
    }

    /**
     * 创建悬浮按钮，用于显示隐藏其他按钮
     */
    private void createFloatingButton() {
        ImageView windowFloatButton = floatingView.findViewById(R.id.windowFloatButton);
        View view = floatingView.findViewById(R.id.functionButtonLayout);
        view.setVisibility(View.GONE);
        windowFloatButton.setOnClickListener(v -> {
            if(view.getVisibility() == View.GONE){
                view.setVisibility(View.VISIBLE);
            }else {
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 创建关闭按钮，用于关闭悬浮窗以及无障碍服务
     */
    private void createCloseSwitch(){
        ToggleButton closeButton = floatingView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(l -> {
            if(closeButton.isButtonState() == ToggleStateEnum.Default){
                onDestroy();
            }
        });
    }

    @NonNull
    private static WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams;
        // 设置悬浮窗参数
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
        }else {
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
        }
        // 设置悬浮窗初始位置为贴左居中
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        return layoutParams;
    }

    public static WindowManager getWindowManager() {
        return windowManager;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return floatingWindowsServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除悬浮窗
        windowManager.removeView(floatingView);
        // 发送消息，让截屏服务也停止
        sendData();
        // 停止此服务
        if (captureService != null){
            unbindService(connection);
            captureService=null;
            Log.i(TAG,"FloatingWindowsService已和截屏服务断开连接");
        }
        QAccessibilityService.getInstance().onInterrupt();
        isFloatingWindowAdded = false;
        Log.i(TAG,"无障碍服务已停止");
        Log.i(TAG,"----悬浮窗服务已释放完毕----");

        // 写入配置
        SharedPreferences.Editor edit = ApplicationConfig.getInstance().getPreferences().edit();
        edit.putString("minElapsedTime",String.valueOf(ImageRecognition.minElapsedTime));
        edit.putString("maxElapsedTime",String.valueOf(ImageRecognition.maxElapsedTime));
        edit.putStringSet("elapsedTimeQueue",ImageRecognition.elapsedTimeQueue.convertToStringSet());
        edit.apply();

        // 写入日志
        LogsFileIO.writeLogsToExternal(this);
    }
}
