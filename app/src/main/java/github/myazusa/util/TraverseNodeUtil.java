package github.myazusa.util;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TraverseNodeUtil {
    private static final String _TAG = TraverseNodeUtil.class.getName();
    private static List<AccessibilityNodeInfo> nodeInfoList = new ArrayList<>();

    /**
     * 验证节点自身是否可以点击
     * @param node 传入节点
     * @return 可点击就返回节点，否则null
     */
    public static AccessibilityNodeInfo traverseSelf(AccessibilityNodeInfo node){
        if(node != null){
            if(node.isClickable()){
                return node;
            }else{
                return null;
            }
        }
        return null;
    }

    /**
     * 找出所有可点击且属于ViewGroup的子节点，用的广度
     * @param node 传入节点
     * @return 返回是ViewGroup且可点击的节点的集合
     */
    public static List<AccessibilityNodeInfo> traverseChilds(AccessibilityNodeInfo node){
        if(node != null){
            Queue<AccessibilityNodeInfo> queue = new LinkedList<>();
            queue.offer(node);
            while (!queue.isEmpty()) {
                AccessibilityNodeInfo currentNode = queue.poll();
                if(currentNode.getClassName().toString().contains("ViewGroup") && currentNode.isClickable()){
                    Log.i(_TAG,"找到符合条件的ViewGroup");
                    nodeInfoList.add(currentNode);
                }
                List<AccessibilityNodeInfo> children = new ArrayList<>();
                for (int i = 0; i < currentNode.getChildCount(); i++) {
                    children.add(currentNode.getChild(i));
                }
                for (AccessibilityNodeInfo child : children) {
                    queue.offer(child);
                }
            }
            // 回收节点资源，避免内存泄漏
            if (node.isAccessibilityFocused() && !node.isImportantForAccessibility()) {
                node.recycle();
            }
        }
        return nodeInfoList;
    }

    /**
     * 向父级查找可点击且是ViewGroup的父节点，用的递归
     * @param node 传入节点
     * @return 返回找到的第一个符合条件的父节点，否则返回null
     */
    public static AccessibilityNodeInfo traverseParent(AccessibilityNodeInfo node){
        if(traverseSelf(node) != null){
            return node;
        }
        AccessibilityNodeInfo parent = node.getParent();
        if(parent != null){
            if(parent.getClassName().toString().contains("ViewGroup") && parent.isClickable()){
                Log.i(_TAG,"找到符合条件的ViewGroup");
                return parent;
            }
            if(parent.getParent() == null){
                return null;
            }else {
                return traverseParent(parent.getParent());
            }
        }
        else {
            return null;
        }
    }

    /**
     * 向父级查找是ViewGroup的父节点，用的递归
     * @param node 传入节点
     * @return 返回第一个符合要求的父节点
     */
    public static AccessibilityNodeInfo traverseUnClickableParent(AccessibilityNodeInfo node){
        if(traverseSelf(node) != null){
            return node;
        }
        AccessibilityNodeInfo parent = node.getParent();
        if(parent != null){
            if(parent.getClassName().toString().contains("ViewGroup")){
                Log.i(_TAG,"找到符合条件的ViewGroup");
                return parent;
            }
            if(parent.getParent() == null){
                return null;
            }else {
                return traverseParent(parent.getParent());
            }
        }
        else {
            return null;
        }
    }

    /**
     * 遍历打印该节点下的所有子节点，用的递归
     * @param node
     * @param childCount
     */
    public static void traverseNodes(AccessibilityNodeInfo node, int childCount) {
        if (node == null) {
            return;
        }
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < childCount; i++) {
            prefix.append("-");
        }

        Log.i(_TAG, prefix + "Class: " + node.getClassName());
        Log.i(_TAG, prefix + "PackageName: " + node.getPackageName());
        Log.i(_TAG, prefix + "Text: " + node.getText());
        Log.i(_TAG, prefix + "ContentDescription: " + node.getContentDescription());
        Log.i(_TAG, prefix + "IsClickable: " + node.isClickable());
        Log.i(_TAG, prefix + "ViewIdResourceName:" + node.getViewIdResourceName());

        // 递归遍历子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            traverseNodes(child, childCount + 1);
        }

        // 回收节点资源，避免内存泄漏
        if (node.isAccessibilityFocused() && !node.isImportantForAccessibility()) {
            node.recycle();
        }
    }

    public static void clearList(){
        nodeInfoList.clear();
    }

    public static void traverseAllWindowNodes(AccessibilityService service) {
        List<AccessibilityWindowInfo> serviceWindows = service.getWindows();
        Log.i(_TAG, "找到共计"+serviceWindows.size() + "个窗口");
        for (AccessibilityWindowInfo windowInfo: serviceWindows) {
            AccessibilityNodeInfo root = windowInfo.getRoot();
            if(root != null){
                traverseNodes(windowInfo.getRoot(),windowInfo.getChildCount());
            }
        }
    }
}
