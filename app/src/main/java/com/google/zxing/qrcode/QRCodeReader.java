/*
 * Copyright 2007 ZXing authors
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

package com.google.zxing.qrcode;

import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointABC;  //KYT
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.detector.Detector;//qr코드 패턴을 찾을때
//import com.google.zxing.datamatrix.decoder.Decoder; 
//import com.google.zxing.datamatrix.detector.Detector; //데이타메트릭스 패턴을 찾을때

import java.util.List;
import java.util.Map;

/**
 * This implementation can detect and decode QR Codes in an image.
 *
 * @author Sean Owen
 */
public class QRCodeReader implements Reader {

  private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
  
  private final Decoder decoder = new Decoder();

  protected Decoder getDecoder() {
    return decoder;
  }

  /**
   * Locates and decodes a QR code in an image.
   *
   * @return a String representing the content encoded by the QR code
   * @throws NotFoundException if a QR code cannot be found
   * @throws FormatException if a QR code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
 
  public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
    return decode(image, null);
  }

  
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, ChecksumException, FormatException {
    DecoderResult decoderResult;
    ResultPoint[] points;
   
    ResultPointABC pointsABC = new ResultPointABC();//KYT
    
    /*
     
    if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
      BitMatrix bits = extractPureBits(image.getBlackMatrix());
      decoderResult = decoder.decode(bits, hints);
      points = NO_POINTS;
    } else {
      DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints); //패턴을 찾아 좌표를 리턴하여 이미지 정렬및 비트 추출   
      decoderResult = decoder.decode(detectorResult.getBits(), hints);//detectorResult.getBits() -> bits 를 리턴함 
      points = detectorResult.getPoints();
   // 코드의 .. 갯수 볼려고 추가 
      Log.d("QrcodeReader실행", "포인트갯수 :"+points.length+"  hints :"+hints);
      		
      //
    }*/
    
    /*    //datamatrix
    if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
        BitMatrix bits = extractPureBits(image.getBlackMatrix());
        decoderResult = decoder.decode(bits);
        points = NO_POINTS;
      } else {
        DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect();
        decoderResult = decoder.decode(detectorResult.getBits());//detectorResult.getBits() -> bits 를 리턴함 
        points = detectorResult.getPoints();
      }
    
      Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points,BarcodeFormat.DATA_MATRIX);
      List<byte[]> byteSegments = decoderResult.getByteSegments();
      if (byteSegments != null) {
        result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
      }*/
    //*
     
    DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(); //패턴을 찾아 좌표를 리턴하여 이미지 정렬및 비트 추출
    
   // Log.d("decoderResult들거마", "decoderResult 들어간다");    
   
	 decoderResult = decoder.decode(detectorResult.getBits());//detectorResult.getBits() -> bits 를 리턴함 
	 
	 pointsABC.clrCodedetect(); //KYT 
	   
   // Log.d("pointst들거마", "points 들어간다");    
   
   
    points = detectorResult.getPoints(); 
  
     
    // 코드의 .. 갯수 볼려고 추가 
   // Log.e("codeReader실행", "포인트갯수 :"+points.length+"  hints :"+hints);
 //   if(points.length==4)Log.d("포인트", "포인트1 :"+points[0]+"포인트2 :"+points[1]+"포인트3 :"+points[2]+"포인트4 :"+points[3]);
//    else Log.d("포인트", "포인트1 :"+points[0]+"포인트2 :"+points[1]+"포인트3 :"+points[2]);
 //   Log.e("포인트", "포인트1 :"+points[0]+"포인트2 :"+points[1]+"포인트3 :"+points[2]); //KYT
    
 //   Log.e("image바이너리", "image length(width,height)="+image.getWidth()+","+image.getHeight()); //KYT
    
   
    
    points = NO_POINTS;
  
    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
    List<byte[]> byteSegments = decoderResult.getByteSegments();
    if (byteSegments != null) {
      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
            
    }//*/
   
    String ecLevel = decoderResult.getECLevel();
    if (ecLevel != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
    }
   
    return result;
  }

  
  public void reset() {
    // do nothing
  }

  /**
   * This method detects a code in a "pure" image -- that is, pure monochrome image
   * which contains only an unrotated, unskewed, image of a code, with some white border
   * around it. This is a specialized method that works exceptionally fast in this special
   * case.
   *
   * @see com.google.zxing.pdf417.PDF417Reader#extractPureBits(BitMatrix)
   * @see com.google.zxing.datamatrix.DataMatrixReader#extractPureBits(BitMatrix)
   */
  private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {

    int[] leftTopBlack = image.getTopLeftOnBit();
    int[] rightBottomBlack = image.getBottomRightOnBit();
    if (leftTopBlack == null || rightBottomBlack == null) {
      throw NotFoundException.getNotFoundInstance();
    }

    float moduleSize = moduleSize(leftTopBlack, image);

    int top = leftTopBlack[1];
    int bottom = rightBottomBlack[1];
    int left = leftTopBlack[0];
    int right = rightBottomBlack[0];

    if (bottom - top != right - left) {
      // Special case, where bottom-right module wasn't black so we found something else in the last row
      // Assume it's a square, so use height as the width
      right = left + (bottom - top);
    }

    int matrixWidth = Math.round((right - left + 1) / moduleSize);
    int matrixHeight = Math.round((bottom - top + 1) / moduleSize);
    if (matrixWidth <= 0 || matrixHeight <= 0) {
      throw NotFoundException.getNotFoundInstance();
    }
    if (matrixHeight != matrixWidth) {
      // Only possibly decode square regions
      throw NotFoundException.getNotFoundInstance();
    }

    // Push in the "border" by half the module width so that we start
    // sampling in the middle of the module. Just in case the image is a
    // little off, this will help recover.
    int nudge = Math.round(moduleSize / 2.0f);
    top += nudge;
    left += nudge;

    // Now just read off the bits
    BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
    for (int y = 0; y < matrixHeight; y++) {
      int iOffset = top + (int) (y * moduleSize);
      for (int x = 0; x < matrixWidth; x++) {
        if (image.get(left + (int) (x * moduleSize), iOffset)) {
          bits.set(x, y);
        } 
      }
    }
    return bits;
  }

  private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
    int height = image.getHeight();
    int width = image.getWidth();
    int x = leftTopBlack[0];
    int y = leftTopBlack[1];
    boolean inBlack = true;
    int transitions = 0;
    while (x < width && y < height) {
      if (inBlack != image.get(x, y)) {
        if (++transitions == 5) {
          break;
        }
        inBlack = !inBlack;
      }
      x++;
      y++;
    }
    if (x == width || y == height) {
      throw NotFoundException.getNotFoundInstance();
    }
    return (x - leftTopBlack[0]) / 7.0f;
  }

}