package net.dmixa.activedisplay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

public class ActiveDisplayActivity extends Activity {

	/** DEBUG flag */
	private static final boolean DEBUG = true;
	
	/** Name on the debug tags */
	private final String TAG = this.getClass().getSimpleName();

	/** Handle to broadcast receiver */
	private NotificationReceiver mReceiver;

	/** handle to notification service */
    private ActiveDisplayNotificationService mService;
    
    /** define if service has been bound or not */
    private boolean mBound;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		
		mBound = false;
		
		setContentView(R.layout.active_display_activity);
		
		registerListener();
		
		startActivity(new Intent(this, ActiveDisplaySettings.class));
	}

	/**
	 * Register the Notification BroadCast Receiver
	 */
	private void registerListener() {

		mReceiver = new NotificationReceiver();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(this.getClass().getPackage().getName() + 
				".NOTIFICATION_LISTENER");
		
		registerReceiver(mReceiver,filter);
		
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		
		super.onResume();
		
		startService(new Intent(this, ActiveDisplayService.class));
		startService(new Intent(this, ActiveDisplayNotificationService.class));
		
	}

	private class NotificationReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if ( DEBUG ) Log.d(TAG, "+++++ Received Notification +++++");
			
			
//			Bundle bundle = intent.getExtras();
//			StatusBarNotification sbn = 
//					(StatusBarNotification) bundle.getParcelable("notification_event");
//			mTextView.setText(sbn.getPackageName());
//			
//			if ( DEBUG ) {
//				Log.d(TAG, "Value:" + mRemoteViewLayout);
//			}
//			View sbnView = sbn.getNotification().contentView.apply(context, null);
//			sbnView.setBackgroundColor(Color.TRANSPARENT);
//			sbnView.setAlpha(0f);
//			mRemoteViewLayout.addView(sbnView);
		}
	}
	
}
