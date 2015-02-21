package net.dmixa.activedisplay;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class ActiveDisplayNotificationService extends
		NotificationListenerService {

	/** Turn on debugging */
	private static final boolean DEBUG = true;

	/** TAG when writing debug messages */
	private final String TAG = this.getClass().getSimpleName();

    /** Binder given to clients */
    private final IBinder mBinder = new LocalBinder();
    
    /** Current Status bar notification */
    private StatusBarNotification mSBN;

    /** Custom Action for this binder */
    public static final String ACTION_CUSTOM = "net.dmixa.activedisplay.ActiveDisplayNotificationService.ACTION_CUSTOM";

    /** Handle to screen Manager */
    private ScreenManager mScreenManager;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	ActiveDisplayNotificationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ActiveDisplayNotificationService.this;
        }
    }

    
    
    /**
     * Return the notification
     * @return
     */
    public StatusBarNotification getNotification() {
    	return mSBN;
    }
    
    /*
     * (non-Javadoc)
     * @see android.service.notification.NotificationListenerService#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "bind");
        if (intent.getAction().equals(ACTION_CUSTOM)) {
            super.onBind(intent);
            return mBinder;
        } else  {
            return super.onBind(intent);
        }
    }
    
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		
		super.onCreate();
		mScreenManager = new ScreenManager(this);
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.service.notification.NotificationListenerService#onNotificationPosted(android.service.notification.StatusBarNotification)
	 */
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		
		if ( DEBUG ) { 
			Log.d(TAG, "+++++ Notification Posted +++++");
			Log.d(TAG,"ID:" + sbn.getId() + "::" + 
						sbn.getNotification().tickerText + "::" + sbn.getPackageName());
		}
		
		// -- Scrreen is not on so turn it on
		if ( ! mScreenManager.isScreenOn() ) {
			mScreenManager.turnScreenOn();
		}
		mSBN = sbn;
		
		Intent intent = new  Intent(this.getClass().getPackage().getName() + 
				".NOTIFICATION_LISTENER");
		intent.putExtra("notification_event", "posted");
		
		sendBroadcast(intent);

	}

	/*
	 * (non-Javadoc)
	 * @see android.service.notification.NotificationListenerService#onNotificationRemoved(android.service.notification.StatusBarNotification)
	 */
	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		if ( DEBUG ) {
			Log.d(TAG, "+++++ Notification Removed: " + sbn.getNotification().toString());
			Log.d(TAG,"ID :" + sbn.getId() + "::" + sbn.getNotification().tickerText +
					"::" + sbn.getPackageName());
		}
	}

}
