package com.hwarim.qholo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CaptureActivity extends Activity {
    static String TAG = "Q-Holo";
    private Context mContext 				= this;
    FrameLayout preview;
    private Camera mCamera;
    private CameraPreview mPreview;
    private RadioButton mCheck;
    private ImageButton mTakePhoto;
    private boolean isPhotoTaken 			= false;
    private boolean isFocused 				= false;
    private boolean errorFound 				= false;
    private boolean isshutter                = false;
    private int cameraId 					= -1;
    public boolean Sock_result 		    = false;
    private int Jresult                     = 0;
    private boolean S_error                = false;
    public int count = 0;
    public String receivedPacket ="";

    public static String savedPath;

    private Display mDisplay;
    public static int deviceWidth, deviceHeight;

    byte[] cropdata;
    //String ip="192.168.219.170";
    String ip="172.19.99.179";
    //String ip="192.168.0.20";
    // String ip="10.10.17.183";
    //String ip="114.108.177.173";
    //String ip="192.168.43.55";


    int port=5005;
    Socket socket=null;
    Socket socket2=null;
    BufferedReader br=null;
    BufferedWriter bw=null;

    private BufferedReader networkReader;
    private BufferedWriter networkWriter;

    ByteArrayInputStream  bais= null;
    // DataOutputStream dos = null;
    BufferedOutputStream bos  = null;
    BufferedInputStream bis   = null;
    DataInputStream dis       = null;
    DataOutputStream dos      = null;
    OutputStream os           = null;
    FileInputStream fis       = null;
    ObjectOutputStream oos    = null;
    //FileOutputStream fos;
/*
	 @Override
	    protected void onStop() {
	        super.onStop();
	    //    try {
	           // socket.close();
	      //  } catch (IOException e) {
	          //  e.printStackTrace();
	      //  }
	    }

	 public void setSocket(String ip, int port) throws IOException {

	        try {
	            socket = new Socket(ip, port);
	            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	            networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        } catch (IOException e) {
	           // System.out.println(e);
	           // e.printStackTrace();
	        }

	    }
*/
    //String ip="116.122.22.13";
    //String ip="172.30.1.14";

    //String ip="175.115.106.149";

    //String ip="10.10.15.91";
    private Bitmap imgRotate(Bitmap bitmap, int angle){

        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
        bitmap.recycle();

        return resizedBitmap;

    }
    public static Bitmap cropCenterBitmap(Bitmap src, int w, int h) {
        if(src == null)
            return null;

        int width = src.getWidth();
        int height = src.getHeight();

        if(width < w && height < h)
            return src;

        int x = 0;
        int y = 0;

        if(width > w)
            x = (width - w)/2;

        if(height > h)
            y = (height - h)/2;

        int cw = w; // crop width
        int ch = h; // crop height

        if(w > width)
            cw = width;

        if(h > height)
            ch = height;

        return Bitmap.createBitmap(src, x, y, cw, ch);

    }

    public byte[] bitmapToByteArray( Bitmap $bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        $bitmap.compress( CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;

        return byteArray ;
    }
/*    public void restart()
    {
        startActivity(new Intent(this, CaptureActivity.class));
    }
*/

    public void resultWaiting(){

        Toast.makeText(mContext, receivedPacket, Toast.LENGTH_SHORT)
                .show();

        if(S_error==false){

            if(Jresult==1){ //OK
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);


                View layout = inflater.inflate(R.layout.confirm2,(ViewGroup) findViewById(R.id.popup));

                final AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

                aDialog.setTitle("판독결과"); //타이틀바 제목
                aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅

                //그냥 닫기버튼을 위한 부분

                aDialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                ImageView iv = (ImageView)layout.findViewById(R.id.image);
                iv.setImageResource(R.drawable.o1);
                //iv.setImageBitmap(sizingBmp);


                //팝업창 생성
                AlertDialog ad = aDialog.create();

                ad.show();//보여줌
                receivedPacket = null;
                //              Jresult=0;
              /*  try {
                    dos.close();
                    oos.close();
                    br.close();
                    //dis.close();
                    socket.close();
                    Log.e("전송스레드","소켓닫음");


                } catch(Exception ie) {
                    Log.e("전송스레드","소켓닫는중 에러발생=>"+ie.toString());
                }
                Log.e("전송스레드","소켓닫음");
                */

            }

            if(Jresult==2) { //"FAKE"
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                View layout = inflater.inflate(R.layout.confirm3,(ViewGroup) findViewById(R.id.popup));


                AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

                aDialog.setTitle("판독결과"); //타이틀바 제목
                aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅

                //그냥 닫기버튼을 위한 부분

                aDialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                ImageView iv = (ImageView)layout.findViewById(R.id.image);
                iv.setImageResource(R.drawable.x1);

                //iv.setImageBitmap(sizingBmp);

                //팝업창 생성
                AlertDialog ad = aDialog.create();

                ad.show();//보여줌!
                receivedPacket = null;


                //             Jresult=0;
             /*   try {
                    dos.close();
                    oos.close();
                    br.close();
                    //dis.close();
                    socket.close();
                    Log.e("전송스레드","소켓닫음");
                } catch(Exception ie) {
                    Log.e("전송스레드","소켓닫는중 에러발생=>"+ie.toString());
                }
                Log.e("전송스레드","소켓닫음");
*/
            }

            if(Jresult==3) { //"RECHECK"
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디

                View layout = inflater.inflate(R.layout.confirm1,(ViewGroup) findViewById(R.id.popup));

                AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

                // aDialog.setTitle("히든스탯 목록"); //타이틀바 제목
                aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅

                //TextView text1= (TextView)layout.findViewById(R.id.textview1);

                // text1.setText("코드를 다시 촬영해 주세요.\n 플래시의 영향을 받을 수 있으므로 아래 사진과 같이 각도를 약간 기울여 플래시 반사를 없애고 촬영 하여 주시기 바랍니다. 계속 시도해도 이 화면이 표시된다면 이 코드는 정품코드가 아닙니다!");

                aDialog.setNegativeButton("다시찍기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                ImageView iv = (ImageView)layout.findViewById(R.id.image);

                iv.setImageResource(R.drawable.recheck5);
                //팝업창 생성
                AlertDialog ad = aDialog.create();

                ad.show();//보여줌!
                receivedPacket = null;
                //              Jresult=Jresult;


            }
            if(Jresult==4) { //"RERECHECK"
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디

                View layout = inflater.inflate(R.layout.confirm4,(ViewGroup) findViewById(R.id.popup));

                AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

                // aDialog.setTitle("히든스탯 목록"); //타이틀바 제목
                aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅

                //TextView text1= (TextView)layout.findViewById(R.id.textview1);

                //text1.setText("코드를 다시 촬영해 주세요.\n 플래시의 영향을 받을 수 있으므로 아래 사진과 같이 각도를 약간 기울여 플래시 반사를 없애고 촬영 하여 주시기 바랍니다. 계속 시도해도 이 화면이 표시된다면 이 코드는 정품코드가 아닙니다!");

                aDialog.setNegativeButton("다시찍기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                ImageView iv = (ImageView)layout.findViewById(R.id.image);

                iv.setImageResource(R.drawable.srecheck1);
                //팝업창 생성
                AlertDialog ad = aDialog.create();

                ad.show();//보여줌!
                receivedPacket = null;
                //             Jresult=0;

        /*        onLowMemory();
                if(count < 10)
                {
                    finish();
                    startActivity(new Intent(this, CaptureActivity.class));
                }*/
            }
            if(Jresult==5) { //"RECHECK2"
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디

                View layout = inflater.inflate(R.layout.confirm5,(ViewGroup) findViewById(R.id.popup));

                AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

                // aDialog.setTitle("히든스탯 목록"); //타이틀바 제목
                aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅


                //TextView text1= (TextView)layout.findViewById(R.id.textview1);

                // text1.setText("코드를 다시 촬영해 주세요.\n 플래시의 영향을 받을 수 있으므로 아래 사진과 같이 각도를 약간 기울여 플래시 반사를 없애고 촬영 하여 주시기 바랍니다. 계속 시도해도 이 화면이 표시된다면 이 코드는 정품코드가 아닙니다!");

                aDialog.setNegativeButton("다시찍기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //               count = count + 1;
                    }
                });

                ImageView iv = (ImageView)layout.findViewById(R.id.image);

                iv.setImageResource(R.drawable.srecheck2);
                //팝업창 생성
                AlertDialog ad = aDialog.create();

                ad.show();//보여줌!
                receivedPacket = null;

                //               Jresult=0;

            }
        }else {
            S_error=false;

            Context mContext = getApplicationContext();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.rwaiting,(ViewGroup) findViewById(R.id.popup));


            AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

            aDialog.setTitle("서버에러"); //타이틀바 제목
            aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅

            //그냥 닫기버튼을 위한 부분

            aDialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            // ImageView iv = (ImageView)layout.findViewById(R.id.image);

            //iv.setImageBitmap(sizingBmp);

            //팝업창 생성
            AlertDialog ad = aDialog.create();

            ad.show();//보여줌!
        }

    }


    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // JPEG
            Log.d(TAG, "4.PictureCallback");
            //int width=1100,height=1100;
            int width = 900, height = 900;
            isPhotoTaken = true;
            mCheck.setChecked(false);
            mCamera.startPreview();


            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); //여기서 사진을 처리,. 사진으 JPG 형식으로 들어오기 때문에 비트맵으로 변환해 주거나 바로 크롭

            Bitmap resizebitmap = imgRotate(bitmap, 90);
            Bitmap bitmapcroped = cropCenterBitmap(resizebitmap, width, height);

            int[] intArray = new int[width * height];
            //copy pixel data from the Bitmap into the 'intArray' array
            bitmapcroped.getPixels(intArray, 0, width, 0, 0, width, height);
            ;

            //byte[] cropdata;
            // LuminanceSource source =new BufferedImageLuminanceSource(bitmapcroped); //안드로이드에서는 버퍼드이미지를 지원하지 않음. android not support BufferdImage
       /*     LuminanceSource source = new RGBLuminanceSource(width, height,intArray);

            // Bitmap source=grayScale(bitmapcroped);
            // PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height); //YcbCr422_sp
            BinaryBitmap bbitmap = new BinaryBitmap(new HybridBinarizer(source));

            ResultPointABC pointsABC = new ResultPointABC();//KYT

            pointsABC.setABC(0, 0, 0, 0, 0, 0,0,0); //KYT RESET

            QRCodeReader reader=new QRCodeReader();

            try {

                //rawResult = multiFormatReader.decodeWithState(bitmap); /KYT
                rawResult = reader.decode(bbitmap);  //QRCodeEwader.java

                if(rawResult.getText()!=null){
                    Log.e("찾은값",">>"+rawResult.getText());

                }
                else {

                }
            } catch (ReaderException re) {
                //비트매트릭스는 찾았지만 리턴 텍스트는 없을 경우   //KYT 추가

                //Log.e(TAG, "이건 가짜임"); //KYT
                // continue
            } finally {

            }*/

            Context mContext = getApplicationContext();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

            //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디

            View layout = inflater.inflate(R.layout.confirm1, (ViewGroup) findViewById(R.id.popup));

            AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

            // aDialog.setTitle("히든스탯 목록"); //타이틀바 제목
            aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅



            /*if((int)pointsABC.getBx()==0||(int)pointsABC.getBy()==0||(int)pointsABC.getCx()==0||(int)pointsABC.getCy()==0||(int)pointsABC.getAx()==0||(int)pointsABC.getAy()==0){

                //cropdata=bitmapToByteArray(bitmapcroped);
                //Bitmap sizingBmp = Bitmap.createScaledBitmap(bitmapcroped, 800, 800, true); //이미지뷰에 넣을 비트맵 너무크면 에러나기 때문에




                aDialog.setNegativeButton("다시찍기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                ImageView iv = (ImageView)layout.findViewById(R.id.image);

                iv.setImageResource(R.drawable.recheck2);
                //팝업창 생성
                AlertDialog ad = aDialog.create();

                ad.show();//보여줌!
            } //좌표 하나라도 0이면 가짜
*/
            //cropdata=bitmapToByteArray(cropCenterBitmap(bitmap,1100,1100));


            cropdata = bitmapToByteArray(bitmapcroped);

            Sock_result = false;


  /*          if(count == 0){
                new ImageSaveTask().execute(cropdata);

                new ServerWaitingTask().execute(cropdata);

                while (Sock_result == false) ;
                resultWaiting();
                receivedPacket = null;
                count = 1;


            }*/
//            else if(count == 1){
            new ImageSaveTask().execute(cropdata);

            new ServerWaitingTask().execute(cropdata);

            try {
                while (Sock_result == false) ;
                resultWaiting();
            } catch (Exception e) {
            }




//            }



            //  Log.e("패턴위치>", "topLeft:"+(int)pointsABC.getBx()+","+(int)pointsABC.getBy()+"  topRight:"+(int)pointsABC.getCx()+","
            // 		+ ""+(int)pointsABC.getCy()+",  bottomLeft:"+(int)pointsABC.getAx()+","+(int)pointsABC.getAy()+
            //	  " Align:"+(int)pointsABC.getDx()+","+(int)pointsABC.getDy());
            //new ImageSaveTask().execute(data);
            //new ImageSaveTask().execute(cropdata);
        }


    };

    private AutoFocusCallback mFocus = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

            if (success) {
                mCheck.setChecked(true);
                isFocused = true;
            } else
                mCheck.setChecked(false);
        }
    };
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter(){
            isshutter = true;

            if(isshutter){
                CheckTypesTask task = new CheckTypesTask();
                task.execute();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);



        DrawOnTop mDraw = new DrawOnTop(this); //KYT

        setContentView(R.layout.activity_main);

        addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        deviceHeight = mDisplay.getHeight();
        deviceWidth = mDisplay.getWidth();


        Log.e(TAG, "W // " + deviceWidth + "   H // " + deviceHeight);

        mContext = this;

        mCheck = (RadioButton) findViewById(R.id.focus);
        mCheck.setChecked(false);
        mCheck.setClickable(false);



        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디

        View layout = inflater.inflate(R.layout.flash,(ViewGroup) findViewById(R.id.popup));

        AlertDialog.Builder aDialog = new AlertDialog.Builder(CaptureActivity.this);

        aDialog.setTitle("Tutorial"); //타이틀바 제목
        aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅

        //TextView text1= (TextView)layout.findViewById(R.id.textview1);

        // text1.setText("코드를 다시 촬영해 주세요.\n 플래시의 영향을 받을 수 있으므로 아래 사진과 같이 각도를 약간 기울여 플래시 반사를 없애고 촬영 하여 주시기 바랍니다. 계속 시도해도 이 화면이 표시된다면 이 코드는 정품코드가 아닙니다!");

        aDialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ImageView iv = (ImageView)layout.findViewById(R.id.image);

        iv.setImageResource(R.drawable.tutoria);
        //팝업창 생성
        AlertDialog ad = aDialog.create();

        ad.show();//보여줌!

    }
    class DrawOnTop extends View { //KYT

        public DrawOnTop(Context context) {

            super(context);



        }

        @Override

        protected void onDraw(Canvas canvas) {


            //int boxsize=deviceHeight/13;//화웨이
            //int boxsize  = deviceHeight/8;//s5
            //int boxsize  = deviceHeight/11; //샤오미
            //int boxsize=deviceHeight/9;//S7

            int boxsize=deviceHeight/9;
            int centerW=deviceWidth/2;
            int centerH=deviceHeight/2;
            Paint paint = new Paint();

            paint.setTextSize(70);

            // canvas.drawText("코드를 사각형 안에 위치시킨후 가운데 파란 원이 나타날 때 까지 OK를 누르세요",centerW-(centerW/2+40), 80, paint); // 텍스트 표시

            paint.setColor(Color.WHITE);
            canvas.drawText("코드를 사각형 안에 위치시킨 후 ",centerW-(centerW/10+330), 160, paint); // 텍스트 표시
            canvas.drawText("버튼을 꾹 누르고 초점이 맞으면 손을 떼어주세요",centerW-(centerW/2+280), 250, paint); // 텍스트 표시

            paint.setStyle(Paint.Style.STROKE); //빈 스타일
            //  paint.setStyle(Paint.Style.FILL); //채워진 스타일

            paint.setStrokeWidth(5);                     // 크기 10

            //canvas.drawRect(centerW - boxsize, centerH - boxsize, centerW + boxsize, centerH + boxsize, paint);    // 라인그리기

            canvas.drawRect(centerW - (boxsize - 50), centerH - (boxsize - 50),  centerW + (boxsize - 50), centerH + (boxsize - 50), paint);
/*
            canvas.drawRect(centerW - (boxsize - 50), centerH - (boxsize - 50), centerW - (boxsize - 120), centerH - (boxsize - 120), paint);    // 라인그리기
            canvas.drawRect(centerW + (boxsize - 120), centerH - (boxsize - 50), centerW + (boxsize - 50), centerH - (boxsize - 120), paint);    // 라인그리기
            canvas.drawRect(centerW - (boxsize - 50), centerH + (boxsize - 120), centerW - (boxsize - 120), centerH + (boxsize - 50), paint);
            canvas.drawRect(centerW + (boxsize - 100), centerH + (boxsize - 100), centerW + (boxsize - 70), centerH + (boxsize - 70), paint);    // 라인그리기
*/
	          /*
	          canvas.drawRect(centerW-boxsize,centerH-boxsize, centerW+boxsize, centerH+boxsize, paint);    // 라인그리기
	          canvas.drawRect(centerW-(boxsize-100),centerH-(boxsize-100), centerW-(boxsize-170), centerH-(boxsize-170), paint);    // 라인그리기
	          canvas.drawRect(centerW+(boxsize-170),centerH-(boxsize-100), centerW+(boxsize-100), centerH-(boxsize-170), paint);    // 라인그리기
	          canvas.drawRect(centerW-(boxsize-100),centerH+(boxsize-170), centerW-(boxsize-170), centerH+(boxsize-100), paint);
	          canvas.drawRect(centerW+(boxsize-150),centerH+(boxsize-150), centerW+(boxsize-120), centerH+(boxsize-120), paint);    // 라인그리기
	    */


            super.onDraw(canvas);

        }
    }


    private boolean checkCameraHardware(Context context) {

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            return true;
        } else {

            Toast.makeText(mContext, "No camera found!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public Camera getCameraInstance(int id) {

        Camera c = null;
        try {
            releaseCameraAndPreview();
            c = Camera.open(id);
            Log.e(TAG,"><>< Camera resource opened successfully ><><");
        } catch (Exception e) {
            //  null
            e.printStackTrace();
        }
        return c;
    }

    private void releaseCameraAndPreview() {

        if(mPreview != null){
            //mPreview.setCamera(null);

            mPreview = null;
        }
        if (mCamera != null) {

            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onResume() {

        super.onResume();


        if (checkCameraHardware(mContext)) {

            mCamera = getCameraInstance(0);

            preview = (FrameLayout) findViewById(R.id.camera_preview);

            mPreview = new CameraPreview(this);

            mPreview.setCamera(mCamera);

            preview.addView(mPreview);


            mTakePhoto = (ImageButton)findViewById(R.id.touchListener);

            mTakePhoto.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    int action = event.getAction();

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:

                            mCamera.autoFocus(mFocus);

                            break;
                        case MotionEvent.ACTION_UP:
                            // JPEG:
                            if (isFocused) {//

                                // mCamera.takePicture(shutterCallback, null, mPicture);
                                mCamera.takePicture(null, null, mPicture);

                            }



                            break;
                    }
                    return false;
                }
            });

        } else if(CameraInfo.CAMERA_FACING_FRONT > -1){
            //
            try {
                cameraId = CameraInfo.CAMERA_FACING_FRONT;
                mCamera = Camera.open(cameraId);
            } catch (Exception e) {
                errorFound = true;
            }
            if (errorFound = true) {
                try {
                    mCamera = Camera.open(0);
                    cameraId = 0;
                } catch (Exception e) {
                    cameraId = -1;
                }
            }
        } else {
            //
            Toast.makeText(mContext, "no camera on this device!",Toast.LENGTH_SHORT).show();
            finish();
        }
		/*
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		mPreview = new CameraPreview(this);
		mPreview.setCamera(mCamera);

		preview.addView(mPreview);

		//
		mTakePhoto = (Button) findViewById(R.id.touchListener);
		mTakePhoto.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();

				switch (action) {
				case MotionEvent.ACTION_DOWN:

					mCamera.autoFocus(mFocus);
					break;
				case MotionEvent.ACTION_UP:
					// JPEG
					if (isFocused) //
						mCamera.takePicture(null, null, mPicture);
					break;

				}
				return false;
			}
		});
		*/
    }
    public void OnClick(View v) {
        // myOnClick ( activity_main.xml )
        if (v.getId() == R.id.flashoff) {
            Camera.Parameters mCameraParameter = mCamera.getParameters();
            mCameraParameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mCameraParameter);
            mCamera.startPreview();
        }
        else if(v.getId()==R.id.flashon){
            Camera.Parameters mCameraParameter = mCamera.getParameters();
            mCameraParameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mCameraParameter);
            mCamera.startPreview();
        }

    }





    @Override
    public void onPause() {

        super.onPause();

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFlashModes() != null && parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)){
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        releaseCameraAndPreview();
        preview.removeAllViews();

    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                CaptureActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("라벨 판별 중..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                for (int i = 0; i < 5; i++) {
                    //asyncDialog.setProgress(i * 30);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }

    }

    /**
     *
     */
    class ImageSaveTask extends AsyncTask<byte[], Void, Boolean> {



        /* */
        @Override
        protected Boolean doInBackground(byte[]... data) {


            //네트워크로 전송 할 때
            try { //여기서 소켓을 연다
                Log.d(TAG,"Socket 생성");
                socket=new Socket(ip,5005);
                socket.setTcpNoDelay(true);//TCP딜레이 제거  2012-01-15
                socket. setPerformancePreferences(1, 2, 0); //네트워크 우선순위
                // 데이터 전송용 스트림 생성


            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG,"소켓을 생성 할 수 없습니다.");
            }

            try {

                dos = new DataOutputStream(socket.getOutputStream());

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HHmmss");
                String date = dateFormat.format(new Date());
                Log.e(TAG,"파일 전송 작업을 시작합니다.");
                // 파일 이름 전송

                String fName = "Label_"+date+".jpg";
                dos.writeUTF(fName);

                Log.e("전송스레드","파일 이름("+fName+")을 전송하였습니다.");

                // 파일 내용을 읽으면서 전송
                // File f = new File(fName);
                // fis = new FileInputStream(f);
                // bis = new BufferedInputStream(fis);

                int len=data[0].length;
                Log.d(TAG,"13.1");
		            /*
		            bais = new ByteArrayInputStream(data[0]);

		            Log.e("전송스레드","보낼 사이즈 : " + len);
		            // while ((len = bis.read(data[0])) != -1) {
		            while ((len = bais.read(data[0])) >0) {
		                dos.write(data[0], 0, len);
		            }
		            dos.flush();
		            */
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.reset();
                oos.writeObject(data[0]);
                oos.flush();

                //dos.close(); //수신도 받아야 하기 때문에 이건 나중에
                //bis.close();

                //bais.close();

                //fis.close();
                Log.e("전송스레드","파일 전송 작업을 완료하였습니다.");
                Log.e("전송스레드","보낸 파일의 사이즈 : " + len);
            } catch (IOException e) {
                // e.printStackTrace();
                Log.e(TAG,e.toString());
            }


          /*  //파일로 저장할때
			File pictureFile = getOutputMediaFile();
			if (pictureFile == null) {
				return false;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data[0]);
				fos.close();



				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

			    Uri contentUri = Uri.fromFile(pictureFile);
			    mediaScanIntent.setData(contentUri);
			    mContext.sendBroadcast(mediaScanIntent);

			    Log.e(TAG, "picture loading path : " + pictureFile.getPath()+"/MyCamera/");

			} catch (IOException e) {
				return false;
			}
			*/
            return true;
        }


        @Override
        protected void onPostExecute(Boolean isDone) {

            if (isDone) {

                //         Toast.makeText(mContext, "Transfer Success!", Toast.LENGTH_SHORT).show();
            }
        }


        private File getOutputMediaFile() {
            // SD
            // Environment.getExternalStorageState()

            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyCameraApp");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "failed to create directory");
                    return null;
                }
            }


            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            File mediaFile;

            savedPath = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
            mediaFile = new File(savedPath);
            Log.e(TAG,"Saved at"+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

            return mediaFile;
        }
    }


    class ServerWaitingTask extends AsyncTask<byte[], Void, Boolean> {



        /* */
        @Override
        protected Boolean doInBackground(byte[]... data) {


            try {
                Log.e("전송스레드","서버로부터 결과 기다리는 중.");

                br=new BufferedReader(new InputStreamReader( socket.getInputStream()));

                receivedPacket=br.readLine(); //ID값 수신대기

                if( receivedPacket != null){
                    if(receivedPacket.equals("OK")){
                        Sock_result = true;
                        Jresult=1;
                    }

                    if(receivedPacket.equals("FAKE")){
                        Sock_result = true;
                        Jresult=2;
                    }

                    if(receivedPacket.equals("RECHECK")){
                        Sock_result=true;
                        Jresult=3;
                    }
                    if(receivedPacket.equals("RERECHECK")){
                        Sock_result=true;
                        Jresult=4;
                    }
                    if(receivedPacket.equals("RECHECK2")){
                        Sock_result=true;
                        Jresult=5;
                    }
                } //else S_error=true;




                // br1.close();
            } catch (IOException e) {	Log.e(TAG,e.toString());
                //S_error=true;
            }/*finally {
                try {
                    dos.close();
                    oos.close();
                    br.close();
                    //dis.close();
                    socket.close();
                    Log.e("전송스레드","소켓닫음");
                } catch(Exception ie) {
                    Log.e("전송스레드","소켓닫는중 에러발생=>"+ie.toString());
                }
            }*/

            Log.e("전송스레드","서버로부터 결과 OK."+receivedPacket);
			 /*
			 try {
  			 	socket.close();
				 } catch (IOException e) {
		            //e.printStackTrace();
		        	Log.e(TAG,"소켓을 닫을 수 없습니다.");
		        }
			 */
            return true;
        }




        /*@Override
        protected void onPostExecute(Boolean isDone) {
            if (isDone) {
                Log.d(TAG,"14.ServerWaitingTask-onPostExecute: Down Transfer Success!");
                Toast.makeText(mContext, "Down Transfer Success!", Toast.LENGTH_SHORT)
                        .show();



            }

        }*/


    }


}
