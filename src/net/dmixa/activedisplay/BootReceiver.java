package net.dmixa.activedisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Class will be used to startup our service on boot
 */
public class BootReceiver extends BroadcastReceiver {

    /** tag when writing DEBUG messages. */
    private final String tag = this.getClass().getSimpleName();

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive( Context context, Intent intent ) {

        if ( Intent.ACTION_BOOT_COMPLETED.equals( intent.getAction() ) ) {

            Intent serviceActiveDisplay = new Intent( context,
                    ActiveDisplayService.class );
            context.startService( serviceActiveDisplay );

            Intent serviceActiveDisplayNotifications = new Intent( context,
                    ActiveDisplayService.class );
            context.startService( serviceActiveDisplayNotifications );

            if ( ActiveDisplayUtil.DEBUG ) {

                Log.d( tag, "ActiveDisplay BOOT_COMPLETED" );

            }
        }
    }
}