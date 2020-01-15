package com.hwarim.qholo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.hardware.Camera;
import android.view.View;
import android.view.LayoutInflater;
public class MainActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				startActivity(new Intent(MainActivity.this, CaptureActivity.class));
				finish();
			}
		};
		handler.sendEmptyMessageDelayed(0, 300);


	}




}

