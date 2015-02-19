package net.dmixa.activedisplay;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


public class MainActivity extends Activity {

	private static final boolean DEBUG = true;
	private static final String TAG = "dmixa.activedisplay.MainActivity";


	/** Button used to generate an event */
	private Button event;

	/** Notification manager for sending events */
	private NotificationManager mNotificationManager;  

	/** auto incrementing notification id */
	int mNotificationID = 0;

	/** Text View that will show the event */
	private TextView mTextView;

	/** Reciever of notifications */
	private NotificationReceiver mReceiver;
	
	/** Layout view of the remote status */
	private FrameLayout mRemoteViewLayout;
	
	/** Handle to Sensor Manager */
	private SensorManager mSensorManager;
	
	/** Handle to Proximity Sensor */
	private Sensor mProximitySensor;
	
	/** Handle to the power manager */
	private PowerManager mPM;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mSensorManager.registerListener(mSensorListener, mProximitySensor, SensorManager.SENSOR_DELAY_UI);

        mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
        
		mTextView = (TextView) findViewById(R.id.textView1);
		mRemoteViewLayout = (FrameLayout) findViewById(R.id.remote_content_view);
		
		if ( DEBUG ) Log.d(TAG, "+++++ remote Veiw: " + mRemoteViewLayout);
		
		event = (Button)findViewById(R.id.button1);
		event.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "+++++ Generating Notification");

				//---create a notification---
				Notification notification = new
						NotificationCompat.Builder(MainActivity.this)   
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Test Notification Title")
				.setContentText("Test Message")
				.setTicker("Ticker Text")
				.setAutoCancel(true)       
				.setNumber(mNotificationID++)
				.build();
				
				//---set the sound and lights---
				notification.defaults |=
						Notification.DEFAULT_SOUND;
				notification.defaults |=
						Notification.DEFAULT_LIGHTS;
				
				//---display the notification---
				mNotificationManager.notify(null, mNotificationID, notification); 

//				float dest = 1;
//			      if (txtView.getAlpha() > 0) {
//			        dest = 0;
//			      }
//			      ObjectAnimator animation3 = ObjectAnimator.ofFloat(txtView,
//			          "alpha", dest);
//			      animation3.setDuration(1000);
//			      animation3.start();
			}
		});

		mReceiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("net.dmixa.activedisplay.NOTIFICATION_LISTENER_EXAMPLE");
		registerReceiver(mReceiver,filter);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mSensorManager.unregisterListener(mSensorListener);
    }

    
    private SensorEventListener mSensorListener = new SensorEventListener() {
		
    	/*
    	 * (non-Javadoc)
    	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
    	 */
		@Override
		public void onSensorChanged(SensorEvent event) {

			if ( DEBUG ) Log.d(TAG, "ActiveDisplay ++++ Sensor: " + event.sensor.toString());
            if (event.sensor.equals(mProximitySensor)) {
            	if ( DEBUG ) {
            		Log.d(TAG, "+++ Found Proximity Sensor +++");
            	}
            	if ( ! mPM.isScreenOn() ) {
            		mPM.wakeUp(SystemClock.uptimeMillis());
            	}
//                float value = event.values[0];
//            	boolean isFar = value >= mProximitySensor.getMaximumRange();
//                if ( DEBUG ) {
//                	Log.d(TAG, "ActiveDisplay ++++ proximity: " + isFar + "::" +
//                            value + "::" + mProximitySensor.getMaximumRange() + 
//                            "::" + mProximityIsFar);
//                }
//                
//                if (isFar != mProximityIsFar) {
//                    mProximityIsFar = isFar;
//                    if (isFar) {
//                        if (!isScreenOn() && mPocketMode != POCKET_MODE_OFF && !isOnCall()) {
//                            if (mNotification == null) {
//                                mNotification = getNextAvailableNotification();
//                            }
//                            if (mNotification != null) {
//                                showNotification(mNotification, true);
//                                turnScreenOn();
//                            } else if (mPocketMode == POCKET_MODE_ALWAYS) {
//                                // showTime();
//                            }
//                        }
//                    }
//                }
            } 
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
		 */
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
        
	private class NotificationReceiver extends BroadcastReceiver{

		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "+++++ Received Notification");
			Bundle bundle = intent.getExtras();
			StatusBarNotification sbn = 
					(StatusBarNotification) bundle.getParcelable("notification_event");
			mTextView.setText(sbn.getPackageName());
			
			if ( DEBUG ) {
				Log.d(TAG, "Value:" + mRemoteViewLayout);
			}
			View sbnView = sbn.getNotification().contentView.apply(context, null);
			sbnView.setBackgroundColor(Color.TRANSPARENT);
			sbnView.setAlpha(0f);
			mRemoteViewLayout.addView(sbnView);
		}
	}
	
	
}
