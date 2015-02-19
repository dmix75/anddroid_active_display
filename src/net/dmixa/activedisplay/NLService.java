package net.dmixa.activedisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NLService extends NotificationListenerService {

	/** Turn on debugging */
	private static final boolean DEBUG = true;

	/** TAG when writing debug messages */
	private final String TAG = this.getClass().getSimpleName();

	/** Notification Listener used to receive messages */
	private NLServiceReceiver mNLServiceReciver;

	@Override
	public void onCreate() {
		
		super.onCreate();
		
		mNLServiceReciver = new NLServiceReceiver();
		
		if ( DEBUG ) Log.d(TAG, this.getClass().getPackage().getName());
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(this.getClass().getPackage().getName() + 
				".NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
		registerReceiver(mNLServiceReciver,filter);
	
	}

	/*
	 * (non-Javadoc)
	 * @see android.service.notification.NotificationListenerService#onNotificationPosted(android.service.notification.StatusBarNotification)
	 */
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		
		if ( DEBUG ) { 
			Log.d(TAG, "+++++ Notification Posted: " + sbn.getNotification().toString());
			Log.d(TAG,"ID:" + sbn.getId() + "::" + 
						sbn.getNotification().tickerText + "::" + sbn.getPackageName());
		}
		
		Intent intent = new  Intent(this.getClass().getPackage().getName() + 
				".NOTIFICATION_LISTENER_EXAMPLE");
		intent.putExtra("notification_event", sbn);
		
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

		Intent intent = new  Intent(this.getClass().getPackage().getName() +
				".NOTIFICATION_LISTENER_EXAMPLE");
		intent.putExtra("notification_event",sbn);

		sendBroadcast(intent);
		
	}


	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mNLServiceReciver);
	}

	private class NLServiceReceiver extends BroadcastReceiver{

		/*
		 * (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			if ( intent.getStringExtra("command").equals("clearall") ) {
			
				NLService.this.cancelAllNotifications();
				
			} else if ( intent.getStringExtra("command").equals("list") ) {
				
				Intent i1 = new  Intent("net.dmixa.activedisplay.NOTIFICATION_LISTENER_EXAMPLE");
				i1.putExtra("notification_event","=====================");
				sendBroadcast(i1);
				int i=1;
				for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
					Intent i2 = new  Intent("net.dmixa.activedisplay.NOTIFICATION_LISTENER_EXAMPLE");
					i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
					sendBroadcast(i2);
					i++;
				}
				Intent i3 = new  Intent("net.dmixa.activedisplay.NOTIFICATION_LISTENER_EXAMPLE");
				i3.putExtra("notification_event","===== Notification List ====");
				sendBroadcast(i3);

			}

		}
	}
}
