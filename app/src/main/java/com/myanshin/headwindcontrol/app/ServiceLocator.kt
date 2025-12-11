package com.myanshin.headwindcontrol.app

import android.annotation.SuppressLint
import com.myanshin.headwindcontrol.app.presentation.AppViewModel

object ServiceLocator {
    @SuppressLint("StaticFieldLeak")
    var appViewModel: AppViewModel? = null
}