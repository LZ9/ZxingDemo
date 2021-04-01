package com.lodz.android.zxingdemo.main

import android.graphics.Bitmap

/**
 * 结果数据
 * @author zhouL
 * @date 2021/3/31
 */
class ResultBean {

    /** 识别截图 */
    var barcodeImg: Bitmap? = null

    /** 格式 */
    var format: String = ""

    /** 类型 */
    var type: String = ""

    /** 时间 */
    var time: String = ""

    /** 元数据 */
    var meta: String = ""

    /** 识别结果 */
    var contents: String = ""

}