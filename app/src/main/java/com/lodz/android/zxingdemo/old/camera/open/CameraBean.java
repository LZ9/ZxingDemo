/*
 * Copyright (C) 2015 ZXing authors
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

/**
 * Represents an open {@link Camera} and its metadata, like facing direction and orientation.
 */
@SuppressWarnings("deprecation") // camera APIs
public final class CameraBean {

  /** 相机 */
  private final Camera camera;
  /** 相机信息 */
  private Camera.CameraInfo mCameraInfo;

  public CameraBean(Camera camera, Camera.CameraInfo cameraInfo) {
    this.camera = camera;
    this.mCameraInfo = cameraInfo;
  }

  public Camera getCamera() {
    return camera;
  }

  public Camera.CameraInfo getCameraInfo() {
    return mCameraInfo;
  }

}
