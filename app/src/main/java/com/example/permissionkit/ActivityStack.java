package com.example.permissionkit;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.Stack;

/**
 * Activity 管理栈
 */
public class ActivityStack {

    private final Object lock = new Object();
    private final Stack<Activity> stack = new Stack<>();
    private final static ActivityStack sInstance = new ActivityStack();

    private ActivityStack() {
    }

    /**
     * 单例
     */
    public static ActivityStack getInstance() {
        return sInstance;
    }

    /**
     * 压入堆栈顶部
     */
    public <A extends Activity> void push(@NonNull A activity) {
        synchronized (lock) {
            stack.push(activity);
        }
    }

    /**
     * 获取当前Activity(最后一个入栈的)
     */
    public Activity curr() {
        synchronized (lock) {
            try {
                return stack.lastElement();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * 移除堆栈中的Activity，并将Activity Finish
     */
    public void pop(Activity activity) {
        synchronized (lock) {
            pop(activity, true);
        }
    }

    /**
     * 移除堆栈中的Activity
     */
    public void pop(Activity activity, boolean finish) {
        synchronized (lock) {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && finish) {
                activity.finish();
            }
            remove(activity);
        }
    }

    /**
     * 移除栈中的所有Activity
     */
    public void popAll() {
        synchronized (lock) {
            if (stack.isEmpty()) {
                return;
            }

            try {
                Iterator<Activity> it = stack.iterator();

                while (it.hasNext()) {
                    Activity activity = it.next();

                    if (activity != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            if (!activity.isFinishing() && !activity.isDestroyed()) {
                                activity.finish();
                            }
                        } else {
                            if (!activity.isFinishing()) {
                                activity.finish();
                            }
                        }
                    }

                    it.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 移除栈
     */
    public void remove(Activity activity) {
        synchronized (lock) {
            if (stack.empty() || !stack.contains(activity)) {
                return;
            }

            try {
                stack.remove(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回指定位置的Activity
     */
    public Activity get(int index) {
        synchronized (lock) {
            if (index < 0) {
                return null;
            }

            if (!stack.empty() && index < stack.size()) {
                return stack.get(index);
            }

            return null;
        }
    }


    /**
     * 返回前一个 Activity
     */
    public Activity pre() {
        synchronized (lock) {
            final int index = indexOf();
            return get(index - 1);
        }
    }

    /**
     * 当前Activity索引位置
     */
    public int indexOf() {
        synchronized (lock) {
            Activity activity = curr();
            if (activity == null) {
                return -1;
            }

            if (!stack.empty() && stack.contains(activity)) {
                return stack.indexOf(activity);
            }

            return -1;
        }
    }

    public <A extends Activity> int indexOf(@NonNull A activity) {
        synchronized (lock) {
            if (!stack.empty() && stack.contains(activity)) {
                return stack.indexOf(activity);
            }

            return -1;
        }
    }

}
