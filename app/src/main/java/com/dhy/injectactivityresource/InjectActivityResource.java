package com.dhy.injectactivityresource;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import static java.sql.DriverManager.println;

@SuppressWarnings("deprecation")
public class InjectActivityResource extends Handler {
    /**
     * 这个方法调用非常频繁，所以建议用JAVA写，以保证没有多余的代码。
     */
    @Override
    public void handleMessage(@NonNull Message msg) {
        Resources resources = (Resources) msg.obj;
        println("injectActivityResource " + resources.hashCode());
    }
}
