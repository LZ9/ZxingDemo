/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lodz.android.zxingdemo.old;


import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.lodz.android.zxingdemo.main.decode.DecodeFormatManager;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";
  public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

  private final CaptureActivityHelper mHelper;
  private final Map<DecodeHintType,Object> hints;
  private DecodeHelper mDecodeHelper;
  private final CountDownLatch handlerInitLatch;

  private CameraManager mCameraManager;

  DecodeThread(CaptureActivityHelper helper,
               CameraManager manager,
               Collection<BarcodeFormat> decodeFormats,
               String characterSet,
               ResultPointCallback resultPointCallback) {

    mCameraManager = manager;
    this.mHelper = helper;
    handlerInitLatch = new CountDownLatch(1);

    hints = new EnumMap<>(DecodeHintType.class);

    // The prefs can't change while the thread is running, so pick them up once here.
    if (decodeFormats == null || decodeFormats.isEmpty()) {
      decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
      decodeFormats.addAll(DecodeFormatManager.getQR_CODE_FORMATS());//二维码
      decodeFormats.addAll(DecodeFormatManager.getINDUSTRIAL_FORMATS());// 条形码，需要横屏识别

      boolean isDefaultAdd = false;
      if (isDefaultAdd){
        decodeFormats.addAll(DecodeFormatManager.getPRODUCT_FORMATS());
        decodeFormats.addAll(DecodeFormatManager.getDATA_MATRIX_FORMATS());
        decodeFormats.addAll(DecodeFormatManager.getAZTEC_FORMATS());
        decodeFormats.addAll(DecodeFormatManager.getPDF417_FORMATS());
      }
    }
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    if (characterSet != null) {
      hints.put(DecodeHintType.CHARACTER_SET, characterSet);
    }
    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
  }

  public DecodeHelper getDecodeHelper(){
    return mDecodeHelper;
  }

  @Override
  public void run() {
    Looper.prepare();
    mDecodeHelper = new DecodeHelper(mHelper, hints,mCameraManager );
    handlerInitLatch.countDown();
    Looper.loop();
  }

}
