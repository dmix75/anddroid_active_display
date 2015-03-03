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
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This class is the main Service listening for the proximity sensor and waking
 * up the device when it was set off.
 * 
 */
public class ActiveDisplayService extends Service implements
        SensorEventListener {

    /** tag for ActiveDisplayUtil.DEBUG output */
    private final String tag = this.getClass().getSimpleName();

    /** Delay before re-registering our sensor listenters */
    public static final int SCREEN_OFF_RECEIVER_DELAY = 500;

    /** handle to sensor Manager */
    private SensorManager mSensorManager;

    /** Handle to proximity sensor */
    private Sensor mProximitySensor;

    /** Mark when the proximity sensor says its near */
    private boolean mProximityNear;

    /** Handle to preferences */
    private SharedPreferences mPrefs;

    /** enabled or disabled service */
    private boolean mEnabled;

    /** Handle to manager to turn on and wake up device */
    private ScreenManager mScreenManager;

    /** Notification preference */
    private static final String PREF_ACTIVE_DISPLAY_TAG = "pref_active_display";

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService( SENSOR_SERVICE );

        mScreenManager = new ScreenManager( this );
        mProximitySensor = mSensorManager
                .getDefaultSensor( Sensor.TYPE_PROXIMITY );
        mProximityNear = false;

        registerReceiver( mReceiver,
                new IntentFilter( Intent.ACTION_SCREEN_OFF ) );
        registerReceiver( mReceiver, new IntentFilter( Intent.ACTION_SCREEN_ON ) );

        mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
        mEnabled = mPrefs.getBoolean( "pref_active_display", true );

        mPrefs.registerOnSharedPreferenceChangeListener( 
                new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key ) {
                if ( PREF_ACTIVE_DISPLAY_TAG.equals( key ) ) {

                    boolean enabled = sharedPreferences.getBoolean( key, true );

                    if ( ActiveDisplayUtil.DEBUG ) {
                        Log.d( tag, "Service status: " + mEnabled );
                    }

                    if ( enabled != mEnabled ) {
                        mEnabled = enabled;
                        if ( !mEnabled ) {

                            unregisterListener();
                            // mScreenManager.unregisterWakeLock();

                        } else {

                            registerListener();
                        }
                    }
                }
            }
        } );
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        if ( ActiveDisplayUtil.DEBUG ) {
            Log.d( tag, "Service has been destroyed" );
        }
        unregisterListener();
        // mScreenManager.unregisterWakeLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContextWrapper#stopService(android.content.Intent)
     */
    @Override
    public boolean stopService( Intent name ) {

        if ( ActiveDisplayUtil.DEBUG ) {
            Log.d( tag, "Service has been stopped" );
        }

        unregisterListener();
        // mScreenManager.unregisterWakeLock();

        return super.stopService( name );
    }

    /**
     * Register this as a sensor event listener.
     */
    private void registerListener() {
        mSensorManager.registerListener( this, mProximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL );
    }

    /**
     * Un-register this as a sensor event listener.
     */
    private void unregisterListener() {
        mSensorManager.unregisterListener( this );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
     * .Sensor, int)
     */
    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ) {
        if ( ActiveDisplayUtil.DEBUG ) {
            Log.d( tag, "onAccuracyChanged()" );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.hardware.SensorEventListener#onSensorChanged(android.hardware
     * .SensorEvent)
     */
    @Override
    public void onSensorChanged( SensorEvent event ) {

        if ( ActiveDisplayUtil.DEBUG ) {
            Log.d( tag,
                    "onSensorChanged - screen on? "
                            + mScreenManager.isScreenOn() );
        }
        float value = event.values[0];
        boolean isFar = value >= mProximitySensor.getMaximumRange();
        Log.d( tag, "ActiveDisplay ++++ proximity: " + isFar + "::" + value
                + "::" + mProximitySensor.getMaximumRange() );

        // -- get out if the screen is already turned on
        if ( mScreenManager.isScreenOn() ) {
            return;
        }

        if ( !isFar ) {

            // -- Mark that the proximity is near and get out
            // -- We need to have been near on previous pass and far
            // -- on this pass
            mProximityNear = true;

        } else {

            // -- Check the previous sensor reading and make sure it was near
            if ( mProximityNear ) {
                mScreenManager.turnScreenOn();
                mProximityNear = false;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind( Intent intent ) {
        return onBind( intent );
    }

    /**
     * Receiver
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        /*
         * (non-Javadoc)
         * 
         * @see
         * android.content.BroadcastReceiver#onReceive(android.content.Context,
         * android.content.Intent)
         */
        @Override
        public void onReceive( Context context, Intent intent ) {

            if ( ActiveDisplayUtil.DEBUG ) {
                Log.d( tag, "onReceive(" + intent + ")" );
            }

            if ( intent.getAction().equals( Intent.ACTION_SCREEN_ON ) ) {

                // -- turn off all the sensors
                unregisterListener();
                // mScreenManager.unregisterWakeLock();

            } else if ( intent.getAction().equals( Intent.ACTION_SCREEN_OFF ) ) {

                if ( mEnabled ) {
                    // -- Fire up the sensors
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if ( mEnabled ) {
                                if ( ActiveDisplayUtil.DEBUG ) {
                                    Log.d( tag, "Runnable executing." );
                                }
                                unregisterListener();
                                registerListener();
                                // mScreenManager.unregisterWakeLock();
                            }
                        }
                    };

                    new Handler().postDelayed( runnable,
                            SCREEN_OFF_RECEIVER_DELAY );
                }

            }

        }
    };

}
