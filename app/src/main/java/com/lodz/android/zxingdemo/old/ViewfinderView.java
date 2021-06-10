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


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.zxing.ResultPoint;
import com.lodz.android.zxingdemo.R;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  /** 扫描线的透明度 */
  private static final int[] LASER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};

  /** 动画延迟 */
  private static final long ANIMATION_DELAY = 80L;
  /** 点位透明度 */
  private static final int CURRENT_POINT_OPACITY = 160;
  /** 最多展示点位 */
  private static final int MAX_RESULT_POINTS = 20;
  /** 点位直径 */
  private static final int POINT_SIZE = 6;

  private CameraManager mCameraManager;

  /** 画笔 */
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  /** 遮罩曾颜色 */
  private final int mMaskColor = getResources().getColor(R.color.viewfinder_mask);
  /** 扫描线颜色 */
  private final int mLaserColor = getResources().getColor(R.color.viewfinder_laser);
  /** 识别点颜色 */
  private final int mResultPointColor = getResources().getColor(R.color.possible_result_points);
  /** 扫描线透明度 */
  private int mLaserAlpha = 0;

  /** 识别点列表 */
  private List<ResultPoint> mPointList = new ArrayList<>();
  /** 最后一次的识别点列表 */
  private List<ResultPoint> mLastPointList = null;

  public ViewfinderView(Context context) {
    super(context);
  }

  public ViewfinderView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void setCameraManager(CameraManager cameraManager) {
    this.mCameraManager = cameraManager;
  }

  @SuppressLint("DrawAllocation")
  @Override
  public void onDraw(Canvas canvas) {
    if (mCameraManager == null) {
      return; // not ready yet, early draw before done configuring
    }

    Rect frame = mCameraManager.getFramingRect();
    Rect previewFrame = mCameraManager.getFramingRect();
    if (frame == null || previewFrame == null) {
      return;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();

    // Draw the exterior (i.e. outside the framing rect) darkened
    // 绘制遮罩层
    mPaint.setColor(mMaskColor);
    canvas.drawRect(0, 0, width, frame.top, mPaint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
    canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

    // Draw a red "laser scanner" line through the middle to show decoding is active
    // 绘制扫描线
    mPaint.setColor(mLaserColor);
    mPaint.setAlpha(LASER_ALPHA[mLaserAlpha]);
    mLaserAlpha = (mLaserAlpha + 1) % LASER_ALPHA.length;
    int middle = frame.height() / 2 + frame.top;
    canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, mPaint);


    float scaleX = frame.width() / (float) previewFrame.width();
    float scaleY = frame.height() / (float) previewFrame.height();

    int frameLeft = frame.left;
    int frameTop = frame.top;



    if (mPointList.isEmpty()) {
      mLastPointList = null;
    } else {
      // 画识别圆点
      mLastPointList = new ArrayList<>();
      mLastPointList.addAll(mPointList);
      mPointList.clear();
      mPaint.setAlpha(CURRENT_POINT_OPACITY);
      mPaint.setColor(mResultPointColor);
      synchronized (this) {
        for (ResultPoint point : mLastPointList) {
          canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX), frameTop + (int) (point.getY() * scaleY), POINT_SIZE, mPaint);
        }
      }

      // 画识别圆点
      mPaint.setAlpha(CURRENT_POINT_OPACITY / 2);
      mPaint.setColor(mResultPointColor);
      synchronized (this) {
        float radius = POINT_SIZE / 2.0f;
        for (ResultPoint point : mLastPointList) {
          canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX), frameTop + (int) (point.getY() * scaleY), radius, mPaint);
        }
      }
    }

    // Request another update at the animation interval, but only repaint the laser line,
    // not the entire viewfinder mask.
    postInvalidateDelayed(ANIMATION_DELAY,
            frame.left - POINT_SIZE,
            frame.top - POINT_SIZE,
            frame.right + POINT_SIZE,
            frame.bottom + POINT_SIZE);
  }

  public void drawViewfinder() {
    invalidate();
  }

  /** 添加识别点信息 */
  public void addPossibleResultPoint(ResultPoint point) {
    mPointList.add(point);
    int size = mPointList.size();
    if (size > MAX_RESULT_POINTS) {
      mPointList.subList(0, size - MAX_RESULT_POINTS / 2).clear();
    }
  }

}
