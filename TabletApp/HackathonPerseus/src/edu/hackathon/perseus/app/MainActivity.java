package edu.hackathon.perseus.app;

import java.util.Locale;

import edu.hackathon.perseus.app.MainService.APP_STATE;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.hackathon.perseus.R;
import edu.hackathon.perseus.R.id;
import edu.hackathon.perseus.R.layout;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		this.initUI();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.initUI();
		
		// Register mMessageReceiver to receive messages.
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter("my-event"));
	}

	@Override
	protected void onPause() {
		// Unregister since the activity is not visible
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	   if ( keyCode == KeyEvent.KEYCODE_MENU ) {
		   menuButtonClick(this.findViewById(android.R.id.content));
	       return true;
	   }

	   // let the system handle all other key events
	   return super.onKeyDown(keyCode, event);
	}

	final int CONTEXT_MENU_ABOUT = 1;
	final int CONTEXT_MENU_HELP = 2;
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
		//Context menu
		menu.setHeaderTitle("Options");
		menu.add(Menu.NONE, CONTEXT_MENU_ABOUT, Menu.NONE, "About");
		menu.add(Menu.NONE, CONTEXT_MENU_HELP, Menu.NONE, "Help");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
		case CONTEXT_MENU_ABOUT:
		{
			Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
			aboutIntent.putExtra("pageType", "about");
			MainActivity.this.startActivity(aboutIntent);
		}
		break;
		case CONTEXT_MENU_HELP:
		{
			Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
			aboutIntent.putExtra("pageType", "help");
			MainActivity.this.startActivity(aboutIntent);
		}
		break;
		}

		return super.onContextItemSelected(item);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
	public void menuButtonClick(View v)
	{
		//To register the button with context menu.
        registerForContextMenu(v);
        openContextMenu(v);
	}
	
	private boolean isRunningOnEmulator() {
		boolean isEmulator = false;
		if (android.os.Build.BRAND.toLowerCase(Locale.US).equals("generic"))
			isEmulator=true;
		return isEmulator;
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isWiFiConnected(){
		boolean isWiFi = false;
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
		isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
//		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		if(mWifi != null && mWifi.isConnected())
//			isWiFi true;
		
		return isWiFi;
	}
	
	private void initUI(){
		String buttonText="Start Test";
		String message="Press 'Start Test' to start Wlan Delay Analyser";
		int messageBgColor=Color.GRAY;
		boolean isServiceEnded=true;
		boolean isServiceEnding=false;
		
		if(MainService.state == APP_STATE.RUNNING){
			buttonText="Stop Test";
			message="Wlan Delay Analyser is running.<br>Press 'Stop Test' to send results to server.";
			messageBgColor=Color.BLUE;
			isServiceEnded=false;
		}
		else if(MainService.state == APP_STATE.FINISHING){
			buttonText="Finishing";
			message="Finishing operations and sending results to servers. Please wait for a while...";
			messageBgColor=Color.YELLOW;
			isServiceEnded=false;
			isServiceEnding=true;
			((Button) findViewById(R.id.btnStartStop)).setClickable(false);
		}
		else if(MainService.state == APP_STATE.ENDED_SUCCESS){
			message="Speed Test is finished successfuly and result file are sent to server.";
			messageBgColor=Color.GREEN;
		}
		else if(MainService.state == APP_STATE.ENDED_FAIL){
			message="Speed Test is finished but the results cannot be sent to server!";
			messageBgColor=Color.RED;
		}
		else if(MainService.state == APP_STATE.STOP_DUE_TO_WIFI_ERROR){
			message="Speed test was stopped because WiFi was disconnected!";
			messageBgColor=Color.RED;
		}
		
		//reset all UI items
		this.resetUI(true);
		
		//update total number of successful and failed operations
		if(MainService.state != APP_STATE.IDLE){
			((TextView) findViewById(R.id.lblElapsedTime)).setText(MainService.serviceTime);
			((EditText) findViewById(R.id.etPlaceName)).setText(MainService.username);
			((TextView) findViewById(R.id.lblNumOfFailDownload)).setText(Integer.toString(MainService.totalDownloadFailure));
			((TextView) findViewById(R.id.lblNumOfSuccessDownload)).setText(Integer.toString(MainService.totalDownloadSuccess));
			((TextView) findViewById(R.id.lblNumOfFailUpload)).setText(Integer.toString(MainService.totalUploadFailure));
			((TextView) findViewById(R.id.lblNumOfSuccessUpload)).setText(Integer.toString(MainService.totalUploadSuccess));
			((TextView) findViewById(R.id.lblNumOfFailPing)).setText(Integer.toString(MainService.totalPingFailure));
			((TextView) findViewById(R.id.lblNumOfSuccessPing)).setText(Integer.toString(MainService.totalPingSuccess));
		}
		
		((TextView) findViewById(R.id.lblMessageBox)).setText(Html.fromHtml(message));
		((LinearLayout) findViewById(R.id.llMessageArea)).setBackgroundColor(messageBgColor);
		((Button) findViewById(R.id.btnStartStop)).setText(buttonText);
		((EditText) findViewById(R.id.etPlaceName)).setEnabled(isServiceEnded);
		((Button) findViewById(R.id.btnStartStop)).setClickable(!isServiceEnding);
		if(isServiceEnded)
			stopService(new Intent(this, MainService.class));
	}
	
	private void updateUI(String message, String buttonText, int messageBgColor, boolean isEditable){
		((Button) findViewById(R.id.btnStartStop)).setText(buttonText);
		((LinearLayout) findViewById(R.id.llMessageArea)).setBackgroundColor(messageBgColor);
		((Button) findViewById(R.id.btnStartStop)).setClickable(isEditable);
		((EditText) findViewById(R.id.etPlaceName)).setEnabled(isEditable);
		((TextView) findViewById(R.id.lblMessageBox)).setText(message);
	}
	
	private void resetUI(boolean isFirstTime){
		if(!isFirstTime){
			((TextView) findViewById(R.id.lblMessageBox)).setText(Html.fromHtml("Wlan Delay Analyser is running.<br>Press 'Stop Test' to send results to server."));
			((LinearLayout) findViewById(R.id.llMessageArea)).setBackgroundColor(Color.BLUE);
			((Button) findViewById(R.id.btnStartStop)).setText("Stop Test");
			((EditText) findViewById(R.id.etPlaceName)).setEnabled(false);
		}

		((TextView) findViewById(R.id.lblNumOfFailDownload)).setText("0");
		((TextView) findViewById(R.id.lblNumOfSuccessDownload)).setText("0");
		((TextView) findViewById(R.id.lblNumOfFailUpload)).setText("0");
		((TextView) findViewById(R.id.lblNumOfSuccessUpload)).setText("0");
		((TextView) findViewById(R.id.lblNumOfFailPing)).setText("0");
		((TextView) findViewById(R.id.lblNumOfSuccessPing)).setText("0");
		((TextView) findViewById(R.id.lblLastDownloadEu)).setText("?");
		((TextView) findViewById(R.id.lblLastDownloadUsa)).setText("?");
		((TextView) findViewById(R.id.lblLastDownloadAsia)).setText("?");
		((TextView) findViewById(R.id.lblLastUploadEu)).setText("?");
		((TextView) findViewById(R.id.lblLastUploadUsa)).setText("?");
		((TextView) findViewById(R.id.lblLastUploadAsia)).setText("?");
		((TextView) findViewById(R.id.lblLastPingEu)).setText("?");
		((TextView) findViewById(R.id.lblLastPingUsa)).setText("?");
		((TextView) findViewById(R.id.lblLastPingAsia)).setText("?");
	}

	public void buttonClick(View v)
	{
		if(!isMyServiceRunning(MainService.class)){
			String alertMsg="";
			String place = ((EditText) findViewById(R.id.etPlaceName)).getText().toString();

			if (!isRunningOnEmulator() && !isWiFiConnected()) {
				alertMsg="You cannot start test without WiFi connection!";
			}
			else if(place.length() == 0) {
				alertMsg="You cannot start test without a place name!";
			}
			
			if(alertMsg.length() != 0) {
				AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
				dlgAlert.setMessage(alertMsg);
				dlgAlert.setTitle("Wlan Delay Analyser");
				dlgAlert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//dismiss the dialog  
					}
				});
				dlgAlert.setCancelable(true);
				dlgAlert.create().show();
			}
			else {
				Log.d(TAG, "Starting main service...");
				Intent serviceIntent = new Intent(this,MainService.class); 
				serviceIntent.putExtra("username", place);
				serviceIntent.putExtra("isEmulator", isRunningOnEmulator());
				startService(serviceIntent);
				resetUI(false);
			}
		}
		else{
			Log.d(TAG, "Stopping main service...");
			stopService(new Intent(this, MainService.class));
		}
	}

	// handler for received Intents for the "my-event" event 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			MainActivity.this.runOnUiThread(new MyRunnable(intent));
			//MyAsyncTask myAsyncTask = new MyAsyncTask();
			//myAsyncTask.execute(intent);
		}
	};

	//	private class MyAsyncTask extends AsyncTask<Intent, Intent, Void> {
	//		@Override
	//		protected Void doInBackground(Intent... params) {
	//			publishProgress(params[0]);
	//			return null;
	//		}
	//
	//		@Override
	//		protected void onPostExecute(Void result) {
	//			super.onPostExecute(result);
	//		}
	//		
	//	    @Override
	//	    protected void onProgressUpdate(Intent... values) {
	//	    	statusUpdate(values[0]);
	//	    }
	//	}

	private class MyRunnable implements Runnable {
		private Intent intent;
		public MyRunnable(Intent _intent) {
			this.intent = _intent;
		}

		public void run() {
			statusUpdate(intent);
		}
	}

	// handler for received Intents for the "my-event" event 
	private void statusUpdate(Intent intent) {
		// Extract data included in the Intent
		String action = intent.getStringExtra("action");
		String region = intent.getStringExtra("region");
		String result = intent.getStringExtra("result");

		if(action.equals("elapsed_time")) {
			((TextView) findViewById(R.id.lblElapsedTime)).setText(result);
		}
		else if(action.equals("message")) {
			((TextView) findViewById(R.id.lblMessageBox)).setText(result);
		}
		else if(action.equals("state_update")) {
			if(region.equals("finishing"))
				this.updateUI(result,"Finishing",Color.YELLOW,false);
			else if(region.equals("ended_fail"))
				this.updateUI(result,"Start Test",Color.RED,true);
			else if(region.equals("ended_success"))
				this.updateUI(result,"Start Test",Color.GREEN,true);
			
			if(region.equals("ended_fail") || region.equals("ended_success"))
				MainService.state = APP_STATE.IDLE;
		}
		else if(action.equals("download")) {
			((TextView) findViewById(R.id.lblNumOfFailDownload)).setText(Integer.toString(MainService.totalDownloadFailure));
			((TextView) findViewById(R.id.lblNumOfSuccessDownload)).setText(Integer.toString(MainService.totalDownloadSuccess));
			
			if(region.equals("EU"))
				((TextView) findViewById(R.id.lblLastDownloadEu)).setText(result + " Mbps");
			else if(region.equals("USA"))
				((TextView) findViewById(R.id.lblLastDownloadUsa)).setText(result + " Mbps");
			else
				((TextView) findViewById(R.id.lblLastDownloadAsia)).setText(result + " Mbps");
		}
		else if(action.equals("upload")) {
			((TextView) findViewById(R.id.lblNumOfFailUpload)).setText(Integer.toString(MainService.totalUploadFailure));
			((TextView) findViewById(R.id.lblNumOfSuccessUpload)).setText(Integer.toString(MainService.totalUploadSuccess));

			if(region.equals("EU"))
				((TextView) findViewById(R.id.lblLastUploadEu)).setText(result + " Mbps");
			else if(region.equals("USA"))
				((TextView) findViewById(R.id.lblLastUploadUsa)).setText(result + " Mbps");
			else
				((TextView) findViewById(R.id.lblLastUploadAsia)).setText(result + " Mbps");
		}
		else{
			((TextView) findViewById(R.id.lblNumOfFailPing)).setText(Integer.toString(MainService.totalPingFailure));
			((TextView) findViewById(R.id.lblNumOfSuccessPing)).setText(Integer.toString(MainService.totalPingSuccess));
			
			if(region.equals("EU"))
				((TextView) findViewById(R.id.lblLastPingEu)).setText(result + " ms");
			else if(region.equals("USA"))
				((TextView) findViewById(R.id.lblLastPingUsa)).setText(result + " ms");
			else
				((TextView) findViewById(R.id.lblLastPingAsia)).setText(result + " ms");
		}
	}
}
