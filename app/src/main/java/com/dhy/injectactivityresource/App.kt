package com.dhy.injectactivityresource

import android.app.Application
import android.os.Handler
import androidx.annotation.Keep

class App : Application() {
    @Keep
    private val injectActivityResource: Handler = InjectActivityResource()
}