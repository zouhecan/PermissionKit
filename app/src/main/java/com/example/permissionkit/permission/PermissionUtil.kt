package com.example.permissionkit.permission;

import android.app.AppOpsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.permissionkit.ActivityStack
import com.example.permissionkit.IOSConfirm
import com.example.permissionkit.R

/**
 * desc: 检查/申请权限 统一工具
 */
class PermissionUtil {
    companion object {
        private val TAG = PermissionUtil::class.java.canonicalName

        /**
         * 检查某项权限
         */
        @JvmStatic
        fun checkPermission(context: Context, permission: String?): Boolean {
            //6.0以下无需检查权限
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }

            if (permission.isNullOrEmpty()) {
                return false
            }

            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                permission
            )
        }

        /**
         * 检查权限
         */
        @JvmStatic
        fun checkPermissions(context: Context, permissions: MutableList<String>?): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isNullOrEmpty()) {
                //无需检查
                return true
            }

            for (perm in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        perm
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }

        /**
         * 请求权限
         */
        @JvmStatic
        fun requestPermissions(
            permissionType: Int,
            rationaleType: Int? = null,
            permissionCallBack: IPermissionCallBack? = null
        ) {
            val activity = ActivityStack.getInstance().curr()
            if (activity == null || activity.isFinishing) {
                Log.w(TAG, "Aborting! activity is finishing when requesting permission")
                return
            }
            requestPermissions(
                activity as FragmentActivity,
                permissionType,
                rationaleType,
                permissionCallBack
            )
        }

        /**
         * 请求权限
         */
        @JvmStatic
        fun requestPermissions(
            activity: FragmentActivity?, permissionType: Int, rationaleType: Int? = null,
            permissionCallBack: IPermissionCallBack? = null
        ) {
            startRequest(activity, permissionType, rationaleType, permissionCallBack)
        }

        /**
         * 发起请求
         */
        private fun startRequest(
            activity: FragmentActivity?, permissionType: Int, rationaleType: Int? = null,
            permissionCallBack: IPermissionCallBack? = null
        ) {
            if (activity == null || activity.isFinishing) {
                Log.w(TAG, "Aborting! activity is finishing when requesting permission")
                return
            }
            var permissions = getPermissionsByType(permissionType)
            sendRequestLog(permissions)
            if (permissions.isEmpty()) {
                Log.w(TAG, "Can't check permissions for empty size")
                return
            }
            permissions = checkPermissionsInner(activity, permissions)
            if (permissions.isEmpty()) {
                Log.w(TAG, "permissions has been granted")
                permissionCallBack?.onPermissionsGranted(true, permissions)
                return
            }
            val fragmentTag = "PermissionFragment"
            val fragment: PermissionFragment
            val fragmentManager = activity.supportFragmentManager
            if (fragmentManager.findFragmentByTag(fragmentTag) != null) {
                fragment = fragmentManager.findFragmentByTag(fragmentTag) as PermissionFragment
            } else {
                fragment = PermissionFragment()
                fragmentManager.beginTransaction().add(fragment, fragmentTag)
                    .commitAllowingStateLoss()
                fragmentManager.executePendingTransactions()
            }
            fragment.requestPermissions(
                permissions,
                getRationalesByType(activity, rationaleType),
                permissionCallBack
            )
        }

        /**
         * 检查悬浮窗权限
         */
        @JvmStatic
        fun checkFloatPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                try {
                    var cls = Class.forName("android.content.Context")
                    val declaredField = cls.getDeclaredField("APP_OPS_SERVICE")
                    declaredField.isAccessible = true
                    var obj: Any? = declaredField[cls] as? String ?: return false
                    val str2 = obj as String
                    obj =
                        cls.getMethod("getSystemService", String::class.java).invoke(context, str2)
                    cls = Class.forName("android.app.AppOpsManager")
                    val declaredField2 = cls.getDeclaredField("MODE_ALLOWED")
                    declaredField2.isAccessible = true
                    val checkOp =
                        cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String::class.java)
                    val result =
                        checkOp.invoke(obj, 24, Binder.getCallingUid(), context.packageName) as Int
                    result == declaredField2.getInt(cls)
                } catch (e: Exception) {
                    false
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val appOpsMgr = context.getSystemService(Context.APP_OPS_SERVICE)
                        ?: return false
                    val mode = (appOpsMgr as AppOpsManager).checkOpNoThrow(
                        "android:system_alert_window", Process.myUid(), context
                            .packageName
                    )
                    mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED
                } else {
                    Settings.canDrawOverlays(context)
                }
            }
        }

        /**
         * 请求定位权限，回调结果中检查了gps开启状态
         * 需要在发起页监听：gps开启通知事件OpenGpsEvent
         */
        @JvmStatic
        fun requestLocationPermission(activity: FragmentActivity?, callBack: IPermissionCallBack?) {
            if (activity == null || activity.isFinishing) {
                return
            }
            requestPermissions(activity, PermissionConstants.PermissionType.ACCESS_FINE_LOCATION
                    or PermissionConstants.PermissionType.ACCESS_COARSE_LOCATION,
                PermissionConstants.RationaleType.LOCATION,
                IPermissionCallBack { grantedAll, perms ->
                    if (grantedAll) {
                        //检查gps权限
                        if (isGPSEnabled(activity)) {
                            sendResultCallBack(callBack, true, perms)
                        } else {
                            showOpenGPSSettingDialog(activity)
                            sendResultCallBack(callBack, false, perms)
                        }
                    } else {
                        sendResultCallBack(callBack, false, perms)
                    }
                })
        }

        /**
         * 发送回调
         */
        private fun sendResultCallBack(
            callBack: IPermissionCallBack?,
            allGranted: Boolean,
            permissions: List<String>?
        ) {
            callBack?.onPermissionsGranted(allGranted, permissions)
            sendResultLog(permissions, allGranted)
        }

        /**
         * 检查是否开通GPS定位或网络定位
         */
        @JvmStatic
        fun isGPSEnabled(context: Context): Boolean {
            return try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
            } catch (e: java.lang.Exception) {
                false
            }
        }

        /**
         * 设置gps弹窗是否已经展示
         */
        private var gpsDialogShowing = false

        /**
         * 开启gps定位服务设置弹窗
         */
        private fun showOpenGPSSettingDialog(mContext: FragmentActivity?) {
            if (gpsDialogShowing) {
                return
            }
            if (mContext == null || mContext.isFinishing) {
                return
            }
            val builder = IOSConfirm.Builder(mContext)
                .setTitle(mContext.getString(R.string.tips_location_closed))
                .setMessage(mContext.getString(R.string.tips_open_location))
                .setPositiveButton(mContext.getString(R.string.tips_setting_location)) { dialog: DialogInterface, _: Int ->
                    gotoLocationSettings(mContext)
                    dialog.dismiss()
                    gpsDialogShowing = false
                }
                .setNegativeButton(mContext.getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    gpsDialogShowing = false
                }
            val dialog = builder.createConfirm()
            dialog.setCancelable(true)
            dialog.show()
            gpsDialogShowing = true
        }

        /**
         * 去系统定位设置界面
         */
        private fun gotoLocationSettings(activity: FragmentActivity?) {
            try {
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity?.startActivityForResult(
                    settingsIntent,
                    PermissionConstants.REQUEST_CODE_OPEN_GPS_SETTING
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 检查权限
         */
        private fun checkPermissionsInner(
            activity: FragmentActivity,
            permissions: MutableList<String>
        ): MutableList<String> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()) {
                //无需检查
                return mutableListOf()
            }

            val iterator = permissions.iterator()
            while (iterator.hasNext()) {
                val permission = iterator.next()
                if (ContextCompat.checkSelfPermission(
                        activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //过滤已授权的选项
                    iterator.remove()
                }
            }
            return permissions
        }

        /**
         * 根据类型获取权限
         */
        private fun getPermissionsByType(permissionType: Int): MutableList<String> {
            val permissions = mutableListOf<String>()
            for ((k, v) in PermissionConstants.permissionTable) {
                if (permissionType and k == k) {
                    permissions.add(v)
                }
            }
            return permissions
        }

        /**
         * 根据类型获取rationale
         */
        private fun getRationalesByType(context: Context, rationaleType: Int?): String? {
            if (rationaleType == null || rationaleType == 0) {
                return ""
            }
            val rationale = StringBuilder()
            for (k in PermissionConstants.rationaleTypeList) {
                if (rationaleType and k == k) {
                    if (rationale.isNotEmpty()) {
                        rationale.append(context.getString(R.string.and))
                    }
                    rationale.append(getRationaleV(context, k))
                }
            }
            return rationale.toString()
        }

        /**
         * 获取解释文案rationale
         */
        private fun getRationaleV(context: Context, k: Int): String? {
            when (k) {
                PermissionConstants.RationaleType.CALENDAR -> return context.getString(R.string.permission_calendar)
                PermissionConstants.RationaleType.CAMERA -> return context.getString(R.string.permission_camera)
                PermissionConstants.RationaleType.CONTACTS -> return context.getString(R.string.permission_contacts)
                PermissionConstants.RationaleType.LOCATION -> return context.getString(R.string.permission_location)
                PermissionConstants.RationaleType.MICROPHONE -> return context.getString(R.string.permission_microphone)
                PermissionConstants.RationaleType.PHONE -> return context.getString(R.string.permission_phone)
                PermissionConstants.RationaleType.SENSORS -> return context.getString(R.string.permission_sensors)
                PermissionConstants.RationaleType.SMS -> return context.getString(R.string.permission_sms)
                PermissionConstants.RationaleType.STORAGE -> return context.getString(R.string.permission_storage)
                PermissionConstants.RationaleType.WINDOW -> return context.getString(R.string.permission_window)
            }
            return ""
        }

        /**
         * 发送请求埋点
         */
        private fun sendRequestLog(permissions: List<String>) {

        }

        /**
         * 发送结果埋点
         */
        private fun sendResultLog(permissions: List<String>?, allGranted: Boolean) {

        }

        /**
         * 组装埋点数据
         */
        private fun getLogInfo(permissions: List<String>?): String {
            permissions ?: return ""
            val sb = java.lang.StringBuilder()
            for (i in permissions.indices) {
                if (i == permissions.size - 1) {
                    sb.append(permissions[i])
                } else {
                    sb.append(permissions[i] + " & ")
                }
            }
            return sb.toString()
        }
    }
}