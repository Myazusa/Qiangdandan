package github.myazusa.androidservice;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;

public class CaptureService extends Service {
    private static final String TAG = CaptureService.class.getName();
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler imageHandler = new Handler(Looper.getMainLooper());
    private boolean imageAvailable = false;
    private Runnable captureTask;
    private boolean isTaskRunning = false;
    private final Handler captureTaskHandler = new Handler(Looper.getMainLooper());
    private int delayMillis = 1000;
    FloatingWindowsService floatingWindowsService = null;
    private final IBinder captureServiceBinder = new CaptureServiceBinder();

    public class CaptureServiceBinder extends Binder {
        CaptureService getService() {
            return CaptureService.this;
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            FloatingWindowsService.FloatingWindowsServiceBinder binder = (FloatingWindowsService.FloatingWindowsServiceBinder) service;
            floatingWindowsService = binder.getService();
            Log.d(TAG,"CaptureService绑定悬浮窗服务成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"CaptureService解绑悬浮窗服务成功");
        }
    };

    private void sendData(Bitmap bitmap){
        Bundle bundle = new Bundle();
        bundle.putParcelable("bitmap_key",bitmap);
        if (floatingWindowsService != null) {
            floatingWindowsService.receiveData(bundle);
        }
    }
    public void receiveData(Bundle bundle) {
        String massage = bundle.getString("stop_key");
        if(massage != null && massage.equals("stop")){
            stopSelf();
        }
        Log.d(TAG, "收到悬浮窗服务停止消息");
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        // 声明前台服务
        Notification notification = createNotification();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_OK);
        Intent data = intent.getParcelableExtra("data");
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        // 绑定两个service
        bindService(new Intent(this, FloatingWindowsService.class), connection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }
    private void startCaptureTask() {
        if (captureTask == null){
            captureTask = new Runnable() {
                @Override
                public void run() {
                    if (imageAvailable){
                        Log.d(TAG, "已截图");
                        Image image = imageReader.acquireLatestImage();
                        if (image != null) {
                            Bitmap bitmap = processImage(image);
                            image.close();
                            // 发送bitmap给悬浮窗服务
                            sendData(bitmap);
                            imageAvailable = false;
                        }
                    }
                    captureTaskHandler.postDelayed(this, delayMillis);
                }
            };
        }
        if (!isTaskRunning && floatingWindowsService != null) {
            captureTaskHandler.postDelayed(captureTask, delayMillis);
            isTaskRunning = true;
            Log.i(TAG, "截图任务已开始");
        }
        // TODO：要处理floatingWindowsService为空的情况，为空就异常退出程序
    }

    /**
     * 中断定时任务
     */
    public void interruptTask() {
        if (isTaskRunning) {
            captureTaskHandler.removeCallbacks(captureTask);
            isTaskRunning = false;
            Log.i(TAG, "截图任务已中断");
        }
    }

    /**
     * 继续定时任务
     */
    public void resumeTask() {
        if(floatingWindowsService != null){
            if(captureTask ==null){
                startCaptureTask();
            }else {
                if (!isTaskRunning) {
                    captureTaskHandler.postDelayed(captureTask, delayMillis);
                    isTaskRunning = true;
                    Log.i(TAG, "截图任务已继续");
                }
            }
        }
        // TODO：要处理floatingWindowsService为空的情况，为空就异常退出程序
    }

    /**
     * 停止定时任务
     */
    private void stopTask() {
        interruptTask();
        captureTask = null;
        Log.i(TAG, "截图任务已停止");
    }
    private void initImageReader(DisplayMetrics metrics){
        if (imageReader == null){
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                imageAvailable = true;
            }, imageHandler);
        }
    }

    public void startCapture() {
        DisplayMetrics metrics = new DisplayMetrics();
        initImageReader(metrics);
        virtualDisplay = mediaProjection.createVirtualDisplay("抢单单截屏",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
    }
    public void stopCapture(){
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private Bitmap processImage(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放定时截屏任务
        stopTask();
        // 停止截图
        stopCapture();
        // 释放服务连接
        if (floatingWindowsService != null) {
            unbindService(connection);
            floatingWindowsService = null;
            Log.i(TAG,"CaptureService已和悬浮窗服务断开连接");
        }
        // 释放mediaProjection服务
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
            Log.i(TAG,"MediaProjection服务已释放");
        }
        Log.i(TAG,"----截图服务已释放完毕----");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return captureServiceBinder;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private Notification createNotification() {
        NotificationChannel channel = null;
        channel = new NotificationChannel("MediaProjectionService", "Screen Capture", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        return new Notification.Builder(this, "MediaProjectionService")
                    .setContentTitle("抢单单")
                    .setContentText("正在截图")
                    .build();

    }
}
