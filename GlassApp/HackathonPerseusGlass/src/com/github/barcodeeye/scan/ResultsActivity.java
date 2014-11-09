package com.github.barcodeeye.scan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.barcodeeye.R;
import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import com.google.android.glass.eye.EyeEventReceiver;
import com.google.android.glass.eye.EyeGesture;
import com.google.android.glass.eye.EyeGestureManager;
import com.google.android.glass.eye.EyeGestureManager.Listener;


public class ResultsActivity extends Activity {

    private static final String TAG = ResultsActivity.class.getSimpleName();
    private List<Card> mCards;
    private Context mContext;
	private CardScrollView mCardScrollView;
	private GestureDetector mGestureDetector;
	private boolean canClose;
	private String text;
	private String footnote;
	private String url;
	private String product;
	private String token;
	private int id;
	private int price;
	private int stock;
	private int remaining;
	private int remainingTime = 10;
	
	private EyeGestureManager mEyeGestureManager;
	private EyeEventReceiver mEyeEventReceiver;
	private EyeGesture target1 = EyeGesture.WINK;
	private EyeGesture target2 = EyeGesture.DOUBLE_BLINK;
	private EyeGestureListener mEyeGestureListener;
    
	//runs without a timer by reposting this handler at the end of the runnable
	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			remainingTime--;
			Log.d(TAG, "Remaining time " + remainingTime);
			updateCards();
			
			if(remainingTime != 0)
				timerHandler.postDelayed(this, 1000);
			else{
        		closeMe();
			}
		}
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = 0;
		canClose = false;
		mContext = this;
		mGestureDetector = createGestureDetector(this);
		url = getIntent().getExtras().getString("url");
		createCards("Veriler Çekiliyor...","Lütfen Bekleyin...");
		
		mEyeGestureManager = EyeGestureManager.from(this);
        for (EyeGesture eg : EyeGesture.values()) {
            boolean supported = mEyeGestureManager.isSupported(eg);
            Log.d("WinkDetector", eg.name() + ":" + supported);
        }
		
		SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.pair_result_key), Context.MODE_PRIVATE);
		token = sharedPref.getString(getString(R.string.pair_result_key), "");
		new HttpAsyncTask().execute(url);
	}
	
	@Override
    protected void onStart() {
		Log.d(TAG, "----------ON START");
        super.onStart();
        setupReceiver();
    }


	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "----------ON PAUSE");
		timerHandler.removeCallbacks(timerRunnable);
		
        removeReceiver();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "----------ON RESUME");
		super.onResume();
	}
	
	private void closeMe(){
		Intent intent = new Intent(mContext, CaptureActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
	}
	
    public void removeReceiver() {
        //mEyeGestureManager.unregister(EyeGesture.BLINK, mEyeGestureListener);
        mEyeGestureManager.unregister(EyeGesture.DOUBLE_BLINK, mEyeGestureListener);
        //mEyeGestureManager.unregister(EyeGesture.WINK, mEyeGestureListener);
        //mEyeGestureManager.unregister(EyeGesture.DOUBLE_WINK, mEyeGestureListener);
        //mEyeGestureManager.unregister(EyeGesture.DOFF, mEyeGestureListener);
        try {
            getApplicationContext().unregisterReceiver(mEyeEventReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupReceiver() {
        mEyeGestureManager = EyeGestureManager.from(getApplicationContext());
        mEyeGestureListener = new EyeGestureListener();

        //mEyeGestureManager.register(EyeGesture.BLINK, mEyeGestureListener);
        mEyeGestureManager.register(EyeGesture.DOUBLE_BLINK, mEyeGestureListener);
        //mEyeGestureManager.register(EyeGesture.WINK, mEyeGestureListener);
        //mEyeGestureManager.register(EyeGesture.DOUBLE_WINK, mEyeGestureListener);
        //mEyeGestureManager.register(EyeGesture.DOFF, mEyeGestureListener);
    }
    
    private class EyeGestureListener implements Listener {
        @Override
        public void onEnableStateChange(EyeGesture eyeGesture, boolean paramBoolean) {
            Log.i(TAG, eyeGesture + " state changed:" + paramBoolean);
        }

        @Override
        public void onDetected(final EyeGesture eyeGesture) {
        	Log.i(TAG, "eyeGesture detected:" + eyeGesture.name());
        	
        	if(eyeGesture == EyeGesture.DOUBLE_BLINK){
                if(id != 0){
                	timerHandler.removeCallbacks(timerRunnable);
                    String addToChartUrl = "http://54.72.214.188:3101/glass/order/"+id+"?token=" + token;
                    id = 0;
        			new HttpAsyncTask().execute(addToChartUrl);
                }
                else if(canClose){
                	closeMe();
                }
        	}
        }
    }

	
	/*
	 * Send generic motion events to the gesture detector
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
	    if (mGestureDetector != null) {
	        return mGestureDetector.onMotionEvent(event);
	    }
	    return false;
	}

	private GestureDetector createGestureDetector(Context context){
	    GestureDetector gestureDetector = new GestureDetector(context);
	    //Create a base listener for generic gestures
	    gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
	        @Override
	        public boolean onGesture(Gesture gesture) {
	            Log.d(TAG,"gesture = " + gesture);
	            switch (gesture) {
	                case TAP:
	                    Log.d(TAG,"TAP called.");
	                    if(id != 0){
	                    	timerHandler.removeCallbacks(timerRunnable);
		                    String addToChartUrl = "http://54.72.214.188:3101/glass/order/"+id+"?token=" + token;
		                    id = 0;
		        			new HttpAsyncTask().execute(addToChartUrl);
	                    }
	                    return true;
	            }

	            return false;
	        }
	    });
	    return gestureDetector;
	}
	
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	Log.d(TAG, "result for http get" + result);
        	try {	
				JSONObject jObject = new JSONObject(result);
				String type = jObject.getString("type");
				if(type.equals("auth")){
					token = jObject.getString("token");
					SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.pair_result_key), Context.MODE_PRIVATE);
					Editor editor = sharedPref.edit();
					editor.putString(getString(R.string.pair_result_key), token);
					editor.commit();
					canClose = true;
					createCards("Cihazlarýnýzý baþarýlý olarak eþleþtirdiniz.","Çýkýþ için çift göz kýrpýn, ya da bekleyin");
				}
				else if(type.equals("product")){
					if(token == ""){
						canClose = true;
						createCards("Ýþlem baþarýsýz!\nÖnce cihazlarýnýzý eþleþtirmeliziniz.","Çýkýþ için çift göz kýrpýn, ya da bekleyin");
					}
					else {
						product = jObject.getString("name");
						id = jObject.getInt("id");
						price = jObject.getInt("price");
						stock = jObject.getInt("stock");
						remaining = jObject.getInt("remaining");
						createCards(product + ", " + price + " TL\n Stokta kalan: " + remaining + "/" + stock + "\nSepete eklemek için çift göz kýrp ya da gözlüðe dokun","Kalan zaman");
					}
				}
				else if(type.equals("order")){
					if(token == ""){
						createCards("Ýþlem baþarýsýz!\nÖnce cihazlarýnýzý eþleþtirmeliziniz.","Çýkýþ için çift göz kýrpýn, ya da bekleyin");
					}
					else {
						boolean success = jObject.getBoolean("success");
						if(success)
							createCards(product + " sepetinize eklendi.","Çýkýþ için çift göz kýrpýn, ya da bekleyin");
						else
							createCards("Ýþlem baþarýsýz!\n"+product+" sepetinize eklenemedi!","Çýkýþ için çift göz kýrpýn, ya da bekleyin");
					}
					canClose = true;
				}
				else if(type.equals("server_error")){
					createCards("Sunucuya eriþilemiyor!\nLütfen Að ayarlarýnýzý kontrol edin.","Çýkýþ için çift göz kýrpýn, ya da bekleyin");
					canClose = true;
				}
				timerHandler.postDelayed(timerRunnable, 1000);
			} catch (JSONException e) {
				Log.d(TAG,"Add to chart is failed.");
				e.printStackTrace();
			}
       }
    }
	
	public static String GET(String url){
		Log.d(TAG, "http get for: " + url);
        InputStream inputStream = null;
        String result = "";
        try {
        	
        	HttpParams httpParameters = new BasicHttpParams();
        	HttpConnectionParams.setConnectionTimeout(httpParameters, 2000);
        	HttpConnectionParams.setSoTimeout(httpParameters, 2000);

        	// create HttpClient
        	HttpClient httpclient = new DefaultHttpClient(httpParameters);
        	
        	// create http get request
        	HttpGet request = new HttpGet(url);

        	// make GET request to the given URL
        	HttpResponse httpResponse = httpclient.execute(request);
 
        	// receive response as inputStream
        	if(httpResponse.getStatusLine().getStatusCode() == 200)
        		inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "{\"type\":\"server_error\"}";
 
        } catch (Exception e) {
            result = "{\"type\":\"server_error\"}";
        }
 
        return result;
    }
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }

	private void createCards(boolean isDeepCreate, String _text, String _footnote) {
		if(isDeepCreate){
			text = _text;
			footnote = _footnote;
		}
		mCards = new ArrayList<Card>();

		Card card;

		card = new Card(this);
		card.setText(_text);
		card.setFootnote(_footnote);
		card.setImageLayout(Card.ImageLayout.FULL);
		card.addImage(R.drawable.question_mark_thumb);
		mCards.add(card);
		
		mCardScrollView = new CardScrollView(this);
		ExampleCardScrollAdapter adapter = new ExampleCardScrollAdapter();
		mCardScrollView.setAdapter(adapter);
		mCardScrollView.activate();
		setContentView(mCardScrollView);
	}
	
	private void createCards(String _text, String _footnote){
		createCards(true, _text, _footnote);
	}
	
	private void updateCards() {
		createCards(false, text, footnote + " " + remainingTime + "...");
	}

    private class ExampleCardScrollAdapter extends CardScrollAdapter {

		@Override
		public int getPosition(Object item) {
			return mCards.indexOf(item);
		}

		@Override
		public int getCount() {
			return mCards.size();
		}

		@Override
		public Object getItem(int position) {
			return mCards.get(position);
		}

		@Override
		public int getViewTypeCount() {
			return Card.getViewTypeCount();
		}

		@Override
		public int getItemViewType(int position){
			return mCards.get(position).getItemViewType();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return  mCards.get(position).getView(convertView, parent);
		}
    }
}
