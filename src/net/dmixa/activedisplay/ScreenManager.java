package net.dmixa.activedisplay;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class ScreenManager {

	/** Turn on debugging */
	private static final boolean DEBUG = true;

	/** TAG when writing debug messages */
	private final String TAG = this.getClass().getSimpleName();

	/** Handle to the power Manager */
	private PowerManager mPM;
	
	/** Handle to wake Lock */
	private WakeLock mWakeLock;

	public ScreenManager(Service service) {
		
		mPM = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
	}
	
	/**
	 * Un-register the wake lock
	 */
	public void unregisterWakeLock() {
		if ( null != mWakeLock ) {
			mWakeLock.release();
			mWakeLock = null;
			if ( DEBUG ) Log.d(TAG,"Releasing Wake Lock");

		}
	}

	/**
	 * create and aquire the wakeLock
	 */
	public void registerWakeLock() {

		if ( null == mWakeLock ) {
			mWakeLock = mPM.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
		}

	}
	
	/** 
	 * Turn on the screen
	 */
	public void turnScreenOn() {

		registerWakeLock();
		if ( null != mWakeLock ) {
			if ( DEBUG ) Log.d(TAG,"Aquiring Wake Lock");
			mWakeLock.acquire();
			unregisterWakeLock();
		} else {
			if ( DEBUG ) Log.d(TAG,"Failed to register wakeLock");
		}
	}
	
	public boolean isScreenOn() {
		return mPM.isScreenOn();
	}
}
