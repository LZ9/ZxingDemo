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

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public final class DecodeHelper {

  private final MultiFormatReader multiFormatReader;
  private boolean isRunning = true;

  private CaptureHelper mHelper;

  private CameraManager mCameraManager;

  public DecodeHelper(CaptureHelper helper, Map<DecodeHintType,Object> hints, CameraManager manager) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.mHelper = helper;
    this.mCameraManager = manager;
  }

  public void startDecode(){
    isRunning = true;
  }

  public void quitDecode(){
    isRunning = false;
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
    if (!isRunning){
      return;
    }
    PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(data, width, height);
    if (source == null){
      mHelper.decodeFailed();
      return;
    }
    Result rawResult = null;
    try {
      rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      multiFormatReader.reset();
    }
    if (rawResult == null){
      mHelper.decodeFailed();
      return;
    }
    int[] pixels = source.renderThumbnail();
    int thumbnailWidth = source.getThumbnailWidth();
    int thumbnailHeight = source.getThumbnailHeight();
    Bitmap bitmap = Bitmap.createBitmap(pixels, 0, thumbnailWidth, thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    mHelper.decodeSucceeded(rawResult, out.toByteArray(), (float) thumbnailWidth / source.getWidth());
  }
}
