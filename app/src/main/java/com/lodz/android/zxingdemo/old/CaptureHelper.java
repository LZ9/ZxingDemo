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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.lodz.android.corekt.utils.UiHandler;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureHelper {

  public static final String BARCODE_BITMAP = "barcode_bitmap";
  public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

  private CaptureActivityHelperListener mListener;

//  private final CaptureActivity activity;
  private State state;
  private final CameraManager cameraManager;

  private DecodeHelper mDecodeHelper;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  CaptureHelper(Collection<BarcodeFormat> decodeFormats, CameraManager cameraManager) {
    CountDownLatch handlerInitLatch = new CountDownLatch(1);
    Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, new ResultPointCallback() {
      @Override
      public void foundPossibleResultPoint(ResultPoint point) {
        if (mListener != null){
          mListener.onFoundPossibleResultPoint(point);
        }
      }
    });
    mDecodeHelper = new DecodeHelper(this, hints, cameraManager);
    handlerInitLatch.countDown();

    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  public void restartPreview(){
    UiHandler.post(new Runnable() {
      @Override
      public void run() {
        restartPreviewAndDecode();
      }
    });
  }

  public void decodeFailed() {
    UiHandler.post(new Runnable() {
      @Override
      public void run() {
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(mDecodeHelper);
      }
    });
  }

  public void decodeSucceeded(Result result, byte[] bytes, float scale) {
    UiHandler.post(new Runnable() {
      @Override
      public void run() {
        state = State.SUCCESS;
        Bitmap barcode = null;
        float scaleFactor = 1.0f;
        if (bytes != null) {
          barcode = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
          // Mutable copy:
          barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
          if (scale != -1f){
            scaleFactor = scale;
          }
        }
        if (mListener != null){
          mListener.decode(result, barcode, scaleFactor);
        }
      }
    });
  }

  public void quitSynchronously() {
    state = State.DONE;
    cameraManager.stopPreview();
    mDecodeHelper.quitDecode();

    // Be absolutely sure we don't send any queued up messages
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(mDecodeHelper);
      if (mListener != null){
        mListener.restartPreviewAndDecode();
      }
    }
  }

  public void setListener(CaptureActivityHelperListener listener){
    mListener = listener;
  }

  public interface CaptureActivityHelperListener {

    void onFoundPossibleResultPoint(ResultPoint point);

    void decode(Result rawResult, Bitmap barcode, float scaleFactor);

    void restartPreviewAndDecode();
  }
}
