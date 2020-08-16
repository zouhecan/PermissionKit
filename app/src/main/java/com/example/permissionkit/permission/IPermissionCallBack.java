package com.example.permissionkit.permission;

import java.util.List;

/**
 * desc: 检查/申请权限回调
 */
public interface IPermissionCallBack {
    void onPermissionsGranted(boolean grantedAll, List<String> perms);
}
