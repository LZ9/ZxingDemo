package com.lodz.android.zxingdemo.main.decode

import com.google.zxing.BarcodeFormat
import java.util.*

/**
 *
 * @author zhouL
 * @date 2021/4/12
 */

object DecodeFormatManager {

    /** 条形码 */
    @JvmStatic
    val INDUSTRIAL_FORMATS: Set<BarcodeFormat> = EnumSet.of(
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.ITF,
            BarcodeFormat.CODABAR
    )

    /** 二维码 */
    @JvmStatic
    val QR_CODE_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.QR_CODE)

    @JvmStatic
    val PRODUCT_FORMATS: Set<BarcodeFormat> = EnumSet.of(
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED
    )

    @JvmStatic
    val DATA_MATRIX_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.DATA_MATRIX)

    @JvmStatic
    val AZTEC_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.AZTEC)

    @JvmStatic
    val PDF417_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.PDF_417)
}