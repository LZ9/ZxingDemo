package com.lodz.android.zxingdemo

import android.Manifest
import android.content.DialogInterface
import android.os.Build
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.lodz.android.corekt.anko.bindView
import com.lodz.android.corekt.anko.goAppDetailSetting
import com.lodz.android.corekt.anko.isPermissionGranted
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.pandora.base.activity.AbsActivity
import com.lodz.android.zxingdemo.main.MainCaptureActivity
import com.lodz.android.zxingdemo.old.CaptureActivity
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest

class MainActivity : AbsActivity() {

    private val hasCameraPermissions = constructPermissionsRequest(
        Manifest.permission.CAMERA,// 相机
        onShowRationale = ::onShowRationaleBeforeRequest,
        onPermissionDenied = ::onDenied,
        onNeverAskAgain = ::onNeverAskAgain,
        requiresPermission = ::onRequestPermission
    )

    private val hasWriteExternalStoragePermissions = constructPermissionsRequest(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,// 存储卡读写
        onShowRationale = ::onShowRationaleBeforeRequest,
        onPermissionDenied = ::onDenied,
        onNeverAskAgain = ::onNeverAskAgain,
        requiresPermission = ::onRequestPermission
    )

    private val hasReadExternalStoragePermissions = constructPermissionsRequest(
        Manifest.permission.READ_EXTERNAL_STORAGE,// 存储卡读写
        onShowRationale = ::onShowRationaleBeforeRequest,
        onPermissionDenied = ::onDenied,
        onNeverAskAgain = ::onNeverAskAgain,
        requiresPermission = ::onRequestPermission
    )

    private val mOldBtn by bindView<Button>(R.id.old_btn)

    private val mMainBtn by bindView<Button>(R.id.main_btn)

    override fun getAbsLayoutId(): Int = R.layout.activity_main

    override fun setListeners() {
        super.setListeners()
        mOldBtn.setOnClickListener {
            CaptureActivity.start(getContext())
        }

        mMainBtn.setOnClickListener {
            MainCaptureActivity.start(getContext())
        }
    }

    override fun initData() {
        super.initData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0以上的手机对权限进行动态申请
            onRequestPermission()//申请权限
        }
    }

    /** 权限申请成功 */
    private fun onRequestPermission() {
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            hasCameraPermissions.launch()
            return
        }
        if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            hasWriteExternalStoragePermissions.launch()
            return
        }
        if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)){
            hasReadExternalStoragePermissions.launch()
            return
        }
    }

    /** 用户拒绝后再次申请前告知用户为什么需要该权限 */
    private fun onShowRationaleBeforeRequest(request: PermissionRequest) {
        request.proceed()//请求权限
    }

    /** 被拒绝 */
    private fun onDenied() {
        onRequestPermission()//申请权限
    }

    /** 被拒绝并且勾选了不再提醒 */
    private fun onNeverAskAgain() {
        toastShort(R.string.splash_check_permission_tips)
        showPermissionCheckDialog()
        goAppDetailSetting()
    }

    /** 显示权限核对弹框 */
    private fun showPermissionCheckDialog(){
        AlertDialog.Builder(getContext())
            .setMessage(R.string.splash_check_permission_title)
            .setPositiveButton(R.string.splash_check_permission_confirm) { dialog, which ->
                onRequestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.splash_check_permission_unconfirmed) { dialog, which ->
                goAppDetailSetting()
            }
            .setOnCancelListener { dialog ->
                toastShort(R.string.splash_check_permission_cancel)
                dialog.dismiss()
                finish()
            }
            .create()
            .show()
    }
}