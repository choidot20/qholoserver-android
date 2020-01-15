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

package com.google.zxing.client.android;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 80L;
  private static final int CURRENT_POINT_OPACITY = 0xA0;
  private static final int MAX_RESULT_POINTS = 0;

  private final Paint paint;
  private Bitmap resultBitmap;
 // private Bitmap maskBitmap;  //KYT
 // int[] pixels = new int[100 * 100]; //KYT
  private final int maskColor;
  private final int resultColor;
  private final int frameColor;
  private final int laserColor;
  private final int resultPointColor;
  private int scannerAlpha;
  private final AtomicReference<List<ResultPoint>> possibleResultPoints;
  private final AtomicReference<List<ResultPoint>> lastPossibleResultPoints;

  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint();
   // maskColor = Color.parseColor("#60000000"); 
    maskColor = Color.parseColor("#90000000"); //KYT
    resultColor = Color.parseColor("#b0000000");
    frameColor = Color.parseColor("#ff000000");
    laserColor = Color.parseColor("#ffff0000");
    resultPointColor = Color.parseColor("#c0ffff00");
   
    scannerAlpha = 0;
    possibleResultPoints = new AtomicReference<List<ResultPoint>>();
    lastPossibleResultPoints = new AtomicReference<List<ResultPoint>>();
    possibleResultPoints.set(new ArrayList<ResultPoint>(5));
    
    
  //  maskBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888); //KYT     
  //  maskBitmap.setPixels( pixels, 0, 50, 0, 0, 50, 50); //KYT
    
    
  }

  @Override
  public void onDraw(Canvas canvas) {
    Rect frame = CameraManager.get().getFramingRect();
    if (frame == null) {
      return;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();

    // Draw the exterior (i.e. outside the framing rect) darkened  //가운데 사각 영역 밖을 약간 어두운 트랜스페어런트 처리
   // paint.setColor(resultBitmap != null ? resultColor : maskColor); //KYT
   
    //paint.setColor(resultBitmap != null ? resultColor : Color.parseColor("#60ffffff")); //KYT
   
    // canvas.drawRect(0, 0, width, frame.top, paint);
   // canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
   // canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
   // canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    
  //  canvas.drawRect(frame.left+70,frame.top+70, frame.left+130, frame.top+130, paint);//KYT  인식점 영역 마스킹 
  //  canvas.drawRect(frame.right-130,frame.top+70, frame.right-70, frame.top+130, paint);//KYT  인식점 영역 마스킹 
  //  canvas.drawRect(frame.left+70,frame.bottom-130,frame.left+130, frame.bottom-70, paint);//KYT  인식점 영역 마스킹 
    
 
  //  canvas.drawBitmap(maskBitmap, null, frame, paint); //KYT 인식점 마킹용
      
    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(CURRENT_POINT_OPACITY);
      canvas.drawBitmap(resultBitmap, null, frame, paint);
    } else {

      // Draw a two pixel solid black border inside the framing rect
     // paint.setColor(frameColor);
      paint.setColor(Color.parseColor("#50000000")); //KYT
      canvas.drawRect(frame.left, frame.top, frame.right + 3, frame.top + 3, paint);
    //  canvas.drawRect(0,0, frame.right + 3, frame.top + 3, paint);
      canvas.drawRect(frame.left, frame.top + 3, frame.left + 3, frame.bottom - 3, paint);
      canvas.drawRect(frame.right - 3, frame.top, frame.right + 3, frame.bottom - 3, paint);
      canvas.drawRect(frame.left, frame.bottom - 3, frame.right + 3, frame.bottom + 3, paint);
      
      
      // Draw a red "laser scanner" line through the middle to show decoding is active
     // paint.setColor(laserColor); //KYT 빨간색 페이드 인 아웃 표시 관련 
      paint.setColor(Color.parseColor("#60ffffff"));
      paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
      scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length; 
       
      
     // canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint); //이건 가운데 
     // canvas.drawRect(frame.left + 1, frame.top+1 , frame.right - 1, frame.bottom -1, paint); //이건 맨위
      
      canvas.drawRect(frame.left+70,frame.top+70, frame.left+130, frame.top+130, paint);//KYT  인식점 영역 마스킹 
      canvas.drawRect(frame.right-130,frame.top+70, frame.right-70, frame.top+130, paint);//KYT  인식점 영역 마스킹 
      canvas.drawRect(frame.left+70,frame.bottom-130,frame.left+130, frame.bottom-70, paint);//KYT  인식점 영역 마스킹 
      
    
      Rect previewFrame = CameraManager.get().getFramingRectInPreview();
      float scaleX = frame.width() / (float) previewFrame.width();
      float scaleY = frame.height() / (float) previewFrame.height();

      List<ResultPoint> currentPossible = possibleResultPoints.get();
      List<ResultPoint> currentLast = lastPossibleResultPoints.get();
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints.set(null);
      } else {
        possibleResultPoints.set(new ArrayList<ResultPoint>(5));
        lastPossibleResultPoints.set(currentPossible);
        paint.setAlpha(CURRENT_POINT_OPACITY);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentPossible) {
          canvas.drawCircle(frame.left + (int) (point.getX() * scaleX),
                            frame.top + (int) (point.getY() * scaleY),
                            6.0f, paint);
        }
      }
      if (currentLast != null) {
        paint.setAlpha(CURRENT_POINT_OPACITY / 2);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentLast) {
          canvas.drawCircle(frame.left + (int) (point.getX() * scaleX),
                            frame.top + (int) (point.getY() * scaleY),
                            3.0f, paint);
        }
      }

      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom); //KYT 애니매이션 딜레이 , 이게 없으면 껌뻑껌뻑 안함. 
    }
  }

  public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    List<ResultPoint> points = possibleResultPoints.get();
    points.add(point);
    if (points.size() > MAX_RESULT_POINTS) {
      // trim it
      points.subList(0, points.size() - MAX_RESULT_POINTS / 2).clear();
    }
  }

}
