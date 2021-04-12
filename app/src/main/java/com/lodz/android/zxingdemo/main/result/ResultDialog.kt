package com.lodz.android.zxingdemo.main.result

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.bindView
import com.lodz.android.pandora.widget.dialog.BaseDialog
import com.lodz.android.zxingdemo.R

/**
 * 结果弹框
 * @author zhouL
 * @date 2021/3/30
 */
class ResultDialog(context: Context) : BaseDialog(context) {

    /** 识别截图 */
    private val mBarcodeImg by bindView<ImageView>(R.id.barcode_img)
    /** 格式 */
    private val mFormatTv by bindView<TextView>(R.id.format_tv)
    /** 类型 */
    private val mTypeTv by bindView<TextView>(R.id.type_tv)
    /** 时间 */
    private val mTimeTv by bindView<TextView>(R.id.time_tv)
    /** 元数据 */
    private val mMetaTv by bindView<TextView>(R.id.meta_tv)
    /** 识别结果 */
    private val mContentsTv by bindView<TextView>(R.id.contents_tv)

    private var mBean: ResultBean? = null


    override fun getLayoutId(): Int = R.layout.dialog_result

    fun setData(bean: ResultBean) {
        mBean = bean
    }

    override fun initData() {
        super.initData()
        val bean = mBean ?: return
        if (bean.barcodeImg == null){
            mBarcodeImg.setImageResource(R.mipmap.ic_launcher_round)
        }else{
            mBarcodeImg.setImageBitmap(bean.barcodeImg)
        }

        val bitmap = if (bean.barcodeImg == null) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round)
        } else {
            bean.barcodeImg
        }
        mBarcodeImg.setImageBitmap(bitmap)
        mFormatTv.text = context.getString(R.string.msg_default_format).append(bean.format)
        mTypeTv.text = context.getString(R.string.msg_default_type).append(bean.type)
        mTimeTv.text = context.getString(R.string.msg_default_time).append(bean.time)
        mMetaTv.text = context.getString(R.string.msg_default_meta).append(bean.meta)
        mContentsTv.text = bean.contents
    }

}