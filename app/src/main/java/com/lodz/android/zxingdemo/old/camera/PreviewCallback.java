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

package com.lodz.android.zxingdemo.old.camera;

import android.graphics.Point;
import android.hardware.Camera;

import com.lodz.android.zxingdemo.old.DecodeHelper;

@SuppressWarnings("deprecation") // camera APIs
public class PreviewCallback implements Camera.PreviewCallback {

  private Point mCameraResolution;
  private DecodeHelper mDecodeHelper;

  public void setCameraResolution(Point cameraResolution) {
    this.mCameraResolution = cameraResolution;
  }

  public void setDecodeHelper(DecodeHelper helper) {
    this.mDecodeHelper = helper;
  }

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    if (mCameraResolution != null && mDecodeHelper != null) {
      mDecodeHelper.decode(data, mCameraResolution.x, mCameraResolution.y);
    }
  }

}
