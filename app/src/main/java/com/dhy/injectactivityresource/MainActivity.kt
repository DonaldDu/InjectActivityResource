package com.dhy.injectactivityresource

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

//class MainActivity : InjectActivityResourceDemo() {
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}