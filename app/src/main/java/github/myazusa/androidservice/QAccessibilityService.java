package github.myazusa.androidservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import java.util.Stack;

import github.myazusa.qiangdandan.R;

public class QAccessibilityService extends AccessibilityService {
    private final static String TAG = QAccessibilityService.class.getName();
    private Stack<View> clickIndicatorStack = new Stack<>();
    @SuppressLint("StaticFieldLeak")
    private static QAccessibilityService instance = null;
    public static QAccessibilityService getInstance(){
        return instance;
    }

    /**
     * 调用无障碍来执行点击，只需传入x和y就可以对这个坐标进行点击
     * @param x 坐标
     * @param y 坐标
     */
    public void performClick(float x, float y) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(clickPath, 0, 100);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(strokeDescription);

        if(dispatchGesture(gestureBuilder.build(), null, null)){
            showClickIndicator(x, y);
        }
    }

    /**
     * 点击视觉效果方法
     * @param x 坐标
     * @param y 坐标
     */
    private void showClickIndicator(float x, float y) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View clickIndicator = inflater.inflate(R.layout.click_indicator_layout, null);

        // 设置悬浮窗的布局参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        // 设置点击标记的位置
        params.gravity = Gravity.TOP | Gravity.LEFT;
        // 偏移量确保圆心对准点击位置
        params.x = (int) x - 50;
        params.y = (int) y - 50;

        // TODO: 这里可能需要判空
        // 添加点击标记到屏幕
        FloatingWindowsService.getWindowManager().addView(clickIndicator, params);
        clickIndicatorStack.push(clickIndicator);

        // TODO: 这里可以反射出去作为setting
        // 延时移除点击标记
        new Handler().postDelayed(() -> {
            if (!clickIndicatorStack.isEmpty()) {
                FloatingWindowsService.getWindowManager().removeView(clickIndicatorStack.pop());
            }
        }, 500);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {
        // 停止无障碍服务
        clickIndicatorStack = null;
        Log.i(TAG,"无障碍服务中断");
    }
}
