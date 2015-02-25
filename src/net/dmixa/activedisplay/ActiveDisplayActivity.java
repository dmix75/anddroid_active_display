package net.dmixa.activedisplay;

import net.dmixa.activedisplay.ActiveDisplayNotificationService.LocalBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * This class is the main activity for ActiveDisplay
 *
 */
public class ActiveDisplayActivity extends Activity {

	/** DEBUG flag */
	private static final boolean DEBUG = true;

	/** Name on the debug tags */
	private final String tag = this.getClass().getSimpleName();

	/** Handle to broadcast receiver */
	private NotificationReceiver mReceiver;

	/** handle to notification service */
	private ActiveDisplayNotificationService mService;

	/** define if service has been bound or not */
	private boolean mBound;

	/** Handle to framelayout to hold Notification */
	private FrameLayout mRemoteViewLayout;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mBound = false;

		setContentView(R.layout.active_display_activity);

		registerListener();

		startActivity(new Intent(this, ActiveDisplaySettings.class));

		mRemoteViewLayout = (FrameLayout) findViewById(R.id.remote_content_view);

	}

	/**
	 * Register the Notification BroadCast Receiver
	 */
	private void registerListener() {

		mReceiver = new NotificationReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(this.getClass().getPackage().getName()
				+ ".NOTIFICATION_LISTENER");

		registerReceiver(mReceiver, filter);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {

		super.onResume();

		startService(new Intent(this, ActiveDisplayService.class));
		startService(new Intent(this, ActiveDisplayNotificationService.class));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {

		super.onStart();

		if (DEBUG) {
			Log.d(tag, "Activity Binding to service");
		}

		// Bind to LocalService
		Intent intent = new Intent(this, ActiveDisplayNotificationService.class);
		intent.setAction(ActiveDisplayNotificationService.ACTION_CUSTOM);
		bindService(intent, mConnection, BIND_AUTO_CREATE);
	}

	/*
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();

		// Unbind from the service
		if (mBound) {
			if (DEBUG) {
				Log.d(tag, "Activity Unbinding from service");
			}
			unbindService(mConnection);
			mBound = false;
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.ServiceConnection#onServiceConnected(android.content
		 * .ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;

			if (DEBUG) {
				Log.d(tag, "Notification Service Connected");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.ServiceConnection#onServiceDisconnected(android.content
		 * .ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			if (DEBUG) {
				Log.d(tag, "Notification Service Disconnected");
			}
			mBound = false;
		}
	};

	/**
	 * This class will receive notifications from the NotificationService
	 *
	 */
	private class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (DEBUG) {
				Log.d(tag, "+++++ Received Notification +++++");
			}

			// StatusBarNotification[] sbnList =
			// mService.getActiveNotifications();
			// if ( null != sbnList ) {
			// for ( StatusBarNotification sbn : sbnList ) {
			// Log.d(tag,"ID:" + sbn.getId() + "::" +
			// sbn.getNotification().tickerText + "::" + sbn.getPackageName());
			// }
			// } else {
			// if (DEBUG) Log.d(tag, "++++ Empty Notifiations ++++");
			// }

			StatusBarNotification sbn = mService.getCurrentNotification();

			if (null != sbn) {

				if (DEBUG) {
					Log.d(tag,
							"ID:" + sbn.getId() + "::"
									+ sbn.getNotification().tickerText + "::"
									+ sbn.getPackageName());
				}

				View sbnView = sbn.getNotification().contentView.apply(context,
						null);
				sbnView.setBackgroundColor(Color.TRANSPARENT);
				sbnView.setAlpha(0f);
				mRemoteViewLayout.addView(sbnView);
			}

			// Bundle bundle = intent.getExtras();
			// StatusBarNotification sbn =
			// (StatusBarNotification)
			// bundle.getParcelable("notification_event");
			// mTextView.setText(sbn.getPackageName());
			//
			// if (DEBUG) {
			// Log.d(tag, "Value:" + mRemoteViewLayout);
			// }
			// View sbnView = sbn.getNotification().contentView.apply(context,
			// null);
			// sbnView.setBackgroundColor(Color.TRANSPARENT);
			// sbnView.setAlpha(0f);
			// mRemoteViewLayout.addView(sbnView);
		}
	}

}
