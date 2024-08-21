package github.myazusa.qiangdandan;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import org.opencv.android.OpenCVLoader;

import java.util.List;

import github.myazusa.androidservice.CaptureService;
import github.myazusa.androidservice.FloatingWindowsService;
import github.myazusa.qiangdandan.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String _TAG = MainActivity.class.getName();
    private static boolean isOpencvReady = false;
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 1410;
    private Intent screenCaptureIntent = null;
    private ActivityMainBinding _binding;

    static {
        if (!OpenCVLoader.initLocal()) {
            Log.w(_TAG, "opencv加载失败");
        }else {
            isOpencvReady = true;
            Log.w(_TAG, "opencv加载成功");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if(!isOpencvReady){
            Toast.makeText(this, "opencv未能正常加载", Toast.LENGTH_SHORT).show();
            exit();
        }
        createStartFloatingWindowsButtonSwitch();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 创建打开悬浮窗的按钮
     */
    private void createStartFloatingWindowsButtonSwitch(){
        AppCompatImageButton startFloatingWindowsButton = findViewById(R.id.startFloatingWindowsButton);
        startFloatingWindowsButton.setOnClickListener(l-> {
            if(checkOverlayPermission() && checkAccessibilityServicePermission()){
                // 如果有前两个权限就申请截屏权限
                MediaProjectionManager mediaProjectionManager =
                        (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent, SCREEN_CAPTURE_REQUEST_CODE);

            }else {
                // 否则去设置开启
                requestOverlayPermission();
            }
        });
    }

    /**
     * 检查是否有悬浮窗权限
     * @return 有则true
     */
    private boolean checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "悬浮窗权限未获取", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 请求悬浮窗权限
     */
    private void requestOverlayPermission() {
        if(!checkOverlayPermission()){
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }else {
            requestAccessibilityPermission();
        }
    }

    /**
     * 检查是否有无障碍权限
     * @return 有则true
     */
    private boolean checkAccessibilityServicePermission() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledAccessibilityServiceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
        for (AccessibilityServiceInfo info : enabledAccessibilityServiceList) {
            if (info.getResolveInfo().serviceInfo.packageName.equals(getPackageName())) {
                return true;
            }
        }
        Log.i(_TAG, "无障碍权限未获取");
        return false;
    }

    /**
     * 请求无障碍权限
     */
    private void requestAccessibilityPermission(){
        if (!checkAccessibilityServicePermission()) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    /**
     * 检查是否有录屏权限
     * @return 有则true
     */
//    private boolean checkMediaProjectionPermission() {
//        MediaProjectionManager mediaProjectionManager =
//                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//        return mediaProjectionManager != null;
//    }

    /**
     * 请求录屏权限
     */
//    private void requestMediaProjectionPermission(){
//        if (!checkMediaProjectionPermission()) {
//            startActivity(((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent());
//        }else {
//            requestAccessibilityPermission();
//        }
//    }

    /**
     * 退出所有Activity，但不会关闭已启动的service
     */
    private void exit(){
        finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            // 如果截屏权限也ok就开启两个服务
            Intent captureServiceIntent = new Intent(getApplicationContext(), CaptureService.class);
            captureServiceIntent.putExtra("resultCode", resultCode).putExtra("data", data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(captureServiceIntent);
            }else {
                startService(captureServiceIntent);
            }
            startService(new Intent(getApplicationContext(), FloatingWindowsService.class));
            Log.d(_TAG,"全部权限已获取，开启悬浮窗");
            finishAffinity();
        }else {
            Toast.makeText(this, "截屏权限未获取", Toast.LENGTH_SHORT).show();
        }
    }
}
