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


import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.util.Collection;
import java.util.EnumMap;
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
               ResultPointCallback resultPointCallback) {

    mCameraManager = manager;
    this.mHelper = helper;
    handlerInitLatch = new CountDownLatch(1);

    hints = new EnumMap<>(DecodeHintType.class);
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    mDecodeHelper = new DecodeHelper(mHelper, hints,mCameraManager );
    handlerInitLatch.countDown();
  }

  public DecodeHelper getDecodeHelper(){
    return mDecodeHelper;
  }

  @Override
  public void run() {
//    Looper.prepare();
//    mDecodeHelper = new DecodeHelper(mHelper, hints,mCameraManager );
//    handlerInitLatch.countDown();
//    Looper.loop();
  }

}
