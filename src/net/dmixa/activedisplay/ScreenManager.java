package net.dmixa.activedisplay;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * This class is a wrapper around turning on and off the screen
 * 
 */
public class ScreenManager {

    /** Delay before re-registering our sensor listenters */
    public static final int SCREEN_OFF_DELAY = 500;

    /** tag when writing ActiveDisplayUtil.DEBUG messages */
    private final String tag = this.getClass().getSimpleName();

    /** Handle to the power Manager */
    private PowerManager mPM;

    /** Handle to wake Lock */
    private WakeLock mWakeLock;

    /**
     * Construct a screen manager
     * 
     * @param service
     *            context to load POWER_SERVICE
     */
    public ScreenManager( Service service ) {

        mPM = (PowerManager) service.getSystemService( Context.POWER_SERVICE );
    }

    /**
     * Un-register the wake lock
     */
    private void unregisterWakeLock() {
        if ( null != mWakeLock ) {
            mWakeLock.release();
            if ( ActiveDisplayUtil.DEBUG ) {
                Log.d( tag, "Releasing Wake Lock" );
            }

        }
    }

    /**
     * create and aquire the wakeLock
     */
    private void registerWakeLock() {

        if ( null == mWakeLock ) {
            mWakeLock = mPM.newWakeLock( PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag" );
        }

    }

    /**
     * Turn on the screen
     */
    public void turnScreenOn() {

        registerWakeLock();
        if ( null != mWakeLock ) {
            if ( ActiveDisplayUtil.DEBUG ) {
                Log.d( tag, "Aquiring Wake Lock" );
            }
            mWakeLock.acquire();
            new Timer().schedule( new TimerTask() {
                @Override
                public void run() {
                    unregisterWakeLock();
                }
            }, SCREEN_OFF_DELAY );
        } else {
            if ( ActiveDisplayUtil.DEBUG ) {
                Log.d( tag, "Failed to register wakeLock" );
            }
        }
    }

    /**
     * 
     * @return flag for screen on or off
     */
    public boolean isScreenOn() {
        return mPM.isScreenOn();
    }
}
