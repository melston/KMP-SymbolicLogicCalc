package com.elsoft.symlogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.elsoft.symlogic.utils.PlatformUtils

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformUtils.init(this)
    }
}