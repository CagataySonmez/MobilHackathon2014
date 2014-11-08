package edu.hackathon.perseus.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.hackathon.perseus.R;
import edu.hackathon.perseus.R.drawable;
import edu.hackathon.perseus.R.raw;

import edu.hackathon.perseus.core.appLogger;
import edu.hackathon.perseus.core.httpSpeedTest;
import edu.hackathon.perseus.core.httpSpeedTest.REGION;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class MainService extends IntentService {
	public static enum APP_STATE {IDLE, RUNNING, FINISHING, ENDED_SUCCESS, ENDED_FAIL, STOP_DUE_TO_WIFI_ERROR}
	public static APP_STATE state = APP_STATE.IDLE;
	public static String username = "";
	public static String serviceTime = "00:00:00";
	public static int totalDownloadSuccess;
	public static int totalDownloadFailure;
	public static int totalUploadSuccess;
	public static int totalUploadFailure;
	public static int totalPingSuccess;
	public static int totalPingFailure;
	
	private static final String TAG = "MainService";
	private static final Integer INTERVAL = 30;
	private static final Integer TEST_TIME = 24*60*60;
	private Timer elapsedTimeTimer = null;
	private Timer euDownloadTimer = null;
	private Timer usaDownloadTimer = null;
	private Timer asiaDownloadTimer = null;
	private Timer euUploadTimer = null;
	private Timer usaUploadTimer = null;
	private Timer asiaUploadTimer = null;
	private Timer euPingTimer = null;
	private Timer usaPingTimer = null;
	private Timer asiaPingTimer = null;
	
	private String euIP="";
	private String usaIP="";
	private String asiaIP="";
	
	private NotificationManager mNotificationManager = null;
	private static final int NOTIFICATION_ID = 1;
	private Context context;

	public MainService() {
		super("MainService");
		// TODO Auto-generated constructor stub
	}

	public MainService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
	}

	public static APP_STATE getAppState(){
		return state;
	}

	public static Integer getInterval(){
		return INTERVAL;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(state == APP_STATE.RUNNING)
			terminate();
	}
	
	private void showNotification(String ticker, String contentText){
		if(mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);	
		else
			mNotificationManager.cancelAll();
		
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB){
			Notification notification = new Notification(R.drawable.ic_menu_notifications, "Speed Test is Started", System.currentTimeMillis());
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
			notification.setLatestEventInfo(this, "Wlan Delay Analyser", "Speed test is running...", contentIntent);
			mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
        else {
        	NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        	Notification notification = builder
        			.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_menu_notifications)
                    .setTicker(ticker)
                    .setWhen(new Date().getTime())
                    .setAutoCancel(true)
                    .setContentTitle("Wlan Delay Analyser")
                    .setContentText(contentText)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .build();

        	mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
	}
	
	private void hideNotification(){
		if(mNotificationManager != null)
			mNotificationManager.cancelAll();
	}

	public class NetworkStateChangeReceiver extends BroadcastReceiver {

	    @Override
	    public void onReceive(Context context, Intent intent) {

//	    	if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//	    		NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//	    		if(networkInfo.isConnected()) {
//	    			// Wifi is connected
//	    			Log.d(TAG, "Wifi is connected: " + String.valueOf(networkInfo));
//	    		}
//	    	} else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//	    		NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//	    		if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected()) {
//	    			// Wifi is disconnected
//	    			Log.d(TAG, "Wifi is disconnected: " + String.valueOf(networkInfo));
//	    		}
//	    	}
	    	
	    	boolean needToStopTest = false;
	    	
	    	if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) || intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
	    		//checks to see if the device has a Wi-Fi connection.
	    		if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
	    			Log.d(TAG, "Wifi is connected!");
	    			//Toast.makeText(context, "Wlan Delay Analyser: Wifi is connected...", Toast.LENGTH_SHORT).show();
	    		} else if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
	    			Log.d(TAG, "Mobile data is connected!");
	    			//Toast.makeText(context, "Wlan Delay Analyser: Mobile data is connected...", Toast.LENGTH_SHORT).show();
	    		} else {
	    			Log.d(TAG, "Wi-Fi is not available anymore!");
	    			//Toast.makeText(context, "Wlan Delay Analyser: Stopped because Wi-Fi is not available anymore!", Toast.LENGTH_SHORT).show();
	    			needToStopTest = true;
	    		}

	    		if(needToStopTest && state == APP_STATE.STOP_DUE_TO_WIFI_ERROR)
	    		{
	    			//Stop timers
	    			elapsedTimeTimer.cancel();
	    			euDownloadTimer.cancel();
	    			usaDownloadTimer.cancel();
	    			asiaDownloadTimer.cancel();
	    			euUploadTimer.cancel();
	    			usaUploadTimer.cancel();
	    			asiaUploadTimer.cancel();
	    			euPingTimer.cancel();
	    			usaPingTimer.cancel();
	    			asiaPingTimer.cancel();
	    			state = APP_STATE.STOP_DUE_TO_WIFI_ERROR;
	    			showNotification("Speed test is stopped.", "Reason: WiFi is disconnected!");
	    			sendStateUpdateMessageToActivity("ended_fail","Speed test is stopped because WiFi is disconnected!");
	    		}
	    	}
	    }
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Main Service is started. Activating timers...");
		showNotification("Speed Test is Started","Speed test is running...");
		
		//if this is not emulator, register for the connectivity receiver events
		if(intent.getBooleanExtra("isEmulator", false) == false){
			//register receiver programmatically.
			//if you use manifest.xml file you have to define inner class as static.
			IntentFilter filter = new IntentFilter();
	        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
	        filter.addAction("android.net.wifi.STATE_CHANGE");
	        filter.addCategory("boun.edu.android.wlandelayanalyser");
	        this.registerReceiver(new NetworkStateChangeReceiver(), filter, null, null);
		}
		
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String devicename=model;
		if (!model.startsWith(manufacturer))
			devicename = manufacturer + " " + model;

		username = intent.getStringExtra("username");
		state = APP_STATE.RUNNING;
		totalDownloadSuccess=0;
		totalDownloadFailure=0;
		totalUploadSuccess=0;
		totalUploadFailure=0;
		totalPingSuccess=0;
		totalPingFailure=0;
		context = this;
		
		appLogger.getInstance().init(username,devicename);
		
		//get last known location, if available
		LocationManager myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location location = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(location!=null){
			appLogger.getInstance().setLocation(location.getLatitude(), location.getLongitude());
			//Toast.makeText(context, "Latitude"+location.getLatitude(), Toast.LENGTH_SHORT).show();
			//Toast.makeText(context, "Longitude"+location.getLongitude(), Toast.LENGTH_SHORT).show();
		}
		
		Timer t = new Timer();
		t.schedule(new TimerTask(){
			@Override
			public void run() {
				euIP = httpSpeedTest.getRedirectUrl(REGION.EU);
				usaIP = httpSpeedTest.getRedirectUrl(REGION.USA);
				asiaIP = httpSpeedTest.getRedirectUrl(REGION.ASIA);
				startHelper();
			}
		}, 0);

		return START_STICKY;
	}
	
	public void startHelper(){
		elapsedTimeTimer = new Timer();
		elapsedTimeTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				Date currTime = new Date();
				long diffInMillis = currTime.getTime() - appLogger.getInstance().getAppStartTime().getTime();
				long diffSeconds = diffInMillis / 1000 % 60;
				long diffMinutes = diffInMillis / (60 * 1000) % 60;
				long diffHours = diffInMillis / (60 * 60 * 1000);

				String secondsInString = (diffSeconds < 10) ? ("0" + diffSeconds) : ("" + diffSeconds);
				String minutesInString = (diffMinutes < 10) ? ("0" + diffMinutes) : ("" + diffMinutes);
				String hoursInString = (diffHours < 10) ? ("0" + diffHours) : ("" + diffHours);

				serviceTime = hoursInString + ":" + minutesInString + ":" + secondsInString;
				
				sendMessageToActivity("elapsed_time","dummy",serviceTime);
				
				if(diffInMillis / 1000 >= TEST_TIME)
					terminate();
			}
		}, 0, 1*1000);
		
		euDownloadTimer = new Timer();
		euDownloadTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING){
					performDownloadTest(httpSpeedTest.REGION.EU, euIP);
				}
			}
		}, 1*1000, INTERVAL*1000);

		usaDownloadTimer = new Timer();
		usaDownloadTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performDownloadTest(httpSpeedTest.REGION.USA, usaIP);
			}
		}, 4*1000, INTERVAL*1000);

		asiaDownloadTimer = new Timer();
		asiaDownloadTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performDownloadTest(httpSpeedTest.REGION.ASIA,asiaIP);
			}
		}, 7*1000, INTERVAL*1000);
		

		euUploadTimer = new Timer();
		euUploadTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performUploadTest(httpSpeedTest.REGION.EU, euIP);
			}
		}, 11*1000, INTERVAL*1000);

		usaUploadTimer = new Timer();
		usaUploadTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performUploadTest(httpSpeedTest.REGION.USA, usaIP);
			}
		}, 14*1000, INTERVAL*1000);

		asiaUploadTimer = new Timer();
		asiaUploadTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performUploadTest(httpSpeedTest.REGION.ASIA, asiaIP);
			}
		}, 17*1000, INTERVAL*1000);
		

		euPingTimer = new Timer();
		euPingTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performPingTest(httpSpeedTest.REGION.EU, euIP);
			}
		}, 21*1000, INTERVAL*1000);

		usaPingTimer = new Timer();
		usaPingTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performPingTest(httpSpeedTest.REGION.USA, usaIP);
			}
		}, 24*1000, INTERVAL*1000);

		asiaPingTimer = new Timer();
		asiaPingTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if(state == APP_STATE.RUNNING)
					performPingTest(httpSpeedTest.REGION.ASIA, asiaIP);
			}
		}, 27*1000, INTERVAL*1000);
		
	}
	
	/**
	 * This method performs download speed test and saves the logs
	 * 
	 * @return void
	 */
	private void performDownloadTest(httpSpeedTest.REGION region, String IP) {
		Log.d(TAG, "Performing Download Test For " + region.toString());
		
		//download action is the first one, so init the ID of maps used in logger
		int logId = appLogger.getInstance().initLog(region);
		
		httpSpeedTest httpTest = new httpSpeedTest(IP);
		String bw = new DecimalFormat("##.###").format(httpTest.testDownload());
		
		if(bw.equals("0"))
			totalDownloadFailure++;
		else
			totalDownloadSuccess++;

		if(state == APP_STATE.RUNNING) {
			sendMessageToActivity("download",region.toString(),bw);
			appLogger.getInstance().addDownloadLog(logId, region, bw);
		}
	}

	/**
	 * This method performs download speed test and saves the logs
	 * 
	 * @return void
	 */
	private void performUploadTest(httpSpeedTest.REGION region, String IP) {
		Log.d(TAG, "Performing Upload Test For " + region.toString());
		
		//get the log id before waiting the operation due to avoid timeout conflict.
		int logId = appLogger.getInstance().getLastLogId(region);
		
		InputStream is = getResources().openRawResource(R.raw.upload_test);
		httpSpeedTest httpTest = new httpSpeedTest(IP);
		String bw = new DecimalFormat("##.###").format(httpTest.testUpload(is));

		if(bw.equals("0"))
			totalUploadFailure++;
		else
			totalUploadSuccess++;


		if(state == APP_STATE.RUNNING){
			sendMessageToActivity("upload",region.toString(),bw);
			appLogger.getInstance().addUploadLog(logId, region, bw);
		}
	}
	
	/**
	 * This method performs ping delay test and saves the logs
	 * 
	 * @return void
	 */
	private void performPingTest(httpSpeedTest.REGION region, String IP) {
		Log.d(TAG, "Performing Ping Test For " + region.toString());
		
		//get the log id before waiting the operation due to avoid timeout conflict.
		int logId = appLogger.getInstance().getLastLogId(region);
		
		httpSpeedTest httpTest = new httpSpeedTest(IP);
		int delay = httpTest.testPing();
		
		if(delay == 0)
			totalPingFailure++;
		else
			totalPingSuccess++;
		
		if(state == APP_STATE.RUNNING) {
			sendMessageToActivity("ping",region.toString(),Integer.toString(delay));
			appLogger.getInstance().addPingLog(logId, region, delay);
		}
	}
	
	private void sendTextMessageToActivity(String message, String type) {
		sendMessageToActivity("message",type,message);
	}
	
	private void sendStateUpdateMessageToActivity(String state, String message) {
		sendMessageToActivity("state_update",state,message);
	}
	
	private void sendMessageToActivity(String action, String region, String result) {
		Intent intent = new Intent("my-event");
		intent.putExtra("action", action);
		intent.putExtra("region", region);
		intent.putExtra("result", result);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
	/**
	 * This method terminates the application
	 * 
	 * @return void
	 */
	private void terminate() {
		state = APP_STATE.FINISHING;
		
		showNotification("Speed test is ended.", "Finishing, please wait for a while...");
		sendStateUpdateMessageToActivity("finishing","Finishing ongoing operations, then the results will be saved. Please wait for a while...");

		//Stop timers then wait for 15 seconds to finish all tasks
		elapsedTimeTimer.cancel();
		euDownloadTimer.cancel();
		usaDownloadTimer.cancel();
		asiaDownloadTimer.cancel();
		euUploadTimer.cancel();
		usaUploadTimer.cancel();
		asiaUploadTimer.cancel();
		euPingTimer.cancel();
		usaPingTimer.cancel();
		asiaPingTimer.cancel();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			  @Override
			  public void run() {
				  terminateHelper();
			  }
			}, 15*1000);
	}

	/**
	 * This method terminates the application
	 * 
	 * @return void
	 */
	private void terminateHelper() {
		try {
			File outputDir = context.getCacheDir(); // context being the Activity pointer
			//File outputFile = File.createTempFile("prefix", "extension", outputDir);
			
			Log.d(TAG, "Creating zip file under " + outputDir.getAbsolutePath());
			String zipFileName = appLogger.getInstance().saveLogsToFile(outputDir.getAbsolutePath());
			String zipFileAbsolutePath = outputDir.getAbsolutePath() + "/" + zipFileName;
			Log.d(TAG, "Zip file is created as  " + zipFileAbsolutePath);
			
			sendTextMessageToActivity("Results are exracted, now it is being uploaded to server...", "info");
			
			httpSpeedTest resultUploader = new httpSpeedTest(euIP);
			double bw = resultUploader.uploadResults(new FileInputStream(zipFileAbsolutePath), zipFileName);
			if(bw==0.0){
				sendStateUpdateMessageToActivity("ended_fail","Speed test is failed: the results cannot be uploaded to server!");
				state = APP_STATE.ENDED_FAIL;
			}
			else{
				sendStateUpdateMessageToActivity("ended_success","Speed Test is finished successfully and the results are uploaded to server.");
				state = APP_STATE.ENDED_SUCCESS;
			}
			File zipFile = new File(zipFileAbsolutePath);
			zipFile.delete();
		} catch (IOException e) {
			sendStateUpdateMessageToActivity("ended_fail","Speed test is failed: the results cannot be written to file or file cannot be extracted!");
			state = APP_STATE.ENDED_FAIL;
		}
		
		hideNotification();
	}
}
