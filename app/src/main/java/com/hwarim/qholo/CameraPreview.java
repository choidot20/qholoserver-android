package com.hwarim.qholo;
import android.view.View;
import android.content.Context;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class  CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	String TAG = "Q-Holo";
    private SurfaceHolder mHolder;
    private Camera mCamera;



	public CameraPreview(Context context) {
		super(context);

		Log.e(TAG, "Preview class created");

        // SurfaceHolder
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void surfaceCreated(SurfaceHolder holder) {


        DisplayMetrics displayMetrics = new DisplayMetrics();


    	Log.e(TAG,"surfaceCreated!");
    	// Surface (holder
        try {



        	//mCamera.unlock();

			mCamera.setPreviewDisplay(holder);


			//=================== KYT
			int m_resWidth = 0;

			int m_resHeight = 0;
			 try{
			m_resWidth = mCamera.getParameters().getPictureSize().width;

		    m_resHeight = mCamera.getParameters().getPictureSize().height;



			   Camera.Parameters parameters = mCamera.getParameters();
                 parameters.set("orientation", "portrait");
                 mCamera.setDisplayOrientation(90);
             //    parameters.setRotation(90);
			//아래 숫자를 변경하여 자신이 원하는 해상도로 변경한다

			    //m_resWidth = 1280;
			    //m_resHeight = 720;

                 //m_resWidth = 4608;
                 //m_resHeight = 3456;

                // m_resWidth = 1948;
                // m_resHeight = 1098;




               // m_resWidth = 4000;        //샤오미
               // m_resHeight = 2250;       //샤오미

             m_resWidth = 3264;     //20180406
			m_resHeight = 1836;  //16:9    20180406



                 //m_resWidth =2322;
                 //m_resHeight =4138;    //노트3 2322  4138
                 parameters.setPictureSize(m_resWidth, m_resHeight);

			  //  parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
			    //parameters.setZoom(parameters.getMaxZoom()/8); //  0이 가장 멀리 있는상태        //샤오미 8 //나머지는 4
			    parameters.setZoom(50);  //30                                                     //샤오미 10 //나머지 50

				  mCamera.setParameters(parameters);
				  } catch (RuntimeException e) {
		            Log.e(TAG, "Error RuntimeException: " + e.getMessage());
		        }
			// end KYT



            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.e(TAG,"Surface Destroyed");

       if (mCamera != null) {
           // mCamera.stopPreview();
            Log.e(TAG,"Preview stopped");
        }
    }

    public void setCamera(Camera camera) {
    	if((mCamera != null)&&(camera == null)){
    		mCamera.stopPreview();

		}
    		mCamera = camera;

    }

    private Camera.Size getBestPreviewSize(int width, int height)
    {
		Camera.Size result = null;
		try{
			Camera.Parameters p = mCamera.getParameters();

			for (Camera.Size size : p.getSupportedPreviewSizes()) {
				if (size.width <= height && size.height <= width) {
					if (result == null) {
						result = size;
					} else {
						int resultArea = result.width * result.height;
						int newArea = size.width * size.height;

						if (newArea > resultArea) {
							result = size;
						}
					}
				}

			}
		 } catch (RuntimeException e) {
	            Log.e(TAG, "Error RuntimeException: " + e.getMessage());
	        }
        return result;

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
      	Log.e(TAG,"Surface Changed");
        if (mHolder.getSurface() == null){
          return;
        }

         try {
            mCamera.stopPreview();

        } catch (Exception e){
         }

      try{
         Camera.Parameters parameters = mCamera.getParameters();

         Camera.Size size = getBestPreviewSize(w, h);
    	Log.e(TAG,"Surface Size w="+size.width+" h="+ size.height);
        parameters.setPreviewSize(size.width, size.height);

      if (parameters.getSupportedFlashModes() != null && parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)){
		   parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }

        mCamera.setParameters(parameters);

       } catch (RuntimeException e) {
           Log.e(TAG, "Error RuntimeException: " + e.getMessage());
       }

        requestLayout();
        try {
            mCamera.setPreviewDisplay(mHolder);

            mCamera.startPreview();

        } catch (Exception e){
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }




}

