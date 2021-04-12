package com.lodz.android.zxingdemo

import android.widget.Button
import com.lodz.android.corekt.anko.bindView
import com.lodz.android.pandora.base.activity.AbsActivity
import com.lodz.android.zxingdemo.main.MainCaptureActivity
import com.lodz.android.zxingdemo.old.CaptureActivity

class MainActivity : AbsActivity() {

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
}