package github.myazusa.qiangdandan;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.opencv.android.OpenCVLoader;

import java.util.List;

import github.myazusa.androidservice.CaptureService;
import github.myazusa.androidservice.FloatingWindowsService;
import github.myazusa.config.ApplicationConfig;
import github.myazusa.io.LogsFileIO;
import github.myazusa.qiangdandan.databinding.ActivityMainBinding;
import github.myazusa.util.FragmentUtil;
import github.myazusa.util.KeyboardUtil;
import github.myazusa.view.LogsFragment;
import github.myazusa.view.OptionsFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static boolean isOpencvReady = false;
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 1410;
    private ActivityMainBinding _binding;
    private Fragment logsFragment = null;
    private Fragment optionsFragment = null;
    private Fragment helpFragment = null;

    static {
        if (!OpenCVLoader.initLocal()) {
            Log.w(TAG, "opencv加载失败");
        }else {
            isOpencvReady = true;
            Log.w(TAG, "opencv加载成功");
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

        // 设置config初始值
        ApplicationConfig.initApplicationConfig(this);
        ApplicationConfig.
                getInstance().
                setDefaultPreferences(this,R.xml.default_preferences);

        createGlobalExceptionHandler();
        createStartFloatingWindowsButtonSwitch();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if(!isOpencvReady){
            Toast.makeText(this, "opencv未能正常加载", Toast.LENGTH_SHORT).show();
            exit();
        }

        checkExternalPermission();
        createLogsFragmentButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    public void switchToFragment(String fragmentName){
        if("logsFragment".equals(fragmentName)){
            FragmentUtil.switchFragment(getSupportFragmentManager(),logsFragment);
        }
        if("optionsFragment".equals(fragmentName)){
            FragmentUtil.switchFragment(getSupportFragmentManager(), optionsFragment);
        }
        if("helpFragment".equals(fragmentName)){
            FragmentUtil.switchFragment(getSupportFragmentManager(),helpFragment);
        }
        if(fragmentName == null){
            FragmentUtil.switchFragment(getSupportFragmentManager(),null);
        }
    }

    private void createLogsFragmentButton(){
        MaterialButton logsFragmentButton = findViewById(R.id.logsFragmentButton);
        MaterialButton optionsFragmentButton = findViewById(R.id.optionsFragmentButton);
        logsFragmentButton.setOnClickListener(l->{
            if (logsFragment == null){
                logsFragment = new LogsFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentSlot, logsFragment).hide(logsFragment).commit();
            }
            switchToFragment("logsFragment");
        });
        optionsFragmentButton.setOnClickListener(l->{
            if (optionsFragment == null){
                optionsFragment = new OptionsFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentSlot, optionsFragment).hide(optionsFragment).commit();
            }
            switchToFragment("optionsFragment");
        });
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
    private void checkExternalPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
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
        Log.i(TAG, "无障碍权限未获取");
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
            Log.d(TAG,"全部权限已获取，开启悬浮窗");
            finishAffinity();
        }else {
            Toast.makeText(this, "截屏权限未获取", Toast.LENGTH_SHORT).show();
        }
    }
    private void createGlobalExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            // 处理未捕获的异常
            Log.e(TAG, "出现未捕获的异常: " + t.getName(), e);
            loggingException();
        });
    }
    private void loggingException() {
        LogsFileIO.writeLogsToExternal(this);
    }

    /**
     * 清除焦点方法
     * @param ev 触摸事件
     *
     * @return 清除成功true或失败false
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (v != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE)
                && v instanceof EditText && !v.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x >= v.getRight() || y < v.getTop() || y > v.getBottom()) {
                KeyboardUtil.hideKeyboard(this,v);
                v.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
