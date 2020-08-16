package com.example.permissionkit.permission;

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.permissionkit.R
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * desc: 申请权限收口fragment
 */
class PermissionFragment : Fragment() {
    private lateinit var mContext: Context
    private val logTag = PermissionFragment::class.java.canonicalName

    //记录当前正在发起request
    private var requests = SparseArray<PermissionRequest>()

    //请求码
    private var requestCode = PermissionConstants.REQUEST_CODE_PERMISSION

    //是否正在请求中
    private var isBusy = AtomicBoolean(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun requestPermissions(permissions: MutableList<String>, rationale: String?, callBack: IPermissionCallBack?) {
        Log.w(logTag, "requestPermissions start")
        val mPermissions = filterPermission(permissions, callBack)
        if (mPermissions.isEmpty()) {
            return
        }
        this.requestCode = makeRequestCode()
        requests.put(requestCode, PermissionRequest(permissions, mutableListOf(callBack), rationale, shouldShowRationale(permissions.toTypedArray())))
        if (isBusy.compareAndSet(false, true)) {
            requestPermissions(permissions.toTypedArray(), requestCode)
        } else {
            showWaitDialogIfBusy(requestCode)
        }
    }

    /**
     * 如果fragment正在忙碌中，展示等待弹窗
     */
    private fun showWaitDialogIfBusy(requestCode: Int) {
        val request = requests[requestCode]
        val permissions = request.mPermissions
        if (permissions.isNullOrEmpty()) {
            requests.remove(requestCode)
            return
        }
        val rationale = String.format(getString(R.string.rationale), request?.mRationale)
        val builder = AlertDialog.Builder(mContext).setTitle("").setMessage(rationale)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            isBusy.set(true)
            requestPermissions(permissions.toTypedArray(), requestCode)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            isBusy.set(false)
            notifyObserver(requestCode, request, false, permissions.toList())
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    /**
     * 过滤正在请求的权限，防止相同重复请求
     * 但是保留它的callback，在请求结果回来后，一同回调
     */
    private fun filterPermission(permissions: MutableList<String>, callBack: IPermissionCallBack?): MutableList<String> {
        if (permissions.isEmpty()) {
            return mutableListOf()
        }
        for (i in 0 until requests.size()) {
            val request = requests.valueAt(i)
            request.mPermissions?.let {
                if (it.containsAll(permissions)) {
                    if (request.mCallBacks.isNullOrEmpty()) {
                        request.mCallBacks = mutableListOf(callBack)
                    } else {
                        request.mCallBacks!!.add(callBack)
                    }
                    return mutableListOf()
                }
            }
        }
        return permissions
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isBusy.set(false)
        Log.w(logTag, "requestPermissions end")
        if (requests.indexOfKey(requestCode) < 0) {
            return
        }
        val request = requests[requestCode]
        var allGranted = false
        val length = grantResults.size
        for (i in 0 until length) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
            allGranted = true
        }
        when {
            allGranted -> {
                //点击了同意权限
                if (isXiaoMiBrand()) {
                    allGranted = doubleCheckPermissionGranted(permissions)
                }
                Log.w(logTag, "all permissions have been granted")
                notifyObserver(requestCode, request, allGranted, permissions.toList())
            }
            shouldShowRationale(permissions) -> {
                //点击了禁止权限
                notifyObserver(requestCode, request, allGranted, permissions.toList())
                Log.w(logTag, "one or more permissions have been denied")
            }
            request.mShouldShowRationale -> {
                //点击了禁止后不再提示，本次操作后变为"禁止后不再提示"的状态，再次申请该权限时，就需要给予rationale弹窗引导
                notifyObserver(requestCode, request, allGranted, permissions.toList())
                Log.w(logTag, "one or more permissions have been denied")
            }
            TextUtils.isEmpty(request?.mRationale) -> {
                //没有设置引导文案，即便属于"禁止后不再提示"的状态，也没法弹窗引导
                notifyObserver(requestCode, request, allGranted, permissions.toList())
            }
            else -> {
                //申请的权限之前，已经属于"禁止后不再提示"的状态，这时候为了让用户有感知，给予弹窗rationale文案进行引导提示
                Log.w(logTag, "one or more permissions have been denied with no longer prompt")
                try {
                    val rationale = String.format(getString(R.string.setting_tip), request?.mRationale)
                    val builder = AlertDialog.Builder(mContext).setTitle("").setMessage(rationale)
                    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        startSettingActivity()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        notifyObserver(requestCode, request, allGranted, permissions.toList())
                        dialog.dismiss()
                    }
                    builder.setCancelable(false)
                    builder.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun notifyObserver(requestCode: Int, request: PermissionRequest?, grantedAll: Boolean, permissions: List<String>?) {
        requests.remove(requestCode)
        val callbacks = request?.mCallBacks
        callbacks?.let {
            for (callback in callbacks) {
                Log.d(logTag, "notifyObserver result of ${request.mRationale} is $grantedAll")
                callback?.onPermissionsGranted(grantedAll, permissions)
            }
        }
    }

    /**
     * 是否点击了拒绝并且不再提示
     * @return false:是的, true:不是
     */
    private fun shouldShowRationale(perms: Array<String>): Boolean {
        for (perm in perms) {
            if (shouldShowRequestPermissionRationale(perm)) {
                return true
            }
        }
        return false
    }

    private fun isXiaoMiBrand(): Boolean {
        return Build.MANUFACTURER == "Xiaomi"
    }

    private fun doubleCheckPermissionGranted(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            // 获取app中 权限关联的操作
            val op = AppOpsManagerCompat.permissionToOp(permission)
            if (!TextUtils.isEmpty(op)) {
                // 查看 操作 是否被允许
                var result = AppOpsManagerCompat.noteProxyOp(mContext, op!!, mContext.packageName)
                if (result == AppOpsManager.MODE_IGNORED) {
                    return false
                }
                // 再次检查，确保有权限
                result = ContextCompat.checkSelfPermission(mContext, permission)
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 跳转到设置页面打开权限
     */
    private fun startSettingActivity() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" +
                    mContext.packageName))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, PermissionConstants.REQUEST_CODE_PERMISSION_SETTING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置页返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PermissionConstants.REQUEST_CODE_PERMISSION_SETTING) {
            return
        }
        Log.d(logTag, "the results for request permissions from system setting page")
        for (i in 0 until requests.size()) {
            val request = requests.valueAt(i)
            val grantedAll = PermissionUtil.checkPermissions(mContext, request.mPermissions)
            notifyObserver(requests.keyAt(i), request, grantedAll, request.mPermissions?.toList())
        }
    }

    /**
     * requestCode
     * 一般情况下，不会出现多个权限同时请求（多个权限没有在一个request中，而是分成了多个request同时请求）
     * 如果出现了同时请求，就需要生成不一样的requestCode，用于区分他们
     */
    private fun makeRequestCode(): Int {
        if (requests.size() <= 0) {
            return PermissionConstants.REQUEST_CODE_PERMISSION
        }
        //随机生成唯一的requestCode，最多尝试10次
        var requestCode: Int
        var tryCount = 0
        do {
            requestCode = Random.nextInt(0x0000FFFF)
            tryCount++
        } while (requests.indexOfKey(requestCode) >= 0 && tryCount < 10)
        return requestCode
    }
}