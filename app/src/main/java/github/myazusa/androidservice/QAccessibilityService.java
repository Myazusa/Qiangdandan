package github.myazusa.androidservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
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
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Optional;
import java.util.Stack;

import github.myazusa.config.ApplicationConfig;
import github.myazusa.qiangdandan.R;
import github.myazusa.util.TraverseNodeUtil;

public class QAccessibilityService extends AccessibilityService {
    private final static String TAG = QAccessibilityService.class.getName();
    private Stack<View> clickIndicatorStack = new Stack<>();
    private static boolean isAccessibilityEventEnable = false;
    private static int lockingLevelToggleState = 1;
    private static AccessibilityNodeInfo rootInActiveWindow;
    private Integer clickIndicatorStackDelayMillis = null;
    @SuppressLint("StaticFieldLeak")
    private static QAccessibilityService instance = null;
    public static QAccessibilityService getInstance(){
        return instance;
    }
    public static void updateLockingLevelToggleState(){
        lockingLevelToggleState = Integer.parseInt(ApplicationConfig.getInstance().getPreferences().getString("lockingLevel","1"));
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
        if(clickIndicatorStackDelayMillis == null){
            clickIndicatorStackDelayMillis = Integer.parseInt(ApplicationConfig.getInstance()
                    .getPreferences().getString("clickIndicatorDelayMillis","800"));
        }
        if (clickIndicatorStackDelayMillis > 0){
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

            // 添加点击标记到屏幕
            FloatingWindowsService.getWindowManager().addView(clickIndicator, params);
            clickIndicatorStack.push(clickIndicator);


            // 延时移除点击标记
            new Handler().postDelayed(() -> {
                if (!clickIndicatorStack.isEmpty()) {
                    FloatingWindowsService.getWindowManager().removeView(clickIndicatorStack.pop());
                }
            }, clickIndicatorStackDelayMillis);
        }
    }

    public static void setIsAccessibilityEventEnable(boolean isAccessibilityEventEnable) {
        QAccessibilityService.isAccessibilityEventEnable = isAccessibilityEventEnable;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int changeType = event.getEventType();
        if(changeType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            rootInActiveWindow = getRootInActiveWindow();
        }
        if (isAccessibilityEventEnable){
            AccessibilityNodeInfo accessibilityNodeInfo = event.getSource();

            switch (lockingLevelToggleState){
                case 0:
                    Log.i(TAG,"物件锁定等级低");
                    if (changeType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && accessibilityNodeInfo != null){
                        // 并且必须是节点发生变化的事件
                        if((changeType & AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE) != 0){
                            accessibilityAction(accessibilityNodeInfo);
                        }
                    }
                    break;
                case 1:
                    Log.i(TAG,"物件锁定等级中");
                    if (changeType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && accessibilityNodeInfo != null){
                        accessibilityAction(accessibilityNodeInfo);
                    }
                    break;
                case 2:
                    Log.i(TAG,"物件锁定等级高");
                    if (changeType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && rootInActiveWindow != null){
                        // 传入根节点
                        accessibilityAction(rootInActiveWindow);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void accessibilityAction(AccessibilityNodeInfo accessibilityNodeInfo){
        List<AccessibilityNodeInfo> infos = accessibilityNodeInfo.findAccessibilityNodeInfosByText("接单");
        if (infos != null){
            for (AccessibilityNodeInfo info :infos) {
                if (info != null){
                    // 自身是否可以点击
                    Optional.ofNullable(TraverseNodeUtil.traverseSelf(info)).ifPresent(i->{
                        i.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i(TAG,"点击自己成功");
                    });
                    // 否则向上查找可以点击的父节点
                    Optional.ofNullable(TraverseNodeUtil.traverseParent(info)).ifPresent(i->{
                        i.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i(TAG,"点击父节点成功");
                    });
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // 停止无障碍服务
        clickIndicatorStack = null;
        Log.i(TAG,"无障碍服务中断");
    }
}
