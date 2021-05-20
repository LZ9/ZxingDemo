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
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.lodz.android.corekt.utils.UiHandler;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.util.Collection;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHelper  {

  private CaptureActivityHelperListener mListener;

//  private final CaptureActivity activity;
  private final DecodeThread decodeThread;
  private State state;
  private final CameraManager cameraManager;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  CaptureActivityHelper(Collection<BarcodeFormat> decodeFormats, String characterSet, CameraManager cameraManager) {
    decodeThread = new DecodeThread(this, cameraManager,decodeFormats, characterSet, new ResultPointCallback() {
      @Override
      public void foundPossibleResultPoint(ResultPoint point) {
        if (mListener != null){
          mListener.onFoundPossibleResultPoint(point);
        }
      }
    });
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  public void restartPreview(long delayMS){
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
        cameraManager.requestPreviewFrame(decodeThread.getDecodeHelper());
      }
    });
  }

  public void decodeSucceeded(Result result, Bundle bundle) {
    UiHandler.post(new Runnable() {
      @Override
      public void run() {
        state = State.SUCCESS;
        Bitmap barcode = null;
        float scaleFactor = 1.0f;
        if (bundle != null) {
          byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
          if (compressedBitmap != null) {
            barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
            // Mutable copy:
            barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
          }
          scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
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
    decodeThread.getDecodeHelper().quit();
    try {
      // Wait at most half a second; should be enough time, and onPause() will timeout quickly
      decodeThread.join(500L);
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(decodeThread.getDecodeHelper());
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
