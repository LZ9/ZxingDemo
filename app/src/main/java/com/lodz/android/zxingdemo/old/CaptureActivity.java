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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.lodz.android.corekt.utils.DateUtils;
import com.lodz.android.corekt.utils.ToastUtils;
import com.lodz.android.zxingdemo.R;
import com.lodz.android.zxingdemo.main.decode.DecodeFormatManager;
import com.lodz.android.zxingdemo.main.media.BeepManager;
import com.lodz.android.zxingdemo.main.result.ResultBean;
import com.lodz.android.zxingdemo.main.result.ResultDialog;
import com.lodz.android.zxingdemo.old.camera.CameraManager;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends AppCompatActivity {

  private static final String TAG = CaptureActivity.class.getSimpleName();

  public static void start(Context context) {
      Intent starter = new Intent(context, CaptureActivity.class);
      context.startActivity(starter);
  }

  private CameraManager mCameraManager;
  private CaptureHelper mHelper;
  private ViewfinderView mViewfinderView;
  private BeepManager mBeepManager;

  private SurfaceView mSurfaceView;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_capture);

    mBeepManager = new BeepManager(this, true);

    Button flashBtn = findViewById(R.id.flash_btn);
    flashBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mCameraManager.setTorch(!mCameraManager.getTorchState());
      }
    });


    // historyManager must be initialized here to update the history preference

    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    mCameraManager = new CameraManager();

    mViewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
    mViewfinderView.setCameraManager(mCameraManager);

//    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//    if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION, true)) {
//    setRequestedOrientation(getCurrentOrientation());// 不使用自动旋转
//    } else {
//      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//    }


    resetStatusView();

    mSurfaceView = (SurfaceView) findViewById(R.id.preview_view);
    mSurfaceView.getHolder().addCallback(mCallback);
  }

//  private int getCurrentOrientation() {
//    int rotation = getWindowManager().getDefaultDisplay().getRotation();
//    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//      switch (rotation) {
//        case Surface.ROTATION_0:
//        case Surface.ROTATION_90:
//          return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//        default:
//          return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//      }
//    } else {
//      switch (rotation) {
//        case Surface.ROTATION_0:
//        case Surface.ROTATION_270:
//          return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//        default:
//          return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//      }
//    }
//  }

  @Override
  public void finish() {
    if (mHelper != null) {
      mHelper.quitSynchronously();
      mHelper = null;
    }
    mBeepManager.release();
    mCameraManager.closeDriver();
    //historyManager = null; // Keep for onActivityResult
    mSurfaceView.getHolder().removeCallback(mCallback);
    super.finish();
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  private void decodeOrStoreSavedBitmap(Result result) {
    // Bitmap isn't used yet -- will be used soon
    if (mHelper == null) {
      return;
    }
    if (result == null) {
      return;
    }
    mHelper.decodeSucceeded(result, null, -1f);
  }

  private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
      initCamera(holder);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }
  };

  /**
   * A valid barcode has been found, so give an indication of success and show the results.
   *
   * @param rawResult The contents of the barcode.
   * @param scaleFactor amount by which thumbnail was scaled
   * @param barcode   A greyscale bitmap of the camera data which was decoded.
   */
  private void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {


    // 是否动态识别
    boolean fromLiveScan = barcode != null;
    if (fromLiveScan) {
      // Then not from history, so beep/vibrate and we have an image to draw on
      mBeepManager.play();
      drawResultPoints(barcode, scaleFactor, rawResult, R.color.result_points);
    }

    ParsedResult result = ResultParser.parseResult(rawResult);

    mViewfinderView.setVisibility(View.GONE);

    ResultBean bean = new ResultBean();
    bean.setBarcodeImg(barcode);
    bean.setFormat(rawResult.getBarcodeFormat().toString());
    bean.setType(result.getType().toString());
    bean.setTime(DateUtils.getFormatString(DateUtils.TYPE_2, new Date(rawResult.getTimestamp())));
    bean.setContents(result.getDisplayResult());

    showResultDialog(CaptureActivity.this, bean);
  }

  /**
   * 动态识别的图片绘制识别点
   * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
   *
   * @param barcode   A bitmap of the captured image.
   * @param scaleFactor amount by which thumbnail was scaled
   * @param rawResult The decoded results which contains the points to draw.
   */
  private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult, @ColorRes int color) {
    ResultPoint[] points = rawResult.getResultPoints();
    if (points == null || points.length <= 0) {
      return;
    }
    if (barcode == null) {
      return;
    }
    Canvas canvas = new Canvas(barcode);
    Paint paint = new Paint();
    paint.setColor(ContextCompat.getColor(getContext(), color));
    if (points.length == 2) {
      paint.setStrokeWidth(4.0f);
      drawLine(canvas, paint, points[0], points[1], scaleFactor);
      return;
    }

    if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
      // Hacky special case -- draw two lines, for the barcode and metadata
      drawLine(canvas, paint, points[0], points[1], scaleFactor);
      drawLine(canvas, paint, points[2], points[3], scaleFactor);
      return;
    }

    paint.setStrokeWidth(10.0f);
    for (ResultPoint point : points) {
      if (point != null) {
        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
      }
    }
  }

  private void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
    if (a != null && b != null) {
      canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
    }
  }

  private void showResultDialog(Context context, ResultBean bean){
    ResultDialog dialog = new ResultDialog(context);
    dialog.setData(bean);
    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        restartPreviewAfterDelay();
      }
    });
    dialog.show();
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      ToastUtils.showShort(getContext(), "No SurfaceHolder provided");
      return;
    }
//    if (cameraManager.isOpen()) {
//      Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
//      return;
//    }
    try {
      mCameraManager.openCamera(getContext(), Camera.CameraInfo.CAMERA_FACING_BACK, surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (mHelper == null) {
        mHelper = new CaptureHelper(createBarcodeFormat(), mCameraManager);
        mHelper.setListener(new CaptureHelper.CaptureActivityHelperListener() {
          @Override
          public void onFoundPossibleResultPoint(ResultPoint point) {
            mViewfinderView.addPossibleResultPoint(point);
          }

          @Override
          public void decode(Result result, Bitmap barcode, float scaleFactor) {
            handleDecode(result, barcode, scaleFactor);
          }

          @Override
          public void restartPreviewAndDecode() {
            drawViewfinder();
          }
        });
      }
      decodeOrStoreSavedBitmap(null);
    } catch (Exception e) {
      e.printStackTrace();
      Log.w(TAG, e);
      showCameraExceptionDialog();
    }
  }

  private Collection<BarcodeFormat> createBarcodeFormat() {
    Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
    decodeFormats.addAll(DecodeFormatManager.getQR_CODE_FORMATS());//二维码
//    decodeFormats.addAll(DecodeFormatManager.getINDUSTRIAL_FORMATS());// 条形码，需要横屏识别

    // 其余格式，添加越多识别速度越慢
//    decodeFormats.addAll(DecodeFormatManager.getPRODUCT_FORMATS());
//    decodeFormats.addAll(DecodeFormatManager.getDATA_MATRIX_FORMATS());
//    decodeFormats.addAll(DecodeFormatManager.getAZTEC_FORMATS());
//    decodeFormats.addAll(DecodeFormatManager.getPDF417_FORMATS());
    return decodeFormats;
  }

  private void showCameraExceptionDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.app_name));
    builder.setMessage(getString(R.string.msg_camera_framework_bug));
    builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        finish();
      }
    });
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
        finish();
      }
    });
    builder.show();
  }

  public void restartPreviewAfterDelay() {
    if (mHelper != null) {
      mHelper.restartPreview();
    }
    resetStatusView();
  }

  private void resetStatusView() {
    mViewfinderView.setVisibility(View.VISIBLE);
  }

  public void drawViewfinder() {
    mViewfinderView.drawViewfinder();
  }

  private Context getContext(){
    return this;
  }
}
