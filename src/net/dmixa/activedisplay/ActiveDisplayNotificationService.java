package net.dmixa.activedisplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * This class is used to Listen for Notifications to the system
 */
public class ActiveDisplayNotificationService extends
        NotificationListenerService {

    /** Notification preference. */
    private static final String PREF_NOTIFICATION_TAG = "pref_active_display_notifications";

    /** tag when writing ActiveDisplayUtil.DEBUG messages. */
    private final String tag = this.getClass().getSimpleName();

    /** Binder given to clients. */
    private final IBinder mBinder = new LocalBinder();

    /** Custom Action for this binder. */
    public static final String ACTION_CUSTOM = "net.dmixa.activedisplay."
            + "ActiveDisplayNotificationService.ACTION_CUSTOM";

    /** Handle to screen Manager. */
    private ScreenManager mScreenManager;

    /** Enabled flag. */
    private boolean mEnabled;

    /** Handle to preferences. */
    private SharedPreferences mPrefs;

    /** The current notification. */
    private StatusBarNotification mCurrentNotification;

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        /**
         * 
         * @return this NotificationService
         */
        ActiveDisplayNotificationService getService() {
            // Return this instance of LocalService so clients can call public
            // methods
            return ActiveDisplayNotificationService.this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind( Intent intent ) {
        Log.d( tag, "bind" );
        if ( intent.getAction().equals( ACTION_CUSTOM ) ) {
            super.onBind( intent );
            return mBinder;
        } else {
            return super.onBind( intent );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {

        super.onCreate();
        mScreenManager = new ScreenManager( this );

        mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
        mEnabled = mPrefs.getBoolean( PREF_NOTIFICATION_TAG, true );

        mPrefs.registerOnSharedPreferenceChangeListener( 
                new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key ) {
                if ( PREF_NOTIFICATION_TAG.equals( key ) ) {
                    if ( ActiveDisplayUtil.DEBUG ) {
                        Log.d( tag, "Notification Preference changed" );
                    }
                    mEnabled = sharedPreferences.getBoolean( key, true );
                }
            }
        } );
    }

    /**
     * 
     * @return current posted notification
     */
    public StatusBarNotification getCurrentNotification() {
        return mCurrentNotification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.service.notification.NotificationListenerService#onNotificationPosted
     * (android.service.notification.StatusBarNotification)
     */
    @Override
    public void onNotificationPosted( StatusBarNotification sbn ) {

        // -- not enabled get out
        if ( !mEnabled ) {
            return;
        }

        if ( ActiveDisplayUtil.DEBUG ) {
            Log.d( tag, "+++++ Notification Posted +++++" );
        }

        // -- Scrreen is not on so turn it on
        if ( !mScreenManager.isScreenOn() ) {
            mScreenManager.turnScreenOn();
        }

        mCurrentNotification = sbn;

        Intent intent = new Intent( this.getClass().getPackage().getName()
                + ".NOTIFICATION_LISTENER" );
        intent.putExtra( "notification_event", "posted" );

        sendBroadcast( intent );

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.service.notification.NotificationListenerService#
     * onNotificationRemoved(android.service.notification.StatusBarNotification)
     */
    @Override
    public void onNotificationRemoved( StatusBarNotification sbn ) {
        if ( ActiveDisplayUtil.DEBUG ) {
            Log.d( tag, "+++++ Notification Removed: "
                    + sbn.getNotification().toString() );
        }
    }

}
