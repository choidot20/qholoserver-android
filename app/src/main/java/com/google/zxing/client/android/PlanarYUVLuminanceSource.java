/*
 * Copyright 2009 ZXing authors
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

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.LuminanceSource;
import com.google.zxing.ResultPointABC;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 *
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PlanarYUVLuminanceSource extends LuminanceSource {

  private final byte[] yuvData; //576000사이즈 데이타 
  private final int dataWidth; //800
  private final int dataHeight;//480
  private final int left; //362
  private final int top; //202 //800-480 가로 사이즈기 때문이다.

  public PlanarYUVLuminanceSource(byte[] yuvData, //576000 갤스2의 경우임 
                                  int dataWidth, //800
                                  int dataHeight, //480
                                  int left, //362
                                  int top, //202
                                  int width, //75 크롭사이즈  가로(길이)
                                  int height, //75 크롭사이즈  세로 (높이)
                                  boolean reverseHorizontal) { 
    super(width, height);

   //if (left + width > dataWidth || top + height > dataHeight) {
     // throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    //}
    
    this.yuvData = yuvData;
    this.dataWidth = dataWidth;
    this.dataHeight = dataHeight;
    this.left = left;
    this.top = top;
    if (reverseHorizontal) {
      reverseHorizontal(width, height);
    }
  }

  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    int offset = (y + top) * dataWidth + left;
    System.arraycopy(yuvData, offset, row, 0, width);
    return row;
  }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();

    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    if (width == dataWidth && height == dataHeight) {
      return yuvData;
    }

    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = top * dataWidth + left;

    // If the width matches the full width of the underlying data, perform a single copy.
    if (width == dataWidth) {
      System.arraycopy(yuvData, inputOffset, matrix, 0, area);
      return matrix;
    }

    // Otherwise copy one cropped row at a time.
    byte[] yuv = yuvData;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
      inputOffset += dataWidth;
    }
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  public int getDataWidth() {
    return dataWidth;
  }

  public int getDataHeight() {
    return dataHeight;
  }

   
  
public void decodeYUV420SP(int[] pixels, byte[] yuv420sp, int width, int height) {  //KYT YUV422sp -> RGB
    final int frameSize = width * height;  
    for (int j = 0, yp = 0; j < height; j++) {
        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
        for (int i = 0; i < width; i++, yp++) {  
            int y = (0xff & ((int) yuv420sp[yp])) - 16;  
            if (y < 0)
                y = 0;  
            if ((i & 1) == 0) {  
                v = (0xff & yuv420sp[uvp++]) - 128;  
                u = (0xff & yuv420sp[uvp++]) - 128;  
            }  
            int y1192 = 1192 * y;  
            int r = (y1192 + 1634 * v);  
            int g = (y1192 - 833 * v - 400 * u);  
            int b = (y1192 + 2066 * u);  
            if (r < 0)
                r = 0;
            else if (r > 262143)
                r = 262143;  
            if (g < 0)
                g = 0;
            else if (g > 262143)
                g = 262143;  
            if (b < 0)
                b = 0;
            else if (b > 262143)  
                b = 262143;  
          //  pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);  
            pixels[yp] = r/1024 ;  
        }
    }
}
   //public Bitmap renderCroppedGreyscaleBitmap() { //original
  public Bitmap renderCroppedGreyscaleBitmap(int destX,int destY) { //여기서 좌표값에 대한 컬러 값을 처리해서 넘겨주자 KYT 
    int width = getWidth();
    int height = getHeight();
    float m=0;
    int max=0;
    int[] pixels = new int[width * height]; //KYT remove grtey 변환용 
    byte[] yuv = yuvData;
   // int sz = width * height; //KYT
    // Decode Yuv data to integer array
    decodeYUV420SP(pixels, yuv, width, height); //KYT yuv -> rgb
    Log.e("renderCropYUV", "YUV size="+Integer.toString(yuv.length));
    Log.e("renderCropYUV", "image()="+yuv[destX*destY]); //KYT
   
    m= pixels[destX*destY];
   
    ResultPointABC points = new ResultPointABC();//KYT
    
    
    int x,y;
    
    for(x=0;x<2;x++)for(y=0;y<2;y++){
    	
    	if(pixels[(destX-1)*(destY-1)]>max)max=pixels[(destX-1)*(destY-1)];
    	
    }
    
   
    //if(m>230)points.setfalse(); //KYT 거짓으로 저장 
    //else points.settrue();

    /*  //KYT remove grey 변환용
    int inputOffset = top * dataWidth + left;

    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      for (int x = 0; x < width; x++) {
        int grey = yuv[inputOffset + x] & 0xff;
        pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
       
      }
      inputOffset += dataWidth;
    }
    */
     
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //original
  //  Bitmap bitmap = BitmapFactory.decodeByteArray(yuvData, 0, width*height);  //KYT
     // Log.e("renderCropYUV", "RGB size="+Integer.toString(pixels.length));
      bitmap.setPixels(pixels, 0, width, 0, 0, width, height); //KYT remove grey 변환용 original
     // bitmap.setPixels(mIntArray, 0, width, 0, 0, width, height); //KYT RGB 변환
      
    return bitmap;
  }

  public Bitmap renderCroppedGreyscaleBitmap2() { //체크섬 에러시 더미 리턴용  KYT 
	    int width = getWidth();
	    int height = getHeight();
	    float m=0;
	    int max=0;
	    int[] pixels = new int[width * height]; //KYT remove grtey 변환용 
	    byte[] yuv = yuvData;
	   // int sz = width * height; //KYT
	    // Decode Yuv data to integer array
	    
	      
	    //if(m>230)points.setfalse(); //KYT 거짓으로 저장 
	    //else points.settrue();

	    /*  //KYT remove grey 변환용
	    int inputOffset = top * dataWidth + left;

	    for (int y = 0; y < height; y++) {
	      int outputOffset = y * width;
	      for (int x = 0; x < width; x++) {
	        int grey = yuv[inputOffset + x] & 0xff;
	        pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
	       
	      }
	      inputOffset += dataWidth;
	    }
	    */
	     
	      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //original
	  //  Bitmap bitmap = BitmapFactory.decodeByteArray(yuvData, 0, width*height);  //KYT
	     // Log.e("renderCropYUV", "RGB size="+Integer.toString(pixels.length));
	      bitmap.setPixels(pixels, 0, width, 0, 0, width, height); //KYT remove grey 변환용 original
	     // bitmap.setPixels(mIntArray, 0, width, 0, 0, width, height); //KYT RGB 변환
	      
	    return bitmap;
	  }
  
  private void reverseHorizontal(int width, int height) {
    byte[] yuvData = this.yuvData;
    for (int y = 0, rowStart = top * dataWidth + left; y < height; y++, rowStart += dataWidth) {
      int middle = rowStart + width / 2;
      for (int x1 = rowStart, x2 = rowStart + width - 1; x1 < middle; x1++, x2--) {
        byte temp = yuvData[x1];
        yuvData[x1] = yuvData[x2];
        yuvData[x2] = temp;
      }
    }
  }

}
