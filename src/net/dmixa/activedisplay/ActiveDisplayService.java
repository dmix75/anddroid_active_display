package net.dmixa.activedisplay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

public class ActiveDisplayService extends Service implements
SensorEventListener {

	/** Flag for debugging */
	private static final boolean DEBUG = true;

	/** Tag for DEBUG output */
	private final String TAG = this.getClass().getSimpleName();

	/** Delay before re-registering our sensor listenters */
	public static final int SCREEN_OFF_RECEIVER_DELAY = 500;

	/** handle to sensor Manager */
	private SensorManager mSensorManager;

	/** Handle to wake Lock */
	private WakeLock mWakeLock;

	/** Handle to the power Manager */
	private PowerManager mPM;

	/** Handle to proximity sensor */
	private Sensor mProximitySensor;

	/** Mark when the proximity sensor says its near */
	private boolean mProximityNear;

	/** Handle to preferences */
	private SharedPreferences mPrefs;

	/** enabled or disabled service */
	private boolean mEnabled;

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mProximityNear = false;

		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mEnabled = mPrefs.getBoolean("pref_active_display", true);

		mPrefs.registerOnSharedPreferenceChangeListener(
				new SharedPreferences.OnSharedPreferenceChangeListener() {

					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
							String key) {
						boolean enabled = sharedPreferences.getBoolean(key, true);
						if ( DEBUG ) Log.d(TAG, "Service status: " + mEnabled);
						if ( enabled != mEnabled ) {
							mEnabled = enabled;
							if ( ! mEnabled ) {

								unregisterListener();
								unregisterWakeLock();

							} else {

								registerListener();
							}
						}
					}
				}); 
	}


	/**
	 * Register this as a sensor event listener.
	 */
	private void registerListener() {
		mSensorManager.registerListener(this,
				mProximitySensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * Un-register this as a sensor event listener.
	 */
	private void unregisterListener() {
		mSensorManager.unregisterListener(this);
	}

	/**
	 * Un-register the wake lock
	 */
	private void unregisterWakeLock() {
		if ( null != mWakeLock ) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	/**
	 * create and aquire the wakeLock
	 */
	private void registerWakeLock() {

		if ( null == mWakeLock ) {
			mWakeLock = mPM.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
		}

	}

	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if ( DEBUG ) Log.d(TAG, "onAccuracyChanged()");
	}

	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {

		if ( DEBUG ) Log.d(TAG, "onSensorChanged - screen on? " + mPM.isScreenOn());
		float value = event.values[0];
		boolean isFar = value >= mProximitySensor.getMaximumRange();
		Log.d(TAG, "ActiveDisplay ++++ proximity: " + isFar + "::" +
				value + "::" + mProximitySensor.getMaximumRange() );

		// -- get out if the screen is already turned on
		if ( mPM.isScreenOn() ) return;

		if ( ! isFar ) {

			// -- Mark that the proximity is near and get out 
			// -- We need to have been near on previous pass and far 
			// -- on this pass
			mProximityNear = true;

		} else {

			// -- Check the previous sensor reading and make sure it was near
			if ( mProximityNear ) {
				turnScreenOn();
				mProximityNear = false;
			}
		}
	}

	/** 
	 * Turn on the screen
	 */
	private void turnScreenOn() {

		registerWakeLock();
		if ( null != mWakeLock ) {
			if ( DEBUG ) Log.d(TAG,"Aquiring Wake Lock");
			mWakeLock.acquire();
			unregisterWakeLock();
		} else {
			if ( DEBUG ) Log.d(TAG,"Failed to register wakeLock");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Receiver 
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		/*
		 * (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			if ( DEBUG ) Log.d(TAG, "onReceive("+intent+")");

			if ( intent.getAction().equals(Intent.ACTION_SCREEN_ON) ) {
				// -- turn off all the sensors
				unregisterListener();
				unregisterWakeLock();
			} else if ( intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ) {

				if ( mEnabled ) {
					// -- Fire up the sensors
					Runnable runnable = new Runnable() {
						public void run() {
							if ( mEnabled ) {
								if ( DEBUG ) Log.d(TAG, "Runnable executing.");
								unregisterListener();
								registerListener();
								unregisterWakeLock();
							}
						}
					};

					new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
				}

			} else {
				// nothing to be done
			}

		}
	};

}
