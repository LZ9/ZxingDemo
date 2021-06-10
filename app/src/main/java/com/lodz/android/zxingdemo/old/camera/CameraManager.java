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

package com.lodz.android.zxingdemo.old.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.client.android.camera.CameraConfigurationUtils;
import com.lodz.android.zxingdemo.old.CaptureHelper;
import com.lodz.android.zxingdemo.old.camera.open.CameraBean;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
@SuppressWarnings("deprecation") // camera APIs
public final class CameraManager {

  private static final String TAG = CameraManager.class.getSimpleName();

  /** 最小扫描宽度 */
  private static final int MIN_SCAN_FRAME_WIDTH = 432; // = 720 * 0.6
  /** 最小扫描高度 */
  private static final int MIN_SCAN_FRAME_HEIGHT = 432;
  /** 最大扫描宽度 */
  private static final int MAX_SCAN_FRAME_WIDTH = 648; // = 1080 * 0.6
  /** 最大扫描高度 */
  private static final int MAX_SCAN_FRAME_HEIGHT = 648;

  /** 相机数据 */
  private CameraBean mCameraBean;
  /** 是否初始化 */
  private boolean isInit;
  /** 是否正在预览 */
  private boolean isPreviewing;

  private int cwRotationFromDisplayToCamera;
  private Point mScreenResolution;
  private Point mBestPreviewSize;

  /**
   * 打开相机
   * @param context 上下文
   * @param cameraId 相机id
   * @param holder 句柄
   */
  public void openCamera(Context context, int cameraId, SurfaceHolder holder) throws IOException {
    if (mCameraBean == null) {
      mCameraBean = createCameraBean(cameraId);
      if (mCameraBean == null) {
        // 相机开启失败
        throw new IOException("Camera.open() failed to return object from driver");
      }
    }

    if (!isInit) {
      isInit = true;


      WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      Display display = manager.getDefaultDisplay();

      int displayRotation = display.getRotation();


      cwRotationFromDisplayToCamera = initFromCameraParameters(mCameraBean, displayRotation);
      Log.i(TAG, "Final display orientation: " + cwRotationFromDisplayToCamera);



      Camera.Parameters parameters = mCameraBean.getCamera().getParameters();
      Point theScreenResolution = new Point();
      display.getSize(theScreenResolution);
      mScreenResolution = theScreenResolution;
      Log.i(TAG, "Screen resolution in current orientation: " + mScreenResolution);
      mBestPreviewSize = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, mScreenResolution);
      Log.i(TAG, "Best available preview size: " + mBestPreviewSize);
    }

    Camera camera = mCameraBean.getCamera();
    try {
      configCameraParameters(mCameraBean);
    } catch (Exception e) {
      e.printStackTrace();
    }
    camera.setPreviewDisplay(holder);
  }

  /**
   * 打开摄像头
   * @param cameraId 相机id
   */
  private CameraBean createCameraBean(int cameraId) {
    try {
      Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
      Camera.getCameraInfo(cameraId, cameraInfo);
      Camera camera = Camera.open(cameraId);
      if (camera != null) {
        return new CameraBean(camera, cameraInfo);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /** 相机是否开启 */
  public boolean isOpen() {
    return mCameraBean != null && mCameraBean.getCamera() != null;
  }

  /** 关闭相机 */
  public void closeCamera() {
    if (mCameraBean != null){
      mCameraBean.getCamera().release();
    }
    mCameraBean = null;
  }

  /** 开始预览 */
  public void startPreview() {
    if (mCameraBean != null && !isPreviewing) {
      mCameraBean.getCamera().startPreview();
      isPreviewing = true;
    }
  }

  /** 停止预览 */
  public void stopPreview() {
    if (mCameraBean != null && isPreviewing) {
      mCameraBean.getCamera().stopPreview();
      isPreviewing = false;
    }
  }

  /**
   * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
   * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
   * respectively.
   *
   */
  public synchronized void requestPreviewFrame(CaptureHelper helper) {
    CameraBean theCamera = mCameraBean;
    if (theCamera != null && isPreviewing) {
      theCamera.getCamera().setOneShotPreviewCallback(new Camera.PreviewCallback(){
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
          if (mBestPreviewSize != null && helper != null) {
            helper.decode(data, mBestPreviewSize.x, mBestPreviewSize.y);
          }
        }
      });
    }
  }

  /**
   * Calculates the framing rect which the UI should draw to show the user where to place the
   * barcode. This target helps with alignment as well as forces the user to hold the device
   * far enough away to ensure the image will be in focus.
   *
   * @return The rectangle to draw on screen in window coordinates.
   */
  public Rect getFramingRect() {
    if (mCameraBean == null) {
      return null;
    }
    if (mScreenResolution == null) {
      // Called early, before init even finished
      return null;
    }

    int width = findDesiredDimensionInRange(mScreenResolution.x, MIN_SCAN_FRAME_WIDTH, MAX_SCAN_FRAME_WIDTH);
    int height = findDesiredDimensionInRange(mScreenResolution.y, MIN_SCAN_FRAME_HEIGHT, MAX_SCAN_FRAME_HEIGHT);

    int leftOffset = (mScreenResolution.x - width) / 2;
    int topOffset = (mScreenResolution.y - height) / 2;
    return new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
  }

  private int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
    int dim = Math.round(resolution * 0.6f);
    if (dim < hardMin) {
      return hardMin;
    }
    return Math.min(dim, hardMax);
  }

  /**
   * A factory method to build the appropriate LuminanceSource object based on the format
   * of the preview buffers, as described by Camera.Parameters.
   *
   * @param data A preview frame.
   * @param width The width of the image.
   * @param height The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    Rect rect = getFramingRect();
    if (rect == null) {
      return null;
    }
    // 如果竖屏进行数据翻转
    Point screenResolution = mScreenResolution;
    if (screenResolution.x < screenResolution.y) {

      byte[] rotatedData = new byte[data.length];
      int newWidth = height;
      int newHeight = width;

      for (int y = 0; y < height; y++) {

        for (int x = 0; x < width; x++) {
          rotatedData[x * newWidth + newWidth - 1 - y] = data[x + y * width];
        }
      }
      return new PlanarYUVLuminanceSource(rotatedData, newWidth, newHeight, rect.left, rect.top, rect.width(), rect.height(), false);
    }
    // Go ahead and assume it's YUV rather than die.
    return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
  }

  /** 获取闪光灯开启状态 */
  public synchronized boolean getTorchState() {
    if (mCameraBean == null || mCameraBean.getCamera() == null) {
      return false;
    }
    Camera.Parameters parameters = mCameraBean.getCamera().getParameters();
    if (parameters == null) {
      return false;
    }
    String flashMode = parameters.getFlashMode();
    return Camera.Parameters.FLASH_MODE_ON.equals(flashMode) || Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode);
  }

  /**
   * 设置闪光灯是否开启
   * @param isOpen 是否开启闪光灯
   */
  public synchronized void setTorch(boolean isOpen) {
    if (mCameraBean == null || mCameraBean.getCamera() == null) {
      return;
    }
    Camera.Parameters parameters = mCameraBean.getCamera().getParameters();
    CameraConfigurationUtils.setTorch(parameters, isOpen);
    mCameraBean.getCamera().setParameters(parameters);
  }

  /**
   * Reads, one time, values from the camera that are needed by the app.
   */
  private int initFromCameraParameters(CameraBean cameraBean, int displayRotation) {
    int cwRotationFromNaturalToDisplay;
    switch (displayRotation) {
      case Surface.ROTATION_0:
        cwRotationFromNaturalToDisplay = 0;
        break;
      case Surface.ROTATION_90:
        cwRotationFromNaturalToDisplay = 90;
        break;
      case Surface.ROTATION_180:
        cwRotationFromNaturalToDisplay = 180;
        break;
      case Surface.ROTATION_270:
        cwRotationFromNaturalToDisplay = 270;
        break;
      default:
        // Have seen this return incorrect values like -90
        if (displayRotation % 90 == 0) {
          cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
        } else {
          throw new IllegalArgumentException("Bad rotation: " + displayRotation);
        }
    }
    Log.i(TAG, "Display at: " + cwRotationFromNaturalToDisplay);

    int cwRotationFromNaturalToCamera = cameraBean.getCameraInfo().orientation;
    Log.i(TAG, "Camera at: " + cwRotationFromNaturalToCamera);

    // Still not 100% sure about this. But acts like we need to flip this:
    if (cameraBean.getCameraInfo().facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
      Log.i(TAG, "Front camera overriden to: " + cwRotationFromNaturalToCamera);
    }
    return (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
  }

  /**
   * 配置相机参数
   * @param cameraBean 相机数据
   */
  private void configCameraParameters(CameraBean cameraBean) {

    Camera camera = cameraBean.getCamera();
    Camera.Parameters parameters = camera.getParameters();

    if (parameters == null) {
      Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
      return;
    }

    Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

    CameraConfigurationUtils.setFocus(parameters, true, false, false);
    parameters.setRecordingHint(true);

    parameters.setPreviewSize(mBestPreviewSize.x, mBestPreviewSize.y);

    camera.setParameters(parameters);

    camera.setDisplayOrientation(cwRotationFromDisplayToCamera);

    Camera.Parameters afterParameters = camera.getParameters();
    Camera.Size afterSize = afterParameters.getPreviewSize();
    if (afterSize != null && (mBestPreviewSize.x != afterSize.width || mBestPreviewSize.y != afterSize.height)) {
      Log.w(TAG, "Camera said it supported preview size " + mBestPreviewSize.x + 'x' + mBestPreviewSize.y +
              ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
      mBestPreviewSize.x = afterSize.width;
      mBestPreviewSize.y = afterSize.height;
    }
  }

}
