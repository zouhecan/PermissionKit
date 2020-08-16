package com.example.permissionkit

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.permissionkit.permission.IPermissionCallBack
import com.example.permissionkit.permission.PermissionConstants
import com.example.permissionkit.permission.PermissionUtil

class MainActivity : AppCompatActivity() {
    private val logTag = MainActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())
        setContentView(R.layout.activity_main)
        Thread.currentThread().uncaughtExceptionHandler =
            Thread.UncaughtExceptionHandler { _: Thread?, e: Throwable ->
                Log.e("CRASH", "" + e.message)
                e.printStackTrace()
            }
    }

    /**
     * 请求单个权限
     */
    fun requestStoragePermission(view: View) {
        PermissionUtil.requestPermissions(
            this,
            PermissionConstants.PermissionType.WRITE_EXTERNAL_STORAGE,
            PermissionConstants.RationaleType.STORAGE,
            IPermissionCallBack { grantedAll, _ ->
                if (grantedAll) {
                    //to do your following thing with storage permission
                    Log.d(logTag, "The STORAGE permission was granted!")
                } else {
                    Log.d(logTag, "The STORAGE permission was denied!")
                }
            })
    }

    /**
     * 同时请求多个不同权限
     */
    fun requestPhoneAndContactPermissionConcurrently(view: View) {
        PermissionUtil.requestPermissions(
            this,
            PermissionConstants.PermissionType.READ_PHONE_STATE or PermissionConstants.PermissionType.READ_CONTACTS,
            PermissionConstants.RationaleType.PHONE or PermissionConstants.RationaleType.CONTACTS,
            IPermissionCallBack { grantedAll, _ ->
                if (grantedAll) {
                    //to do your following thing with phone and contact permission
                    Log.d(logTag, "The PHONE and CONTACT permission were all granted!")
                } else {
                    Log.d(logTag, "The PHONE and CONTACT permission were denied!")
                }
            })
    }

    /**
     * 重复请求相同的权限
     */
    fun requestLocationPermissionRepeatedly(view: View) {
        PermissionUtil.requestLocationPermission(this, IPermissionCallBack { grantedAll, _ ->
            if (grantedAll) {
                //to do your following thing with location_1 permission
                Log.d(logTag, "The LOCATION_1 permission was granted!")
            } else {
                Log.d(logTag, "The LOCATION_1 permission was denied!")
            }
        })
        PermissionUtil.requestLocationPermission(this, IPermissionCallBack { grantedAll, _ ->
            if (grantedAll) {
                //to do your following thing with location_2 permission
                Log.d(logTag, "The LOCATION_2 permission was granted!")
            } else {
                Log.d(logTag, "The LOCATION_2 permission was denied!")
            }
        })
    }

    /**
     * 连续请求多个不同的权限
     */
    fun requestAudioAndCameraPermissionSerially(view: View) {
        PermissionUtil.requestPermissions(this,
            PermissionConstants.PermissionType.RECORD_AUDIO,
            PermissionConstants.RationaleType.MICROPHONE,
            IPermissionCallBack { grantedAll, _ ->
                if (grantedAll) {
                    //to do your following thing with audio permission
                    Log.d(logTag, "The AUDIO permission was granted!")
                } else {
                    Log.d(logTag, "The AUDIO permission was denied!")
                }
            }
        )
        PermissionUtil.requestPermissions(this,
            PermissionConstants.PermissionType.CAMERA,
            PermissionConstants.RationaleType.CAMERA,
            IPermissionCallBack { grantedAll, _ ->
                if (grantedAll) {
                    //to do your following thing with camera permission
                    Log.d(logTag, "The CAMERA permission was granted!")
                } else {
                    Log.d(logTag, "The CAMERA permission was denied!")
                }
            }
        )
    }
}