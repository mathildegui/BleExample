package com.example.lesdentsbleues.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.IdRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit

fun <T> Any.cast(): T = this as T

fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permissionType) ==
            PackageManager.PERMISSION_GRANTED
}

fun Activity.requestPermission(permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}

fun FragmentActivity.commit(@IdRes id: Int, fragment: Fragment) {
    supportFragmentManager.commit {
        setReorderingAllowed(true)
        addToBackStack(null)
        replace(id, fragment)
    }
}
