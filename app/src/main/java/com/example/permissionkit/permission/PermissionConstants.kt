package com.example.permissionkit.permission;

import android.Manifest

/**
 * desc: app权限常量
 */
class PermissionConstants {
    companion object {
        const val REQUEST_CODE_PERMISSION = 100
        const val REQUEST_CODE_OPEN_GPS_SETTING = 101
        const val REQUEST_CODE_PERMISSION_SETTING = 102

        val permissionTable = mutableMapOf(
                PermissionType.WRITE_EXTERNAL_STORAGE to Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PermissionType.ACCESS_FINE_LOCATION to Manifest.permission.ACCESS_FINE_LOCATION,
                PermissionType.ACCESS_COARSE_LOCATION to Manifest.permission.ACCESS_COARSE_LOCATION,
                PermissionType.READ_PHONE_STATE to Manifest.permission.READ_PHONE_STATE,
                PermissionType.READ_CONTACTS to Manifest.permission.READ_CONTACTS,
                PermissionType.RECORD_AUDIO to Manifest.permission.RECORD_AUDIO,
                PermissionType.CAMERA to Manifest.permission.CAMERA
        )

        val rationaleTypeList = mutableListOf(RationaleType.CALENDAR, RationaleType.CAMERA, RationaleType.CONTACTS,
                RationaleType.LOCATION, RationaleType.MICROPHONE, RationaleType.PHONE, RationaleType.SENSORS,
                RationaleType.SMS, RationaleType.STORAGE, RationaleType.WINDOW)
    }

    class PermissionType {
        companion object {
            //读写权限
            const val WRITE_EXTERNAL_STORAGE = 1 shl 0

            //精确定位权限
            const val ACCESS_FINE_LOCATION = 1 shl 1

            //模糊定位权限
            const val ACCESS_COARSE_LOCATION = 1 shl 2

            //电话权限
            const val READ_PHONE_STATE = 1 shl 3

            //联系人权限
            const val READ_CONTACTS = 1 shl 4

            //麦克风权限
            const val RECORD_AUDIO = 1 shl 5

            //相机权限
            const val CAMERA = 1 shl 6
        }
    }

    class RationaleType {
        companion object {
            //日历
            const val CALENDAR = 1 shl 0

            //相机
            const val CAMERA = 1 shl 1

            //联系人
            const val CONTACTS = 1 shl 2

            //定位
            const val LOCATION = 1 shl 3

            //麦克风
            const val MICROPHONE = 1 shl 4

            //打电话
            const val PHONE = 1 shl 5

            //传感器
            const val SENSORS = 1 shl 6

            //短信
            const val SMS = 1 shl 7

            //数据读写
            const val STORAGE = 1 shl 8

            //悬浮窗
            const val WINDOW = 1 shl 9
        }
    }
}