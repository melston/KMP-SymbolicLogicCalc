package com.elsoft.symlogic.utils

import android.content.Context
import java.lang.ref.WeakReference

actual object PlatformUtils {
    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context)
    }

    private val context: Context
        get() = contextRef?.get() ?: throw IllegalStateException("PlatformUtils not initialized with Context")


    actual fun getProblemSetDir(): String {
        return context.filesDir.path + "/problems"
    }
}