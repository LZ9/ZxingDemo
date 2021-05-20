/*
 * Copyright (C) 2012 ZXing authors
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

package com.lodz.android.zxingdemo.old.camera.open;

import android.hardware.Camera;
import android.util.Log;

/**
 * Abstraction over the {@link Camera} API that helps open them and return their metadata.
 */
@SuppressWarnings("deprecation") // camera APIs
public final class OpenCameraInterface {

  private static final String TAG = OpenCameraInterface.class.getName();


  private OpenCameraInterface() {
  }

  /**
   * Opens the requested camera with {@link Camera#open(int)}, if one exists.
   *
   * @param cameraId camera ID of the camera to use
   * @return handle to {@link CameraBean} that was opened
   */
  public static CameraBean open(int cameraId) {
    Log.i(TAG, "Opening camera #" + cameraId);
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    Camera.getCameraInfo(cameraId, cameraInfo);
    Camera camera = Camera.open(cameraId);
    if (camera == null) {
      return null;
    }
    return new CameraBean(cameraId,
            camera,
            cameraInfo.facing,
            cameraInfo.orientation);
  }

}
