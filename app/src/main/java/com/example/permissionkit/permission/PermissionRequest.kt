package com.example.permissionkit.permission;

class PermissionRequest(
        var mPermissions: MutableList<String>?,
        var mCallBacks: MutableList<IPermissionCallBack?>?,
        var mRationale: String?,
        var mShouldShowRationale: Boolean
)