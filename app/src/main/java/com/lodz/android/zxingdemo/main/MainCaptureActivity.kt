package com.lodz.android.zxingdemo.main

import android.content.Context
import android.content.Intent
import com.lodz.android.pandora.base.activity.AbsActivity
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

    override fun getAbsLayoutId(): Int = R.layout.activity_main_capture
}