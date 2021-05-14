/*
 * Copyright (C) 2010 ZXing authors
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
import android.os.Bundle;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public final class DecodeHelper {

  private final MultiFormatReader multiFormatReader;
  private boolean running = true;

  private CaptureActivityHelper mHelper;

  private CameraManager mCameraManager;

  public DecodeHelper(CaptureActivityHelper helper, Map<DecodeHintType,Object> hints, CameraManager manager) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.mHelper = helper;
    this.mCameraManager = manager;
  }

  public void quit(){
    running = false;
//    Looper.myLooper().quit();
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  public void decode(byte[] data, int width, int height) {
    if (!running){
      return;
    }
    Result rawResult = null;
    PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(data, width, height);
    if (source != null) {
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      try {
        rawResult = multiFormatReader.decodeWithState(bitmap);
      } catch (ReaderException re) {
        // continue
      } finally {
        multiFormatReader.reset();
      }
    }

    if (rawResult != null) {
      // Don't log the barcode contents for security.
      Bundle bundle = new Bundle();
      bundleThumbnail(source, bundle);
      mHelper.decodeSucceeded(rawResult, bundle);
    } else {
      mHelper.decodeFailed();
    }
  }

  private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
    int[] pixels = source.renderThumbnail();
    int width = source.getThumbnailWidth();
    int height = source.getThumbnailHeight();
    Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
    bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
  }

}