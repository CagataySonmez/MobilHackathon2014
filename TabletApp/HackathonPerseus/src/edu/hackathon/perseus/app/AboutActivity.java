package edu.hackathon.perseus.app;

import edu.hackathon.perseus.R;
import edu.hackathon.perseus.R.id;
import edu.hackathon.perseus.R.layout;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.widget.TextView;

public class AboutActivity extends Activity {
	final String aboutTitle = "About this Application";
	final String aboutText = "This application is used to analyse characteristics of WiFi 802.11 " +
			"wide area network (WAN) delay from your region to Europe, Asia and America.<br><br>" +
			"Please note that, this application<br>" +
			"* periodically uploads and downloads dummy files.<br>" +
			"* uses model name (e.g. HTC One) and coarse location (via WiFi) for non-personalized data analyses.<br>" +
			"* does not use mobile data!<br>" +
			"* does not collect any data from the device!<br>" +
			"* does not use any sensor and its data<br><br>" +
			"You can report bugs or ask something about this application by sending " +
			"an e-mail to cagatay.sonmez@boun.edu.tr";
	
	final String helpTitle = "How to Use";
	final String helpText = "Important notes about this application are given below:<br><br>" +
			"* To start speed test, you need to type a name for your place. Please note that the name of the place will help us on further data processing.<br>" +
			"* There is only one button that you can use to start/stop speed test.<br>" +
			"* Speed test procedure runs on background, so you can close the application after you start the speed test.<br>" +
			"* Please do not forget to stop speed test before closing your wifi connection, because when wifi connection is gone, speed test is stopped.<br>" +
			"* Please be sure that your test results are successfuly sent to server before closing your WiFi connection.<br>" +
			"* The result of the operations are provided at the bottom of the screen, please follow those messages.<br>";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		
		String pageType = getIntent().getExtras().getString("pageType");
		
		if(pageType.equals("about")){
			((TextView) findViewById(R.id.AboutPageTitleText)).setText(aboutTitle);
			((TextView) findViewById(R.id.AboutPageMessageText)).setText(Html.fromHtml(aboutText));
		}
		else if(pageType.equals("help")){
			((TextView) findViewById(R.id.AboutPageTitleText)).setText(helpTitle);
			((TextView) findViewById(R.id.AboutPageMessageText)).setText(Html.fromHtml(helpText));			
		}
	}
}
