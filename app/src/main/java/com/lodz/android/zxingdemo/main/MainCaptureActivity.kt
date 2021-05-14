package com.lodz.android.zxingdemo.main

import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import com.google.zxing.client.android.camera.CameraConfigurationUtils
import com.lodz.android.corekt.anko.bindView
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.pandora.base.activity.AbsActivity
import com.lodz.android.pandora.widget.camera.CameraHelper
import com.lodz.android.pandora.widget.camera.OnCameraListener
import com.lodz.android.zxingdemo.R

/**
 *
 * @author zhouL
 * @date 2021/4/12
 */
class MainCaptureActivity :AbsActivity(){

    companion object {
        fun start(context: Context){
            val intent = Intent(context, MainCaptureActivity::class.java)
            context.startActivity(intent)
        }
    }

    /** SurfaceView */
    private val mSurfaceView by bindView<SurfaceView>(R.id.surface_view)
    /** 闪光灯 */
    private val mFlashBtn by bindView<Button>(R.id.flash_btn)

    private var mCameraHelper: CameraHelper? = null

    override fun getAbsLayoutId(): Int = R.layout.activity_main_capture

    override fun setListeners() {
        super.setListeners()
        mFlashBtn.setOnClickListener {
            controlFlash()
        }
    }

    /** 控制相机闪光灯 */
    private fun controlFlash() {
        val isSuccess = mCameraHelper?.controlFlash() ?: false
        if (!isSuccess) {
            toastShort("不支持闪光灯")
        }
    }

    override fun initData() {
        super.initData()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mCameraHelper = CameraHelper(this, mSurfaceView)
        val parameters = mCameraHelper?.getCamera()?.parameters
        if (parameters != null){
            CameraConfigurationUtils.setFocus(parameters, true, false, false)
        }
        mCameraHelper?.setOnCameraListener(object :OnCameraListener{
            override fun onFaceDetect(faces: ArrayList<RectF>) {

            }

            override fun onPreviewFrame(data: ByteArray?) {


            }

            override fun onStatusChange(status: Int, msg: String) {


            }

            override fun onTakePic(data: ByteArray?) {


            }
        })
    }
}