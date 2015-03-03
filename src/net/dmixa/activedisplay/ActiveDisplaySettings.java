package net.dmixa.activedisplay;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * This class will display the ActiveDisplay Settings.
 * 
 */
public class ActiveDisplaySettings extends PreferenceActivity {

    /*
     * (non-Javadoc)
     * 
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.active_display_preferences );
    }

}
