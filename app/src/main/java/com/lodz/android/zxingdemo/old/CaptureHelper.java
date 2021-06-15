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
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;
import com.lodz.android.corekt.utils.UiHandler;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.io.ByteArrayOutputStream;
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

  private CaptureActivityHelperListener mListener;

//  private final CaptureActivity activity;
  private State mState;
  private final CameraManager mCameraManager;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  public CaptureHelper(Collection<BarcodeFormat> decodeFormats, CameraManager cameraManager) {
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

    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);

    handlerInitLatch.countDown();

    mState = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.mCameraManager = cameraManager;
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
        mState = State.PREVIEW;
        mCameraManager.requestPreviewFrame(CaptureHelper.this);
      }
    });
  }

  public void decodeSucceeded(Result result, byte[] bytes, float scale) {
    UiHandler.post(new Runnable() {
      @Override
      public void run() {
        mState = State.SUCCESS;
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
    mState = State.DONE;
    mCameraManager.stopPreview();
    quitDecode();

    // Be absolutely sure we don't send any queued up messages
  }

  private void restartPreviewAndDecode() {
    if (mState == State.SUCCESS) {
      mState = State.PREVIEW;
      mCameraManager.requestPreviewFrame(CaptureHelper.this);
      if (mListener != null){
        mListener.restartPreviewAndDecode();
      }
    }
  }

  private MultiFormatReader multiFormatReader;
  private boolean isRunning = true;


  public void startDecode(){
    isRunning = true;
  }

  public void quitDecode(){
    isRunning = false;
  }

  /**
   * 解码
   * @param data 图片数据
   * @param width 宽
   * @param height 高
   */
  public void decode(byte[] data, int width, int height) {
    if (!isRunning){
      return;
    }
//    Bitmap a = BitmapUtils.rotateBitmap(getPriviewPic(data, width, height), 90f);
    PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(data, width, height);
    if (source == null){
      decodeFailed();
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
      decodeFailed();
      return;
    }
    int[] pixels = source.renderThumbnail();
    int thumbnailWidth = source.getThumbnailWidth();
    int thumbnailHeight = source.getThumbnailHeight();
    Bitmap bitmap = Bitmap.createBitmap(pixels, 0, thumbnailWidth, thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    decodeSucceeded(rawResult, out.toByteArray(), (float) thumbnailWidth / source.getWidth());
  }

  private Bitmap getPriviewPic(byte[] data, int width, int height) {//这里传入的data参数就是onpreviewFrame中需要传入的byte[]型数据
    BitmapFactory.Options newOpts = new BitmapFactory.Options();
    newOpts.inJustDecodeBounds = true;
    YuvImage yuvimage = new YuvImage(
            data,
            ImageFormat.NV21,
            width,
            height,
            null);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
    byte[] rawImage = baos.toByteArray();
    //将rawImage转换成bitmap
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.RGB_565;
    return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
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
