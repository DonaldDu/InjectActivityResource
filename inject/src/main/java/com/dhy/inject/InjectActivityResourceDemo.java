package com.dhy.inject;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;

public class InjectActivityResourceDemo extends AppCompatActivity {

    private static Handler injectActivityResource;
    private Message msg;

    private void initInjectActivityResource() {
        if (injectActivityResource != null) return;
        Context application = this.getApplicationContext();
        try {
            Field injectActivityResourceField = application.getClass().getDeclaredField("injectActivityResource");
            injectActivityResourceField.setAccessible(true);
            injectActivityResource = (Handler) injectActivityResourceField.get(application);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initInjectActivityResource();
        super.onCreate(savedInstanceState);
    }

    @Override
    public Resources getResources() {
        if (injectActivityResource != null) {
            Resources resources = super.getResources();
            if (msg == null) msg = new Message();
            msg.obj = resources;
            injectActivityResource.handleMessage(msg);
            return resources;
        }
        return super.getResources();
    }
}
